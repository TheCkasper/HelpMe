package com.helpme.app

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.FirebaseApp
import com.helpme.app.core.di.AppContainer
import com.helpme.app.core.notification.NotificationHelper

class HelpMeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        AppContainer.init(this)
        NotificationHelper.createNotificationChannels(this)

        val prefs = getSharedPreferences("helpme_settings", Context.MODE_PRIVATE)
        val themeMode = prefs.getInt("app_theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(themeMode)
    }
}
