package com.donut.assignment2.domain.usecase.inspector

import com.donut.assignment2.domain.repository.UserRepository
import javax.inject.Inject

class GetUserByPhoneUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(phoneNumber: String) = userRepository.getUserByPhone(phoneNumber)
}