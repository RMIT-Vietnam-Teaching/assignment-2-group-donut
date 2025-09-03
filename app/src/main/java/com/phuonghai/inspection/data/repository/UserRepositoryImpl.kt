package com.phuonghai.inspection.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.phuonghai.inspection.domain.model.User
import com.phuonghai.inspection.domain.repository.IAuthRepository
import com.phuonghai.inspection.domain.repository.IUserRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestore : FirebaseFirestore,
    private val fireAuth: FirebaseAuth
): IUserRepository {
    companion object{
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

    override suspend fun getUserById(): Result<User?> {
        return try {
            val userId = fireAuth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
            val document = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()
            val user = document.toObject(User::class.java)
            Log.d(TAG, "Retrieved user: $user")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user by ID", e)
            Result.failure(e)
        }
    }
}