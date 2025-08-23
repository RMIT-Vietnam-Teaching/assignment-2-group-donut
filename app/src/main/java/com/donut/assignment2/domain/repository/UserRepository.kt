package com.donut.assignment2.domain.repository

import com.donut.assignment2.domain.model.User
import com.donut.assignment2.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getUserByPhone(phoneNumber: String): User?
    suspend fun getUsersByRole(role: UserRole): List<User>
    suspend fun getInspectorsBySupervisor(supervisorPhone: String): List<User>
    suspend fun getAllSupervisors(): List<User>
    suspend fun saveUser(user: User): Result<Unit>
    suspend fun deleteUser(phoneNumber: String): Result<Unit>
    suspend fun saveUserToLocal(user: User): Result<Unit>
    suspend fun clearLocalCache(): Result<Unit>
}