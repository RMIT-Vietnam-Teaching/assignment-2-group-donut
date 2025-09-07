package com.phuonghai.inspection.domain.repository

import com.phuonghai.inspection.domain.model.ChatMessage
import com.phuonghai.inspection.domain.model.ChatThread
import kotlinx.coroutines.flow.Flow

interface IChatMessageRepository {
    suspend fun sendMessage(
        supervisorId: String,
        inspectorId: String,
        message: ChatMessage
    )
    // Listen to all messages in a conversation (real-time updates)
    fun getMessages(
        supervisorId: String,
        inspectorId: String
    ): Flow<List<ChatMessage>>

    suspend fun loadChatHistory(supervisorId: String): Flow<List<ChatThread>>
}