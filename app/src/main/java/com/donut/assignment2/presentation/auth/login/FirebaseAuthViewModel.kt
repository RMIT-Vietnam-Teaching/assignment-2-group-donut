package com.donut.assignment2.presentation.auth.login

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.donut.assignment2.data.repository.OTPResult
import com.donut.assignment2.domain.model.User
import com.donut.assignment2.domain.usecase.auth.SendOTPUseCase
import com.donut.assignment2.domain.usecase.auth.VerifyOTPUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FirebaseAuthViewModel @Inject constructor(
    private val sendOTPUseCase: SendOTPUseCase,
    private val verifyOTPUseCase: VerifyOTPUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "FirebaseAuthViewModel"
    }

    private val _uiState = MutableStateFlow(FirebaseAuthUiState())
    val uiState: StateFlow<FirebaseAuthUiState> = _uiState.asStateFlow()

    fun sendOTP(phoneNumber: String, activity: Activity) {
        Log.d(TAG, "Sending OTP to: $phoneNumber")

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                otpSent = false,
                verificationId = null
            )

            try {
                sendOTPUseCase(phoneNumber, activity).collect { result ->
                    Log.d(TAG, "OTP Result: $result")

                    when (result) {
                        is OTPResult.Success -> {
                            Log.d(TAG, "OTP sent successfully, verificationId: ${result.verificationId}")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                otpSent = true,
                                verificationId = result.verificationId,
                                errorMessage = null
                            )
                        }
                        is OTPResult.Error -> {
                            Log.e(TAG, "OTP Error: ${result.exception.message}")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                otpSent = false,
                                errorMessage = "Gửi OTP thất bại: ${result.exception.message}"
                            )
                        }
                        is OTPResult.AutoVerified -> {
                            Log.d(TAG, "Auto verified")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isVerificationSuccess = true,
                                otpSent = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception sending OTP", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Lỗi không xác định: ${e.message}"
                )
            }
        }
    }

    fun verifyOTP(otpCode: String) {
        val verificationId = _uiState.value.verificationId
        Log.d(TAG, "Verifying OTP with verificationId: $verificationId")

        if (verificationId.isNullOrBlank()) {
            Log.w(TAG, "VerificationId is null or blank")
            _uiState.value = _uiState.value.copy(
                errorMessage = "Phiên xác thực không hợp lệ. Vui lòng gửi lại OTP."
            )
            return
        }

        verifyOTPWithId(verificationId, otpCode)
    }

    fun verifyOTPWithId(verificationId: String, otp: String) {
        Log.d(TAG, "Verifying OTP with ID: $verificationId, OTP: $otp")

        if (verificationId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "ID xác thực không hợp lệ"
            )
            return
        }

        if (otp.length != 6) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Mã OTP phải có 6 chữ số"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                verifyOTPUseCase(verificationId, otp)
                    .onSuccess { user ->
                        Log.d(TAG, "OTP verification successful for user: ${user.phoneNumber}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            user = user,
                            isVerificationSuccess = true,
                            errorMessage = null
                        )
                    }
                    .onFailure { error ->
                        Log.e(TAG, "OTP verification failed", error)

                        val errorMessage = getErrorMessage(error)

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = errorMessage
                        )
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during OTP verification", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Lỗi không xác định: ${e.message}"
                )
            }
        }
    }

    private fun getErrorMessage(error: Throwable): String {
        val errorMsg = error.message?.lowercase() ?: ""
        Log.d(TAG, "Processing error: $errorMsg")

        return when {
            errorMsg.contains("invalid-verification-code") ||
                    errorMsg.contains("invalid verification code") ||
                    errorMsg.contains("the verification code") -> {
                "Mã OTP không đúng. Vui lòng kiểm tra lại."
            }

            errorMsg.contains("session-expired") ||
                    errorMsg.contains("expired") ||
                    errorMsg.contains("timeout") -> {
                "Phiên xác thực đã hết hạn. Vui lòng gửi lại OTP."
            }

            errorMsg.contains("too-many-requests") ||
                    errorMsg.contains("quota") ||
                    errorMsg.contains("limit") -> {
                "Quá nhiều yêu cầu. Vui lòng thử lại sau vài phút."
            }

            errorMsg.contains("network") ||
                    errorMsg.contains("internet") ||
                    errorMsg.contains("connection") ||
                    errorMsg.contains("host") ||
                    errorMsg.contains("unreachable") -> {
                "Lỗi kết nối mạng. Vui lòng kiểm tra internet và thử lại."
            }

            errorMsg.contains("firebase") && errorMsg.contains("app") -> {
                "Lỗi cấu hình Firebase. Vui lòng liên hệ hỗ trợ."
            }

            else -> {
                // Log full error để debug
                Log.e(TAG, "Unhandled error: ${error.message}")
                "Xác thực thất bại. Vui lòng thử lại sau. (${error.javaClass.simpleName})"
            }
        }
    }

    fun resetNavigationState() {
        Log.d(TAG, "Resetting navigation state")
        _uiState.value = _uiState.value.copy(
            isVerificationSuccess = false,
            user = null
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class FirebaseAuthUiState(
    val isLoading: Boolean = false,
    val otpSent: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
    val verificationId: String? = null,
    val isVerificationSuccess: Boolean = false
)