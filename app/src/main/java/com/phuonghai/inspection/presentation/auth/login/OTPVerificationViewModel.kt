package com.phuonghai.inspection.presentation.auth.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.phuonghai.inspection.domain.model.UserRole
import com.phuonghai.inspection.domain.repository.AuthState
import com.phuonghai.inspection.domain.repository.IAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OTPVerificationViewModel @Inject constructor(
    private val repo: IAuthRepository
) : ViewModel() {

    val authState: SharedFlow<AuthState> = repo.authState

    private val _userRole = MutableStateFlow<UserRole?>(null)
    val userRole: StateFlow<UserRole?> = _userRole.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _lastMessage = MutableStateFlow<String?>(null)
    val lastMessage: StateFlow<String?> = _lastMessage.asStateFlow()

    init {
        viewModelScope.launch {
            authState.collect { state ->
                Log.d(TAG, "=== STATE CHANGED: ${state::class.simpleName} ===")

                when (state) {
                    is AuthState.Loading -> {
                        _isLoading.value = true
                        _lastMessage.value = "Loading…"
                        Log.d(TAG, "AuthState.Loading")
                    }
                    is AuthState.CodeSent -> {
                        _isLoading.value = false
                        _lastMessage.value = "CodeSent vid=${state.verificationId.takeLast(6)}"
                        Log.d(TAG, "AuthState.CodeSent: ${state.verificationId}")
                    }
                    is AuthState.CodeTimeout -> {
                        _isLoading.value = false
                        _lastMessage.value = "CodeTimeout"
                        Log.w(TAG, "AuthState.CodeTimeout")
                    }
                    is AuthState.Error -> {
                        _isLoading.value = false
                        _lastMessage.value = "Error: ${state.message}"
                        Log.e(TAG, "AuthState.Error: ${state.message}")
                    }
                    is AuthState.Success -> {
                        _isLoading.value = false
                        _lastMessage.value = "Success -> fetch role"
                        Log.d(TAG, "AuthState.Success -> calling getCurrentUser()")

                        try {
                            // ✅ SỬA CÁCH LẤY UID
                            Log.d(TAG, "Firebase Auth UID: ${FirebaseAuth.getInstance().currentUser?.uid}")

                            val user = repo.getCurrentUser()
                            Log.d(TAG, "getCurrentUser() returned: $user")

                            if (user == null) {
                                Log.e(TAG, "⚠️ USER IS NULL!")
                                _lastMessage.value = "User null!"
                            } else {
                                Log.d(TAG, "✅ User found: role=${user.role}")
                                _userRole.value = user.role
                                _lastMessage.value = "Got role: ${user.role}"
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "❌ Exception in getCurrentUser()", e)
                            _lastMessage.value = "Exception: ${e.message}"
                        }
                    }
                    // ✅ THÊM ELSE BRANCH ĐỂ HANDLE EXHAUSTIVE
                    else -> {
                        Log.d(TAG, "Unhandled state: $state")
                    }
                }
            }
        }
    }

    fun verifyCode(verificationId: String, code: String) {
        Log.d(TAG, "verifyCode() called: vid=$verificationId, code=$code")
        viewModelScope.launch {
            repo.verifyCode(verificationId, code)
        }
    }

    companion object {
        private const val TAG = "OTP_VM_DEBUG"
    }
}