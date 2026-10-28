package com.helpme.app.domain.model

data class Report(
    val reporteId: String = "",
    val tipo: String = "",
    val descripcion: String = "",
    val fotoUrl: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val estado: String = "Pendiente",
    val usuarioId: String = "",
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaActualizacion: Long = System.currentTimeMillis(),
    val tipoId: String = ""
)
