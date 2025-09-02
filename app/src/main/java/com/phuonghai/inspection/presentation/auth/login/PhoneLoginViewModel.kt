package com.phuonghai.inspection.presentation.auth.login

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.phuonghai.inspection.domain.repository.AuthState
import com.phuonghai.inspection.domain.repository.IAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

@HiltViewModel
class PhoneLoginViewModel @Inject constructor(
    private val authRepository: IAuthRepository
) : ViewModel() {

    val authState: SharedFlow<AuthState> = authRepository.authState

    fun sendVerificationCode(phoneNumber: String, activity: Activity) {
        authRepository.sendVerificationCode(phoneNumber, activity)
    }
}