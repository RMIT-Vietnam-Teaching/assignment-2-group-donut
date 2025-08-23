package com.donut.assignment2.domain.model

import java.time.LocalDateTime

data class User(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val fullName: String = "",
    val role: UserRole = UserRole.INSPECTOR,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class UserRole {
    INSPECTOR,
    SUPERVISOR
}