package com.helpme.app.core.notification

import android.content.Context
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.helpme.app.domain.model.Report

object GlobalNotificationListener {

    private var isListening = false
    private val database = FirebaseDatabase.getInstance().reference.child("reportes")
    private val knownReports = mutableMapOf<String, String>() // reportId -> estado
    private var isInitialLoadComplete = false
    private val appStartTime = System.currentTimeMillis()

    fun startListening(context: Context) {
        if (isListening) return
        isListening = true

        val applicationContext = context.applicationContext

        // Step 1: Pre-load all existing report keys so past reports never trigger notifications
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val report = child.getValue(Report::class.java)
                    val key = child.key ?: report?.reporteId.orEmpty()
                    if (key.isNotBlank()) {
                        knownReports[key] = report?.estado.orEmpty()
                    }
                }
                isInitialLoadComplete = true
                attachRealtimeChildListener(applicationContext)
            }

            override fun onCancelled(error: DatabaseError) {
                isInitialLoadComplete = true
                attachRealtimeChildListener(applicationContext)
            }
        })
    }

    private fun attachRealtimeChildListener(applicationContext: Context) {
        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                try {
                    val report = snapshot.getValue(Report::class.java) ?: return
                    val key = snapshot.key ?: report.reporteId
                    val finalReport = if (report.reporteId.isBlank()) report.copy(reporteId = key) else report

                    val isAlreadyKnown = knownReports.containsKey(key)
                    knownReports[key] = finalReport.estado

                    // Only notify for TRULY new reports created AFTER app start
                    val isNewReportCreatedAfterAppStart = finalReport.fechaCreacion >= (appStartTime - 10000)

                    if (!isAlreadyKnown && isInitialLoadComplete && isNewReportCreatedAfterAppStart) {
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
