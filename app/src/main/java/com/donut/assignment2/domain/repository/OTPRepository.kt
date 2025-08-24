package com.donut.assignment2.domain.repository

import com.donut.assignment2.domain.model.User

interface OTPRepository {
    suspend fun validateOTP(otpCode: String): Result<User?>
    suspend fun generateOTP(userId: String): Result<String>
    suspend fun isOTPValid(otpCode: String): Result<Boolean>
}