package com.donut.assignment2.domain.model

import java.time.LocalDateTime

data class OTPSession(
    val otpCode: String,
    val userId: String,
    val userRole: UserRole,
    val expiryTime: LocalDateTime,
    val isUsed: Boolean = false
)