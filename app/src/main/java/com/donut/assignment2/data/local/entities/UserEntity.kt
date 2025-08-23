package com.donut.assignment2.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val username: String,
    val email: String,
    val fullName: String,
    val role: String, // UserRole as String
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val password: String // Simple password storage for demo
)
