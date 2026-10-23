package com.helpme.app.domain.repository

import com.helpme.app.domain.model.User

interface UserRepository {
    suspend fun getUserProfile(uid: String): User?
}
