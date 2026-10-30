package com.helpme.app.core.notification

import android.content.Context
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.helpme.app.domain.model.Report

object GlobalNotificationListener {

    private var isListening = false
    private val database = FirebaseDatabase.getInstance().reference.child("reportes")
    private val knownReports = mutableMapOf<String, String>() // reportId -> estado
    private var isInitialLoadComplete = false

    fun startListening(context: Context) {
        if (isListening) return
        isListening = true

        val applicationContext = context.applicationContext

        // Mark initial load complete after 2 seconds so we only notify for NEW events coming after startup
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            isInitialLoadComplete = true
        }, 2000)

        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                try {
                    val report = snapshot.getValue(Report::class.java) ?: return
                    val key = snapshot.key ?: report.reporteId
                    val finalReport = if (report.reporteId.isBlank()) report.copy(reporteId = key) else report

                    knownReports[key] = finalReport.estado

                    // Only notify if initial load is complete (real-time new report created on ANY device)
                    if (isInitialLoadComplete) {
                        NotificationHelper.showNewReportNotification(
                            context = applicationContext,
                            reportId = finalReport.reporteId,
                            title = if (finalReport.tipo.isNotBlank()) finalReport.tipo else "Incidencia Comunitarias",
                            description = if (finalReport.descripcion.isNotBlank()) finalReport.descripcion else "Nuevo reporte publicado en la zona."
                        )
                    }
                } catch (e: Exception) {
                    // Ignore parsing error
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                try {
                    val report = snapshot.getValue(Report::class.java) ?: return
                    val key = snapshot.key ?: report.reporteId
                    val finalReport = if (report.reporteId.isBlank()) report.copy(reporteId = key) else report

                    val previousStatus = knownReports[key]
                    val newStatus = finalReport.estado

                    if (previousStatus != null && previousStatus != newStatus && isInitialLoadComplete) {
                        NotificationHelper.showStatusChangeNotification(
                            context = applicationContext,
                            reportId = finalReport.reporteId,
                            reportCode = finalReport.getFormattedCode(),
                            newStatus = newStatus
                        )
                    }

                    knownReports[key] = newStatus
                } catch (e: Exception) {
                    // Ignore
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
