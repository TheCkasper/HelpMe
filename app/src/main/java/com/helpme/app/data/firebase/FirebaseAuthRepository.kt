package com.helpme.app.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.helpme.app.domain.model.AuthResult
import com.helpme.app.domain.model.OperationResult
import com.helpme.app.domain.model.SessionState
import com.helpme.app.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository : AuthRepository {
    private val auth = FirebaseAuth.getInstance()

    override suspend fun signInWithEmail(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return AuthResult.Error("Error desconocido al iniciar sesión")
            AuthResult.Success(uid)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error al iniciar sesión")
        }
    }

    override suspend fun registerWithEmail(
        name: String,
        email: String,
        password: String
    ): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return AuthResult.Error("Error desconocido al registrar")
            val uid = user.uid

            // Update FirebaseUser profile display name
            try {
                val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
                    displayName = name
                }
                user.updateProfile(profileUpdates).await()
            } catch (e: Exception) {
                // Ignore profile update error if any
            }

            // Save user profile directly in Realtime Database before completing registration
            try {
                val db = FirebaseDatabase.getInstance().reference
                val userRef = db.child("users").child(uid)
                val userMap = mapOf(
                    "name" to name,
                    "email" to email,
                    "rol" to "Ciudadano"
                )
                userRef.setValue(userMap).await()
            } catch (e: Exception) {
                // Ignore DB error if offline
            }

            AuthResult.Success(uid)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error al registrarse")
        }
    }

    override suspend fun sendPasswordReset(email: String): OperationResult {
        return try {
            auth.sendPasswordResetEmail(email).await()
            OperationResult.Success
        } catch (e: Exception) {
            OperationResult.Error(e.message ?: "Error al enviar enlace")
        }
    }

    override suspend fun signInWithGoogle(idToken: String): AuthResult {
        return try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: return AuthResult.Error("No se obtuvo objeto de usuario en Firebase tras autenticación")
            val uid = user.uid

            val googleName = user.displayName.orEmpty().ifBlank { 
                user.email?.substringBefore("@")?.replace(".", " ")?.replaceFirstChar { it.uppercase() }.orEmpty() 
            }.ifBlank { "Usuario Google" }
            val googleEmail = user.email.orEmpty()

            try {
                val db = FirebaseDatabase.getInstance().reference
                val userRef = db.child("users").child(uid)

                val nameSnap = userRef.child("name").get().await()
                val currentName = nameSnap.getValue(String::class.java).orEmpty()

                val updates = mutableMapOf<String, Any>(
                    "email" to googleEmail
                )
                if (currentName.isBlank()) {
                    updates["name"] = googleName
                }

                userRef.updateChildren(updates).await()

                val rolSnap = userRef.child("rol").get().await()
                if (!rolSnap.exists()) {
                    userRef.child("rol").setValue("Ciudadano").await()
                }
            } catch (e: Exception) {
                // Ignore DB error if offline so auth still succeeds
            }

            AuthResult.Success(uid)
        } catch (e: Exception) {
            AuthResult.Error("Error en Firebase Auth: ${e.localizedMessage ?: e.message}")
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override fun observeSession(): Flow<SessionState> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                trySend(SessionState.Authenticated(user.uid))
            } else {
                trySend(SessionState.Unauthenticated)
            }
        }

        auth.addAuthStateListener(authStateListener)

        awaitClose {
            auth.removeAuthStateListener(authStateListener)
        }
    }
}
