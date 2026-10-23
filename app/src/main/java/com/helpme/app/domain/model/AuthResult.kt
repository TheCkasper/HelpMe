package com.helpme.app.domain.model

sealed class AuthResult {
    data class Success(val uid: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult()
    object Idle : AuthResult()
}

sealed class OperationResult {
    object Success : OperationResult()
    data class Error(val message: String) : OperationResult()
    object Loading : OperationResult()
    object Idle : OperationResult()
}

sealed class SessionState {
    data class Authenticated(val uid: String) : SessionState()
    object Unauthenticated : SessionState()
    object Loading : SessionState()
}
