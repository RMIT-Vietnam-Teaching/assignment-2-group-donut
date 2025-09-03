package com.phuonghai.inspection.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.phuonghai.inspection.domain.model.Branch
import com.phuonghai.inspection.domain.repository.IBranchRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class BranchRepositoryImpl @Inject constructor(
    private val firestore : FirebaseFirestore
): IBranchRepository {
    companion object{
        private const val BRANCHES_COLLECTION = "branchs"
        private const val TAG = "BranchRepository"
    }
    override suspend fun getBranches(): Result<List<Branch>> {
        return try {
            val snapshot = firestore.collection(BRANCHES_COLLECTION)
                .get()
                .await()
            val branches = snapshot.documents.mapNotNull { it.toObject(Branch::class.java) }
            Log.d(TAG, "Retrieved branches: $branches")
            Result.success(branches)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting branches", e)
            Result.failure(e)
        }
    }
}