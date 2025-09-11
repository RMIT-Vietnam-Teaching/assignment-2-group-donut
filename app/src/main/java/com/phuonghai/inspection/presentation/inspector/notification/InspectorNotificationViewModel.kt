package com.phuonghai.inspection.presentation.home.inspector.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.phuonghai.inspection.domain.model.Notification
import com.phuonghai.inspection.domain.usecase.GetNotificationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationUiState(
    val isLoading: Boolean = false,
    val notifications: List<Notification> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class InspectorNotificationViewModel @Inject constructor(
    private val getNotificationUseCase: GetNotificationUseCase,
) : ViewModel() {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""


    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = getNotificationUseCase(userId)
            result.onSuccess { list ->
                _uiState.value = NotificationUiState(
                    isLoading = false,
                    notifications = list
                )
            }.onFailure { e ->
                _uiState.value = NotificationUiState(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}
