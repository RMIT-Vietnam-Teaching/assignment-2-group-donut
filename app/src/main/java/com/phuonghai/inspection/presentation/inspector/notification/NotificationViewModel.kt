package com.phuonghai.inspection.presentation.home.inspector.notification

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor() : ViewModel() {

    companion object {
        private const val TAG = "NotificationVM"
    }

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    fun loadNotifications() {
        Log.d(TAG, "Loading notifications")

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // TODO: Load real notifications from repository
                kotlinx.coroutines.delay(1000) // Simulate loading

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    unreadCount = 2
                )

                Log.d(TAG, "Notifications loaded successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading notifications", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Không thể tải thông báo: ${e.message}"
                )
            }
        }
    }

    fun markAsRead(notificationId: String) {
        Log.d(TAG, "Marking notification as read: $notificationId")

        viewModelScope.launch {
            try {
                // TODO: Mark notification as read in repository
                val currentUnread = _uiState.value.unreadCount
                _uiState.value = _uiState.value.copy(
                    unreadCount = maxOf(0, currentUnread - 1)
                )

                Log.d(TAG, "Notification marked as read")
            } catch (e: Exception) {
                Log.e(TAG, "Error marking notification as read", e)
            }
        }
    }

    fun markAllAsRead() {
        Log.d(TAG, "Marking all notifications as read")

        viewModelScope.launch {
            try {
                // TODO: Mark all notifications as read in repository
                _uiState.value = _uiState.value.copy(unreadCount = 0)

                Log.d(TAG, "All notifications marked as read")
            } catch (e: Exception) {
                Log.e(TAG, "Error marking all notifications as read", e)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class NotificationUiState(
    val isLoading: Boolean = false,
    val unreadCount: Int = 0,
    val errorMessage: String? = null
) {
    val hasUnread: Boolean get() = unreadCount > 0
    val showError: Boolean get() = errorMessage != null
}