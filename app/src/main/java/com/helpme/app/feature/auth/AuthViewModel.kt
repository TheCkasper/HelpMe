package com.helpme.app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helpme.app.core.di.AppContainer
import com.helpme.app.domain.model.AuthResult
import com.helpme.app.domain.model.OperationResult
import com.helpme.app.domain.model.SessionState
import com.helpme.app.domain.validation.AuthValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authRepository = AppContainer.authRepository
    
    val sessionState: StateFlow<SessionState> = authRepository.observeSession()
        .let { flow ->
            val stateFlow = MutableStateFlow<SessionState>(SessionState.Loading)
            viewModelScope.launch { flow.collect { stateFlow.value = it } }
            stateFlow.asStateFlow()
        }

    private val _loginState = MutableStateFlow<AuthResult>(AuthResult.Idle)
    val loginState = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<AuthResult>(AuthResult.Idle)
    val registerState = _registerState.asStateFlow()

    private val _resetState = MutableStateFlow<OperationResult>(OperationResult.Idle)
    val resetState = _resetState.asStateFlow()

    fun login(email: String, password: String) {
        if (!AuthValidator.isValidEmail(email)) {
            _loginState.value = AuthResult.Error("Correo electrónico inválido")
            return
        }
        if (!AuthValidator.isValidPassword(password)) {
            _loginState.value = AuthResult.Error("La contraseña debe tener al menos 8 caracteres")
            return
        }

        viewModelScope.launch {
            _loginState.value = AuthResult.Loading
            _loginState.value = authRepository.signInWithEmail(email, password)
        }
    }

    fun register(name: String, email: String, password: String, confirm: String) {
        if (!AuthValidator.isValidName(name)) {
            _registerState.value = AuthResult.Error("El nombre es inválido (2-80 caracteres)")
            return
        }
        if (!AuthValidator.isValidEmail(email)) {
            _registerState.value = AuthResult.Error("Correo electrónico inválido")
            return
        }
        if (!AuthValidator.isValidPassword(password)) {
            _registerState.value = AuthResult.Error("La contraseña debe tener al menos 8 caracteres")
            return
        }
        if (password != confirm) {
            _registerState.value = AuthResult.Error("Las contraseñas no coinciden")
            return
        }

        viewModelScope.launch {
            _registerState.value = AuthResult.Loading
            _registerState.value = authRepository.registerWithEmail(name, email, password)
        }
    }

    fun resetPassword(email: String) {
        if (!AuthValidator.isValidEmail(email)) {
            _resetState.value = OperationResult.Error("Correo electrónico inválido")
            return
        }

        viewModelScope.launch {
            _resetState.value = OperationResult.Loading
            _resetState.value = authRepository.sendPasswordReset(email)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
    
    fun resetStates() {
        _loginState.value = AuthResult.Idle
        _registerState.value = AuthResult.Idle
        _resetState.value = OperationResult.Idle
    }
}
