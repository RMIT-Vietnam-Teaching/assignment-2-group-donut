package com.donut.assignment2.domain.model

import java.time.LocalDateTime

data class User(
    val phoneNumber: String,             // Primary Key (unique identifier)
    val fullName: String,                // Tên đầy đủ
    val email: String?,                  // Email (nullable)
    val role: UserRole,                  // INSPECTOR, SUPERVISOR, ADMIN
    val supervisorPhone: String? = null, // Phone supervisor (chỉ Inspector có)
    val profileImageUrl: String? = null  // Avatar URL (nullable)
)

enum class UserRole {
    INSPECTOR,      // Người kiểm tra
    SUPERVISOR     // Người giám sát
}