package com.helpme.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.helpme.app.core.di.AppContainer

class HelpMeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        AppContainer.init(this)
    }
}
