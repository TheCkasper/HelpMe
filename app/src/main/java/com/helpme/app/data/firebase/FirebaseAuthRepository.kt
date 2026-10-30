package com.helpme.app.data.firebase

import com.google.firebase.auth.FirebaseAuth
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
            val uid = result.user?.uid ?: return AuthResult.Error("Error desconocido al registrar")
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
            val uid = result.user?.uid ?: return AuthResult.Error("Error al iniciar sesión con Google")
            AuthResult.Success(uid)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error al iniciar sesión con Google")
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
