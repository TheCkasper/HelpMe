package com.helpme.app.domain.model

data class User(
    val uid: String,
    val nombre: String,
    val correo: String,
    val dpi: String,
    val fechaActualizacion: Long,
    val rol: String,
    val estadoCuenta: String,
    val proveedorAuth: String,
    val fechaRegistro: Long
)
