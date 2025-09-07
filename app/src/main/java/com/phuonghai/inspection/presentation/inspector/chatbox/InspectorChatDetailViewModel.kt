package com.phuonghai.inspection.presentation.inspector.chatbox

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.phuonghai.inspection.domain.model.ChatMessage
import com.phuonghai.inspection.domain.model.User
import com.phuonghai.inspection.domain.repository.IChatMessageRepository
import com.phuonghai.inspection.domain.usecase.GetUserInformationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InspectorChatDetailViewModel @Inject constructor(
    private val chatMessageRepository: IChatMessageRepository,
    private val getUserInformationUseCase: GetUserInformationUseCase
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _supervisorName = MutableStateFlow("")
    val supervisorName: StateFlow<String> = _supervisorName

    // keep supervisorId for reuse (e.g., sendMessage)
    private var supervisorId: String? = null

    /**
     * Load chat history between inspector and supervisor.
     */
    fun getMessages(inspectorId: String) {
        _isLoading.value = true

        viewModelScope.launch {
            // fetch inspector info (to get supervisorId)
            val result: Result<User?> = getUserInformationUseCase(inspectorId)
            val inspector = result.getOrNull()
            supervisorId = inspector?.supervisorId // <-- save supervisorId here

            // fetch supervisor details
            supervisorId?.let { supId ->
                val supervisorResult: Result<User?> = getUserInformationUseCase(supId)
                val supervisor = supervisorResult.getOrNull()
                _supervisorName.value = supervisor?.fullName ?: "Unknown"

                // collect chat messages
                chatMessageRepository.getMessages(supId, inspectorId)
                    .collect { messages ->
                        _messages.value = messages
                        _isLoading.value = false
                    }
            }
        }
    }

    /**
     * Send a new message
     */
    fun sendMessage(inspectorId: String, text: String) {
        supervisorId?.let { supId ->
            viewModelScope.launch {
                _isLoading.value = true
                val message = ChatMessage(
                    senderId = inspectorId,
                    receiverId = supId, // <-- supervisor is receiver
                    text = text,
                    timestamp = System.currentTimeMillis()
                )
                chatMessageRepository.sendMessage(supId, inspectorId, message)
                _isLoading.value = false
            }
        }
    }
}
