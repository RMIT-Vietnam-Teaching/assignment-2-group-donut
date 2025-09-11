package com.phuonghai.inspection.domain.repository

import com.phuonghai.inspection.domain.model.Notification

interface INotificationRepository {
    suspend fun createNotification(notification: Notification): Result<Unit>
    suspend fun getNotifications(receiverId: String): Result<List<Notification>>
}