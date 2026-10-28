package com.helpme.app.core.di

import com.helpme.app.data.firebase.FirebaseAuthRepository
import com.helpme.app.data.firebase.FirebaseReportRepository
import com.helpme.app.data.mock.MockUserRepository
import com.helpme.app.domain.repository.AuthRepository
import com.helpme.app.domain.repository.ReportRepository
import com.helpme.app.domain.repository.UserRepository

object AppContainer {
    val authRepository: AuthRepository by lazy {
        FirebaseAuthRepository()
    }
    
    val userRepository: UserRepository by lazy {
        MockUserRepository()
    }

    val reportRepository: ReportRepository by lazy {
        FirebaseReportRepository()
    }
}
