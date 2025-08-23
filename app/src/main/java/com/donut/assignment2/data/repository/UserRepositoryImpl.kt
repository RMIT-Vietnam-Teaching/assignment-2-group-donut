package com.donut.assignment2.data.repository

import com.donut.assignment2.data.local.dao.*
import com.donut.assignment2.data.mapper.*
import com.donut.assignment2.domain.model.*
import com.donut.assignment2.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userMapper: UserMapper
) : UserRepository {

    override suspend fun getUserById(id: String): Result<User?> {
        return try {
            val userEntity = userDao.getUserById(id)
            val user = userEntity?.let { userMapper.fromEntity(it) }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserByUsername(username: String): Result<User?> {
        return try {
            val userEntity = userDao.getUserByUsername(username)
            val user = userEntity?.let { userMapper.fromEntity(it) }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createUser(user: User): Result<String> {
        return try {
            val userEntity = userMapper.toEntity(user, "defaultPassword") // Demo only
            userDao.insertUser(userEntity)
            Result.success(user.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        return try {
            val userEntity = userMapper.toEntity(user)
            userDao.updateUser(userEntity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun authenticateUser(username: String, password: String): Result<User?> {
        return try {
            val userEntity = userDao.authenticateUser(username, password)
            val user = userEntity?.let { userMapper.fromEntity(it) }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUsersByRole(role: UserRole): Result<List<User>> {
        return try {
            val userEntities = userDao.getUsersByRole(role.name)
            val users = userEntities.map { userMapper.fromEntity(it) }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}