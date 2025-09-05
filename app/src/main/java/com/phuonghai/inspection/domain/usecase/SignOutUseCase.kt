package com.phuonghai.inspection.domain.usecase.auth

import com.phuonghai.inspection.domain.repository.IAuthRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val authRepository: IAuthRepository
) {
    suspend operator fun invoke() {
        authRepository.signOut()
    }
}