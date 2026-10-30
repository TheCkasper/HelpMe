package com.helpme.app.domain.repository

import com.helpme.app.domain.model.AuthResult
import com.helpme.app.domain.model.OperationResult
import com.helpme.app.domain.model.SessionState
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signInWithEmail(email: String, password: String): AuthResult
    suspend fun registerWithEmail(name: String, email: String, password: String): AuthResult
    suspend fun sendPasswordReset(email: String): OperationResult
    suspend fun signInWithGoogle(idToken: String): AuthResult
    suspend fun signOut()
    fun observeSession(): Flow<SessionState>
}
