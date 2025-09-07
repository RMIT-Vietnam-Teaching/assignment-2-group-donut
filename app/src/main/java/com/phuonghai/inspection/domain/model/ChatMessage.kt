package com.phuonghai.inspection.domain.model


data class ChatMessage(
    val id: String = "",          // messageId (Firestore doc ID or Realtime push key)
    val senderId: String = "",    // UID of the sender
    val receiverId: String = "",  // UID of the receiver
    val text: String = "",        // message content
    val timestamp: Long = 0L      // Unix time in milliseconds
)

data class ChatThread(
    val inspectorId: String = "",
    val latestMessage: ChatMessage? = null,
    val inspectorName: String? = null,
    val inspectorPhone: String? = null,
)
