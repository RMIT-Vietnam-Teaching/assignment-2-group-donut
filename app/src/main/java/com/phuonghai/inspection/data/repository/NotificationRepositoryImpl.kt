package com.phuonghai.inspection.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.phuonghai.inspection.domain.model.Notification
import com.phuonghai.inspection.domain.repository.INotificationRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : INotificationRepository {
    companion object {
        private const val TAG = "NotificationRepository"
        private const val NOTIFICATIONS_COLLECTION = "notifications"
    }
    override suspend fun createNotification(notification: Notification): Result<Unit> {
        try{
            firestore.collection(NOTIFICATIONS_COLLECTION)
                .document(notification.id)
                .set(notification)
                .await()
        }
        catch (e: Exception){
            return Result.failure(e)
        }
        Log.d(TAG, "Notification created: $notification")
        return Result.success(Unit)
    }

    override suspend fun getNotifications(receiverId: String): Result<List<Notification>> {
        return try {
            val snapshot = firestore.collection("notifications")
                .whereEqualTo("receiverId", receiverId)
                .get()
                .await()

            val notifications = snapshot.toObjects(Notification::class.java)
            Log.d(TAG, "Notifications retrieved: $notifications")
            Result.success(notifications)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}