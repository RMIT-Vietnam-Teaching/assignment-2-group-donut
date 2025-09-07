package com.phuonghai.inspection.presentation.supervisor.chatbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.phuonghai.inspection.domain.model.ChatMessage
import com.phuonghai.inspection.domain.repository.IChatMessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupervisorChatDetailViewModel @Inject constructor(
    private val chatRepository: IChatMessageRepository
): ViewModel() {
    private val supervisorId = FirebaseAuth.getInstance().currentUser?.uid

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun getMessages(inspectorId: String) {
        if(supervisorId != null) {
            viewModelScope.launch {
                _isLoading.value = true
                chatRepository.getMessages(supervisorId, inspectorId).collect { messages ->
                    _messages.value = messages
                    _isLoading.value = false
                }
            }
        }
    }
    fun sendMessage(inspectorId: String, message: ChatMessage) {
        if(supervisorId != null) {
            viewModelScope.launch {
                _isLoading.value = true
                chatRepository.sendMessage(supervisorId, inspectorId, message)
                _isLoading.value = false
            }
        }
    }
}