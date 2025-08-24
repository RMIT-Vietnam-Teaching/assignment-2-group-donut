package com.donut.assignment2.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["supervisorPhone"]),
        Index(value = ["role"])
    ]
)
data class UserEntity(
    @PrimaryKey
    val phoneNumber: String,             // Primary Key
    val fullName: String,                // Full name
    val email: String?,                  // Email (nullable)
    val role: String,                    // UserRole as String
    val supervisorPhone: String? = null, // Supervisor's phone number
    val profileImageUrl: String? = null  // Avatar URL (nullable)
)