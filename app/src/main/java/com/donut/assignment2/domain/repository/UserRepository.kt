package com.donut.assignment2.domain.repository

import com.donut.assignment2.domain.model.User
import com.donut.assignment2.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    // 🔍 Basic CRUD Operations
    suspend fun getUserByPhone(phoneNumber: String): User?
    suspend fun getUsersByRole(role: UserRole): List<User>
    suspend fun saveUser(user: User): Result<Unit>
    suspend fun updateUser(user: User): Result<Unit>  // 🆕 Added this method
    suspend fun deleteUser(phoneNumber: String): Result<Unit>

    // 👨‍💼 Supervisor-Inspector Relationships
    suspend fun getInspectorsBySupervisor(supervisorPhone: String): List<User>
    suspend fun getAllSupervisors(): List<User>
    suspend fun countInspectorsBySupervisor(supervisorPhone: String): Int  // 🆕 Added this method

    // 🔄 Local Cache Operations
    suspend fun saveUserToLocal(user: User): Result<Unit>
    suspend fun clearLocalCache(): Result<Unit>

    // 📊 Real-time Data Flows
    fun getUsersFlow(): Flow<List<User>>  // 🆕 Added this method
    fun getInspectorsByRoleFlow(supervisorPhone: String): Flow<List<User>>  // 🆕 Added this method
}