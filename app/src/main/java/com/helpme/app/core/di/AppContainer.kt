package com.helpme.app.core.di

import android.content.Context
import com.google.firebase.FirebaseApp
import com.helpme.app.data.firebase.FirebaseAuthRepository
import com.helpme.app.data.firebase.FirebaseReportRepository
import com.helpme.app.data.mock.MockAuthRepository
import com.helpme.app.data.mock.MockReportRepository
import com.helpme.app.data.mock.MockUserRepository
import com.helpme.app.domain.repository.AuthRepository
import com.helpme.app.domain.repository.ReportRepository
import com.helpme.app.domain.repository.UserRepository

object AppContainer {
    private var applicationContext: Context? = null
    private var usesLocalReportStorage = false

    fun init(context: Context) {
        applicationContext = context.applicationContext
    }

    private val isFirebaseAvailable: Boolean by lazy {
        try {
            FirebaseApp.getInstance()
            true
        } catch (exception: IllegalStateException) {
            false
        }
    }

    val authRepository: AuthRepository by lazy {
        if (isFirebaseAvailable) FirebaseAuthRepository() else MockAuthRepository()
    }

    val userRepository: UserRepository by lazy {
        MockUserRepository()
    }

    val reportRepository: ReportRepository by lazy {
        if (isFirebaseAvailable) {
            FirebaseReportRepository()
        } else {
            usesLocalReportStorage = true
            MockReportRepository(requireApplicationContext())
        }
    }

    val reportsAreStoredLocally: Boolean
        get() {
            reportRepository
            return usesLocalReportStorage
        }

    private fun requireApplicationContext(): Context =
        checkNotNull(applicationContext) { "AppContainer.init must be called before accessing repositories." }
}
