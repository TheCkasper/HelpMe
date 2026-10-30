package com.helpme.app.domain.model

import com.google.firebase.database.Exclude
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Report(
    val reporteId: String = "",
    val codigoReporte: String = "",
    val tipo: String = "",
    val tipoId: String = "",
    val descripcion: String = "",
    val fotoUrl: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val estado: String = "Pendiente",
    val usuarioId: String = "",
    val nombreUsuario: String = "",
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaActualizacion: Long = System.currentTimeMillis()
) {
    @Exclude
    fun getFormattedCode(): String {
        if (codigoReporte.isNotBlank()) return codigoReporte
        val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(fechaCreacion))
        val shortId = if (reporteId.length >= 4) reporteId.takeLast(4).uppercase() else "001"
        return "#HP-$dateStr-$shortId"
    }

    @Exclude
    fun getFormattedDate(): String {
        return SimpleDateFormat("dd/MM/yyyy - hh:mm a", Locale.getDefault()).format(Date(fechaCreacion))
    }
}
