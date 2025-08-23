package com.donut.assignment2.data.local.dao

import androidx.room.*
import com.donut.assignment2.data.local.entities.UserEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber")
    suspend fun getUserByPhone(phoneNumber: String): UserEntity?

    @Query("SELECT * FROM users WHERE role = :role")
    suspend fun getUsersByRole(role: String): List<UserEntity>

    @Query("SELECT * FROM users WHERE supervisorPhone = :supervisorPhone AND role = 'INSPECTOR'")
    suspend fun getInspectorsBySupervisor(supervisorPhone: String): List<UserEntity>

    @Query("SELECT COUNT(*) FROM users WHERE supervisorPhone = :supervisorPhone AND role = 'INSPECTOR'")
    suspend fun countInspectorsBySupervisor(supervisorPhone: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE phoneNumber = :phoneNumber")
    suspend fun deleteByPhone(phoneNumber: String)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}