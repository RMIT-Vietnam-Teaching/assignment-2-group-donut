package com.phuonghai.inspection.domain.usecase

import com.phuonghai.inspection.domain.model.Notification
import com.phuonghai.inspection.domain.repository.INotificationRepository
import javax.inject.Inject

class CreateNotificationUseCase @Inject constructor(
    private val notificationRepository: INotificationRepository
) {
    suspend operator fun invoke(notification: Notification): Result<Unit> {
        return notificationRepository.createNotification(notification)
    }

}