package com.phuonghai.inspection.presentation.auth.login

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.phuonghai.inspection.domain.repository.AuthState
import com.phuonghai.inspection.domain.repository.IAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.phuonghai.inspection.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: IAuthRepository
) : ViewModel() {
    private val _userRole = MutableStateFlow<UserRole?>(null)
    val userRole: StateFlow<UserRole?> = _userRole.asStateFlow()
    val authState: SharedFlow<AuthState> = authRepository.authState

    init {
        viewModelScope.launch {
            authState.collect { state ->
                if (state is AuthState.Success) {
                    val currentUser = authRepository.getCurrentUser()
                    _userRole.value = currentUser?.role
                }
            }
        }
    }

    fun sendVerificationCode(phoneNumber: String, activity: Activity) {
        authRepository.sendVerificationCode(phoneNumber, activity)
    }

    fun verifyCode(verificationId: String, code: String) {
        authRepository.verifyCode(verificationId, code)
    }
}