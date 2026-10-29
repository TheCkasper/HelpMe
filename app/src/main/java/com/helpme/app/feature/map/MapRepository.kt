package com.helpme.app.feature.map

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.helpme.app.domain.model.Report
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

interface MapRepository {
    fun observeReports(): Flow<List<Report>>
}

class FirebaseMapRepository : MapRepository {
    private val database = FirebaseDatabase.getInstance().reference.child("reportes")

    override fun observeReports(): Flow<List<Report>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val reports = mutableListOf<Report>()
                for (child in snapshot.children) {
                    try {
                        val report = child.getValue(Report::class.java)
                        if (report != null) {
                            val key = child.key ?: report.reporteId
                            val finalReport = if (report.reporteId.isBlank()) report.copy(reporteId = key) else report
                            reports.add(finalReport)
                        } else {
                            // Fallback manual parsing in case of field type mismatches
                            val id = child.child("reporteId").getValue(String::class.java) ?: child.key ?: ""
                            val tipo = child.child("tipo").getValue(String::class.java) ?: ""
                            val descripcion = child.child("descripcion").getValue(String::class.java) ?: ""
                            val fotoUrl = child.child("fotoUrl").getValue(String::class.java) ?: ""
                            val latitud = child.child("latitud").getValue(Double::class.java) ?: 0.0
                            val longitud = child.child("longitud").getValue(Double::class.java) ?: 0.0
                            val estado = child.child("estado").getValue(String::class.java) ?: "Pendiente"
                            val usuarioId = child.child("usuarioId").getValue(String::class.java) ?: ""
                            val fechaCreacion = child.child("fechaCreacion").getValue(Long::class.java) ?: 0L
                            val fechaActualizacion = child.child("fechaActualizacion").getValue(Long::class.java) ?: 0L
                            val tipoId = child.child("tipoId").getValue(String::class.java) ?: ""

                            reports.add(
                                Report(
                                    reporteId = id,
                                    tipo = tipo,
                                    descripcion = descripcion,
                                    fotoUrl = fotoUrl,
                                    latitud = latitud,
                                    longitud = longitud,
                                    estado = estado,
                                    usuarioId = usuarioId,
                                    fechaCreacion = fechaCreacion,
                                    fechaActualizacion = fechaActualizacion,
                                    tipoId = tipoId
                                )
                            )
                        }
                    } catch (e: Exception) {
                        // Skip malformed individual record gracefully
                    }
                }
                trySend(reports)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        database.addValueEventListener(listener)
        awaitClose {
            database.removeEventListener(listener)
        }
    }
}
