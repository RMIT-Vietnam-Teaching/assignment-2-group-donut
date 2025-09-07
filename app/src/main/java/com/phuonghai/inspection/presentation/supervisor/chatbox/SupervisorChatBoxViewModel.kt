package com.phuonghai.inspection.presentation.supervisor.chatbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.phuonghai.inspection.domain.model.ChatThread
import com.phuonghai.inspection.domain.model.User
import com.phuonghai.inspection.domain.repository.IChatMessageRepository
import com.phuonghai.inspection.domain.usecase.GetUserInformationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupervisorChatViewModel @Inject constructor(
    private val chatRepository: IChatMessageRepository,
    private val getUserInformationUseCase: GetUserInformationUseCase
) : ViewModel() {

    private val supervisorId = FirebaseAuth.getInstance().currentUser?.uid

    private val _listChatHistory = MutableStateFlow<List<ChatThread>>(emptyList())
    val listChatHistory: StateFlow<List<ChatThread>> = _listChatHistory

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadChatHistory()
    }

    private fun loadChatHistory() {
        supervisorId?.let { id ->
            viewModelScope.launch {
                _isLoading.value = true
                chatRepository.loadChatHistory(id).collect { threads ->
                    val threadsWithNames = threads.map { thread ->
                        async {
                            val result: Result<User?> = getUserInformationUseCase(thread.inspectorId)
                            val inspector = result.getOrNull()
                            ChatThread(
                                inspectorId = thread.inspectorId,
                                latestMessage = thread.latestMessage,
                                inspectorName = inspector?.fullName ?: "Unknown",
                                inspectorPhone = inspector?.phoneNumber ?: "Unknown"
                            )
                        }
                    }.awaitAll()

                    _listChatHistory.value = threadsWithNames
                    _isLoading.value = false
                }
            }
        }
    }
}