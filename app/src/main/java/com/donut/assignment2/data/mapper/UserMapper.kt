package com.donut.assignment2.data.mapper

import com.donut.assignment2.data.local.entities.UserEntity
import com.donut.assignment2.domain.model.User
import com.donut.assignment2.domain.model.UserRole
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserMapper @Inject constructor() {

    fun toEntity(user: User, password: String = ""): UserEntity {
        return UserEntity(
            id = user.id,
            username = user.username,
            email = user.email,
            fullName = user.fullName,
            role = user.role.name,
            isActive = user.isActive,
            createdAt = user.createdAt,
            password = password // Simple password storage for demo
        )
    }

    fun fromEntity(entity: UserEntity): User {
        return User(
            id = entity.id,
            username = entity.username,
            email = entity.email,
            fullName = entity.fullName,
            role = UserRole.valueOf(entity.role),
            isActive = entity.isActive,
            createdAt = entity.createdAt
        )
    }
}