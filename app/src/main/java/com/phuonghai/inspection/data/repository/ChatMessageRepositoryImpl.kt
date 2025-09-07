package com.phuonghai.inspection.data.repository

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.phuonghai.inspection.domain.model.ChatMessage
import com.phuonghai.inspection.domain.model.ChatThread
import com.phuonghai.inspection.domain.repository.IChatMessageRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class ChatMessageRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase
) : IChatMessageRepository {

    override suspend fun sendMessage(
        supervisorId: String,
        inspectorId: String,
        message: ChatMessage
    ) {
        val ref = database.getReference("chatbox/$supervisorId/$inspectorId/messages")
        val newMessageRef = ref.push()
        newMessageRef.setValue(message.copy(id = newMessageRef.key ?: ""))
    }

    override fun getMessages(supervisorId: String, inspectorId: String): Flow<List<ChatMessage>> =
        callbackFlow {
            val ref = database.getReference("chatbox/$supervisorId/$inspectorId/messages")
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages = snapshot.children.mapNotNull { it.getValue(ChatMessage::class.java) }
                    trySend(messages)
                }

                override fun onCancelled(error: DatabaseError) {}
            }
            ref.addValueEventListener(listener)
            awaitClose { ref.removeEventListener(listener) }
        }

    override suspend fun loadChatHistory(supervisorId: String): Flow<List<ChatThread>> = callbackFlow {
        val ref = database.getReference("chatbox/$supervisorId")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val threads = snapshot.children.map { inspectorSnapshot ->
                    val inspectorId = inspectorSnapshot.key ?: ""
                    // get last message inside "messages"
                    val latestMsg = inspectorSnapshot
                        .child("messages")
                        .children
                        .maxByOrNull { it.child("timestamp").getValue(Long::class.java) ?: 0 }
                        ?.getValue(ChatMessage::class.java)

                    ChatThread(inspectorId = inspectorId, latestMessage = latestMsg)
                }
                trySend(threads)
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
}
