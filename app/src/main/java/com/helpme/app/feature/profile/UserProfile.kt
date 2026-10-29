package com.helpme.app.feature.profile

import com.google.firebase.database.IgnoreExtraProperties

/** Modelo de usuario en Realtime Database: users/{uid} */
@IgnoreExtraProperties
data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val dpi: String? = null // opcional
)
