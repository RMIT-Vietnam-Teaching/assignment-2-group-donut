package com.donut.assignment2.domain.repository

import android.app.Activity
import com.donut.assignment2.data.repository.OTPResult
import com.donut.assignment2.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun sendOTP(phoneNumber: String, activity: Activity): Flow<OTPResult>
    suspend fun verifyOTP(verificationId: String, otp: String): Result<User> // Sửa lại tham số
    suspend fun getCurrentUser(): User?
    suspend fun signOut(): Result<Unit>
    fun isUserLoggedIn(): Boolean
    suspend fun updateUserProfile(displayName: String?, email: String?): Result<Unit>
    suspend fun deleteAccount(): Result<Unit>
    suspend fun refreshUser(): Result<User?>
}