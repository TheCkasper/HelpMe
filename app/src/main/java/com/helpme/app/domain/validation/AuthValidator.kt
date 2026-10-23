package com.helpme.app.domain.validation

import android.util.Patterns

object AuthValidator {
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.isNotBlank() && password.length >= 8
    }

    fun isValidName(name: String): Boolean {
        return name.isNotBlank() && name.length in 2..80
    }
}
