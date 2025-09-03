package com.phuonghai.inspection.domain.repository

import com.phuonghai.inspection.domain.model.User

interface IUserRepository {
    suspend fun getInspectors() : Result<List<User>>
    suspend fun getUserById(): Result<User?>
}