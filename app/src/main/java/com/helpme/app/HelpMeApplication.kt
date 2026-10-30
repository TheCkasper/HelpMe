package com.helpme.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.helpme.app.core.di.AppContainer
import com.helpme.app.core.notification.NotificationHelper

class HelpMeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        AppContainer.init(this)
        NotificationHelper.createNotificationChannels(this)
    }
}
