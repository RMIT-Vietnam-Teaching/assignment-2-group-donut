package com.phuonghai.inspection.presentation.auth.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phuonghai.inspection.domain.model.UserRole
import com.phuonghai.inspection.domain.repository.AuthState
import com.phuonghai.inspection.domain.repository.IAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _userRole = MutableStateFlow<UserRole?>(null)
    val userRole = _userRole.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        // ✅ LISTEN TO AUTH STATE CHANGES
        viewModelScope.launch {
            authRepository.authState.collect { authState ->
                when (authState) {
                    is AuthState.SignedOut -> {
                        // User signed out, clear role and stop loading
                        _userRole.value = null
                        _isLoading.value = false
                    }
                    else -> {
                        // For other auth states, check current user
                        checkCurrentUser()
                    }
                }
            }
        }

        // Initial check
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser()
                _userRole.value = currentUser?.role
                _isLoading.value = false
            } catch (e: Exception) {
                _userRole.value = null
                _isLoading.value = false
            }
        }
    }
}