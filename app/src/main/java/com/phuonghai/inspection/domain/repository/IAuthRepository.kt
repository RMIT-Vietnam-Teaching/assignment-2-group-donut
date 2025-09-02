package com.phuonghai.inspection.domain.repository

import android.app.Activity
import com.phuonghai.inspection.domain.model.User
import kotlinx.coroutines.flow.SharedFlow

interface IAuthRepository {
    fun sendVerificationCode(phoneNumber: String, activity: Activity)
    fun verifyCode(verificationId: String, code: String)

    suspend fun getCurrentUser(): User?
    val authState: SharedFlow<AuthState>
}

sealed class AuthState {
    object Success : AuthState()
    data class CodeSent(val verificationId: String) : AuthState()
    data class Error(val message: String?) : AuthState()
    object Loading : AuthState()
    object CodeTimeout : AuthState() // <= thêm
}
