package com.phuonghai.inspection.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.phuonghai.inspection.domain.model.User
import com.phuonghai.inspection.domain.repository.IUserRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
): IUserRepository {
    companion object {
        private const val USERS_COLLECTION = "users"
        private const val TAG = "UserRepository"
    }

    override suspend fun getInspectors(): Result<List<User>> {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .whereEqualTo("role", "INSPECTOR")
                .get()
                .await()
            val inspectors = snapshot.documents.mapNotNull { it.toObject(User::class.java) }
            Log.d(TAG, "Retrieved inspectors: $inspectors")
            Result.success(inspectors)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting inspectors", e)
            Result.failure(e)
        }
    }

    override suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .get()
                .await()
            val users = snapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(User::class.java)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to parse user document: ${document.id}", e)
                    null
                }
            }
            Log.d(TAG, "Retrieved ${users.size} users")
            Result.success(users)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all users", e)
            Result.failure(e)
        }
    }

    override suspend fun getUserById(userId: String): Result<User?> {
        return try {
            val document = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()

            val user = if (document.exists()) {
                document.toObject(User::class.java)
            } else {
                null
            }

            Log.d(TAG, "Retrieved user by ID: $userId")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user by ID: $userId", e)
            Result.failure(e)
        }
    }
}