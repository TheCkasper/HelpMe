package com.helpme.app.data.mock

import com.helpme.app.domain.model.AuthResult
import com.helpme.app.domain.model.OperationResult
import com.helpme.app.domain.model.SessionState
import com.helpme.app.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class MockAuthRepository : AuthRepository {
    private val sessionFlow = MutableStateFlow<SessionState>(SessionState.Unauthenticated)
    private val mockUsers = mutableMapOf<String, String>() // email -> password

    override suspend fun signInWithEmail(email: String, password: String): AuthResult {
        delay(1000)
        return if (mockUsers[email] == password || (email == "test@test.com" && password == "12345678")) {
            val uid = "UID_DE_PRUEBA"
            sessionFlow.value = SessionState.Authenticated(uid)
            AuthResult.Success(uid)
        } else {
            AuthResult.Error("Credenciales inválidas en modo demo")
        }
    }

    override suspend fun registerWithEmail(
        name: String,
        email: String,
        password: String
    ): AuthResult {
        delay(1000)
        if (mockUsers.containsKey(email)) {
            return AuthResult.Error("El correo ya está registrado en modo demo")
        }
        mockUsers[email] = password
        val uid = "UID_DE_PRUEBA_${System.currentTimeMillis()}"
        sessionFlow.value = SessionState.Authenticated(uid)
        return AuthResult.Success(uid)
    }

    override suspend fun sendPasswordReset(email: String): OperationResult {
        delay(1000)
        return OperationResult.Success
    }

    override suspend fun signInWithGoogle(idToken: String): AuthResult {
        delay(1000)
        val uid = "GOOGLE_UID_DEMO"
        sessionFlow.value = SessionState.Authenticated(uid)
        return AuthResult.Success(uid)
    }

    override suspend fun signOut() {
        delay(500)
        sessionFlow.value = SessionState.Unauthenticated
    }

    override fun observeSession(): Flow<SessionState> = sessionFlow
}
