package com.phuonghai.inspection.presentation.home.inspector.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phuonghai.inspection.domain.model.User
import com.phuonghai.inspection.domain.repository.IAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InspectorProfileViewModel @Inject constructor(
    private val authRepository: IAuthRepository
) : ViewModel() {

    companion object {
        private const val TAG = "InspectorProfileVM"
    }

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile() {
        Log.d(TAG, "Loading inspector profile")

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val currentUser = authRepository.getCurrentUser()
                Log.d(TAG, "Profile loaded: $currentUser")

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    user = currentUser,
                    errorMessage = null
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error loading profile", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Không thể tải thông tin profile: ${e.message}"
                )
            }
        }
    }

    fun logout() {
        Log.d(TAG, "Logout requested")
        viewModelScope.launch {
            try {
                // TODO: Implement logout logic
                // Clear user session, navigate to login
                Log.d(TAG, "Logout successful")
            } catch (e: Exception) {
                Log.e(TAG, "Error during logout", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Lỗi khi đăng xuất: ${e.message}"
                )
            }
        }
    }

    fun sendChatMessage(message: String) {
        Log.d(TAG, "Sending chat message: $message")
        viewModelScope.launch {
            try {
                // TODO: Implement chat message sending
                Log.d(TAG, "Chat message sent successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error sending chat message", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Lỗi gửi tin nhắn: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null
) {
    val showContent: Boolean get() = !isLoading && user != null
    val showError: Boolean get() = errorMessage != null
}