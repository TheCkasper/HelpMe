package com.helpme.app.data.mock

import com.helpme.app.domain.model.User
import com.helpme.app.domain.repository.UserRepository
import kotlinx.coroutines.delay

class MockUserRepository : UserRepository {
    override suspend fun getUserProfile(uid: String): User? {
        delay(500)
        if (uid.startsWith("UID_DE_PRUEBA")) {
            return User(
                uid = uid,
                nombre = "Usuario Demo",
                correo = "test@test.com",
                dpi = "",
                fechaActualizacion = System.currentTimeMillis(),
                rol = "Ciudadano",
                estadoCuenta = "Activo",
                proveedorAuth = "password",
                fechaRegistro = System.currentTimeMillis()
            )
        }
        return null
    }
}
