package com.donut.assignment2.domain.usecase.auth

import com.donut.assignment2.domain.model.User
import com.donut.assignment2.domain.repository.AuthRepository
import javax.inject.Inject

class VerifyOTPUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(verificationId: String, otp: String): Result<User> {
        return authRepository.verifyOTP(verificationId, otp)
    }
}