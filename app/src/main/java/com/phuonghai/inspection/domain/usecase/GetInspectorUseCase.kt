package com.phuonghai.inspection.domain.usecase



import com.phuonghai.inspection.domain.model.User

import com.phuonghai.inspection.domain.repository.IUserRepository

import javax.inject.Inject



class GetInspectorsUseCase @Inject constructor(

    private val userRepository: IUserRepository

) {

    suspend operator fun invoke(): Result<List<User>> {

        return userRepository.getInspectors()

    }

}