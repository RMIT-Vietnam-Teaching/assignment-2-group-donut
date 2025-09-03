package com.phuonghai.inspection.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.phuonghai.inspection.domain.model.Task
import com.phuonghai.inspection.domain.repository.ITaskRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
): ITaskRepository {
    companion object {
        private const val TAG = "TaskRepository"
        private const val TASKS_COLLECTION = "tasks"
    }
    override suspend fun createTask(task: Task): Result<Unit> {
        return try{
            firestore.collection(TASKS_COLLECTION)
                .document(task.taskId)
                .set(task)
                .await()
            Log.d(TAG, "Task created successfully: ${task.taskId}")
            Result.success(Unit)
        }catch(e: Exception){
            Log.e(TAG, "Error creating task", e)
            Result.failure(e)
        }
    }

    override suspend fun getTask(taskId: String): Result<Task> {
        TODO("Not yet implemented")
    }
}