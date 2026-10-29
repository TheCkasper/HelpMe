package com.helpme.app.data.firebase

import com.google.firebase.database.FirebaseDatabase
import com.helpme.app.domain.model.OperationResult
import com.helpme.app.domain.model.Report
import com.helpme.app.domain.repository.ReportRepository
import kotlinx.coroutines.tasks.await

class FirebaseReportRepository : ReportRepository {
    private val database = FirebaseDatabase.getInstance().reference.child("reportes")

    override suspend fun createReport(report: Report): OperationResult {
        return try {
            val key = database.push().key ?: return OperationResult.Error("No se pudo generar el ID del reporte")
            val now = System.currentTimeMillis()
            val finalReport = report.copy(
                reporteId = key,
                fechaCreacion = if (report.fechaCreacion == 0L) now else report.fechaCreacion,
                fechaActualizacion = now
            )
            database.child(key).setValue(finalReport).await()
            OperationResult.Success
        } catch (e: Exception) {
            OperationResult.Error(e.message ?: "Error al guardar el reporte en Firebase")
        }
    }
}
