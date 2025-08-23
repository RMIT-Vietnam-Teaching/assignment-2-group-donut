package com.donut.assignment2.domain.repository

import com.donut.assignment2.domain.model.User
import com.donut.assignment2.domain.model.UserRole

interface UserRepository {
    suspend fun getUserById(id: String): Result<User?>
    suspend fun getUserByUsername(username: String): Result<User?>
    suspend fun createUser(user: User): Result<String>
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun authenticateUser(username: String, password: String): Result<User?>
    suspend fun getUsersByRole(role: UserRole): Result<List<User>>
}