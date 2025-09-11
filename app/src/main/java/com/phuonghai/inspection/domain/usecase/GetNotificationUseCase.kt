package com.phuonghai.inspection.domain.usecase

import com.phuonghai.inspection.domain.repository.INotificationRepository
import javax.inject.Inject

class GetNotificationUseCase @Inject constructor(
    private val notificationRepository: INotificationRepository
) {
    suspend operator fun invoke(receiverId: String) = notificationRepository.getNotifications(receiverId)

}