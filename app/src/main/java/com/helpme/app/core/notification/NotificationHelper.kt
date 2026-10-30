package com.helpme.app.core.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.helpme.app.MainActivity
import com.helpme.app.R

object NotificationHelper {

    private const val CHANNEL_ID_INCIDENTS = "helpme_incidents_channel"
    private const val CHANNEL_NAME_INCIDENTS = "Alertas de Incidencias Comunitarias"

    private const val CHANNEL_ID_STATUS = "helpme_status_channel"
    private const val CHANNEL_NAME_STATUS = "Cambios de Estado de Reportes"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val incidentsChannel = NotificationChannel(
                CHANNEL_ID_INCIDENTS,
                CHANNEL_NAME_INCIDENTS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones sobre nuevos reportes e incidencias en la zona"
                enableVibration(true)
            }

            val statusChannel = NotificationChannel(
                CHANNEL_ID_STATUS,
                CHANNEL_NAME_STATUS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones sobre actualización de estado de tus reportes"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(incidentsChannel)
            notificationManager.createNotificationChannel(statusChannel)
        }
    }

    @SuppressLint("MissingPermission")
    fun showNewReportNotification(context: Context, reportId: String, title: String, description: String) {
        val prefs = context.getSharedPreferences("helpme_settings", Context.MODE_PRIVATE)
        val notificationsEnabled = prefs.getBoolean("push_notifs_enabled", true)
        if (!notificationsEnabled) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to_report_id", reportId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            reportId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_INCIDENTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🚨 Nuevo Reporte: $title")
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationId = (System.currentTimeMillis() % 10000).toInt() + 1000
        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: Exception) {
            // Permission denied or missing
        }
    }

    @SuppressLint("MissingPermission")
    fun showStatusChangeNotification(context: Context, reportId: String, reportCode: String, newStatus: String) {
        val prefs = context.getSharedPreferences("helpme_settings", Context.MODE_PRIVATE)
        val alertsEnabled = prefs.getBoolean("emergency_alerts_enabled", true)
        if (!alertsEnabled) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to_report_id", reportId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            reportId.hashCode() + 1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_STATUS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("📋 Estado Actualizado ($reportCode)")
            .setContentText("El reporte ahora está en estado: $newStatus. Toca para ver.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationId = (System.currentTimeMillis() % 10000).toInt() + 2000
        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: Exception) {
            // Permission denied or missing
        }
    }
}
