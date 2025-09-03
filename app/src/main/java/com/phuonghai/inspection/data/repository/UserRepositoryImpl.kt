package com.phuonghai.inspection.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.phuonghai.inspection.domain.model.User
import com.phuonghai.inspection.domain.repository.IUserRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestore : FirebaseFirestore
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
}