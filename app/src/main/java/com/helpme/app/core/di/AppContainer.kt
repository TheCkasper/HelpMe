package com.helpme.app.core.di

import com.helpme.app.data.firebase.FirebaseAuthRepository
import com.helpme.app.data.mock.MockUserRepository
import com.helpme.app.domain.repository.AuthRepository
import com.helpme.app.domain.repository.UserRepository

object AppContainer {
    val authRepository: AuthRepository by lazy {
        FirebaseAuthRepository() // Firebase conectado
    }
    
    val userRepository: UserRepository by lazy {
        MockUserRepository()
    }
}
