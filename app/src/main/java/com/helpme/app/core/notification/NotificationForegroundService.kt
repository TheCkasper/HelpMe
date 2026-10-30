package com.helpme.app.core.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.helpme.app.R

class NotificationForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundNotification()
        GlobalNotificationListener.startListening(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        GlobalNotificationListener.startListening(applicationContext)
        return START_STICKY
    }

    private fun startForegroundNotification() {
        val channelId = "helpme_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Servicio de Monitoreo HelpMe",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Mantiene activa la recepción de notificaciones de emergencias en segundo plano"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("HelpMe Activo")
            .setContentText("Monitoreando emergencias ciudadanas en tiempo real...")
            .setSmallIcon(R.mipmap.ic_helpme_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        try {
            startForeground(9999, notification)
        } catch (e: Exception) {
            // Ignore foreground service permission issues on older API edge cases
        }
    }
}
