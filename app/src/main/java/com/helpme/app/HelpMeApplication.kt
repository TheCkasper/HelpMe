package com.helpme.app

import android.app.Application
import com.helpme.app.core.di.AppContainer

class HelpMeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize AppContainer or Firebase if needed
        val auth = AppContainer.authRepository
    }
}
