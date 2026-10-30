package com.helpme.app.data.firebase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.helpme.app.domain.model.OperationResult
import com.helpme.app.domain.model.Report
import com.helpme.app.domain.repository.ReportRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FirebaseReportRepository : ReportRepository {
    private val database = FirebaseDatabase.getInstance().reference.child("reportes")

    override suspend fun createReport(report: Report): OperationResult {
        return try {
            val key = database.push().key ?: return OperationResult.Error("No se pudo generar el ID del reporte")
            val now = System.currentTimeMillis()
            val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(now))
            val shortKey = key.takeLast(4).uppercase()
            val code = "#HP-$dateStr-$shortKey"

            val finalReport = report.copy(
                reporteId = key,
                codigoReporte = code,
                fechaCreacion = if (report.fechaCreacion == 0L) now else report.fechaCreacion,
                fechaActualizacion = now
            )
            database.child(key).setValue(finalReport).await()
            OperationResult.Success
        } catch (e: Exception) {
            OperationResult.Error(e.message ?: "Error al guardar el reporte en Firebase")
        }
    }

    override fun observeReports(): Flow<List<Report>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Report>()
                for (child in snapshot.children) {
                    val report = child.getValue(Report::class.java)
                    if (report != null) {
                        list.add(report)
                    }
                }
                // Sort by newest first
                trySend(list.sortedByDescending { it.fechaCreacion })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        database.addValueEventListener(listener)
        awaitClose { database.removeEventListener(listener) }
    }

    override suspend fun getReportById(reportId: String): Report? {
        return try {
            val snapshot = database.child(reportId).get().await()
            snapshot.getValue(Report::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateReportStatus(reportId: String, newStatus: String): OperationResult {
        return try {
            val updates = mapOf(
                "estado" to newStatus,
                "fechaActualizacion" to System.currentTimeMillis()
            )
            database.child(reportId).updateChildren(updates).await()
            OperationResult.Success
        } catch (e: Exception) {
            OperationResult.Error(e.message ?: "Error al actualizar el estado")
        }
    }

    override fun observeUserReports(userId: String): Flow<List<Report>> = callbackFlow {
        val query = database.orderByChild("usuarioId").equalTo(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Report>()
                for (child in snapshot.children) {
                    val report = child.getValue(Report::class.java)
                    if (report != null) {
                        list.add(report)
                    }
                }
                trySend(list.sortedByDescending { it.fechaCreacion })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }
}
