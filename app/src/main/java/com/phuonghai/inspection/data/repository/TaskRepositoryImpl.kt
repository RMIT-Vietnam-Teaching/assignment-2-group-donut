package com.phuonghai.inspection.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.phuonghai.inspection.domain.model.Task
import com.phuonghai.inspection.domain.model.TaskStatus
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
        return try {
            val document = firestore.collection(TASKS_COLLECTION)
                .document(taskId)
                .get()
                .await()

            val task = if (document.exists()) {
                document.toObject(Task::class.java)
            } else {
                null
            }

            if (task != null) {
                Log.d(TAG, "Retrieved task: $taskId")
                Result.success(task)
            } else {
                Log.w(TAG, "Task not found: $taskId")
                Result.failure(Exception("Task not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting task", e)
            Result.failure(e)
        }
    }

    override suspend fun getTasksByInspectorId(inspectorId: String): Result<List<Task>> {
        return try {
            val snapshot = firestore.collection(TASKS_COLLECTION)
                .whereEqualTo("inspectorId", inspectorId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val tasks = snapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(Task::class.java)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to parse task document: ${document.id}", e)
                    null
                }
            }

            Log.d(TAG, "Retrieved ${tasks.size} tasks for inspector: $inspectorId")
            Result.success(tasks)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting tasks for inspector: $inspectorId", e)
            Result.failure(e)
        }
    }

    override suspend fun updateTaskStatus(taskId: String, status: TaskStatus): Result<Unit> {
        return try {
            firestore.collection(TASKS_COLLECTION)
                .document(taskId)
                .update("status", status)
                .await()

            Log.d(TAG, "Task status updated: $taskId -> $status")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating task status", e)
            Result.failure(e)
        }
    }

    override suspend fun getTasksByBranch(branchId: String): Result<List<Task>> {
        return try {
            val snapshot = firestore.collection(TASKS_COLLECTION)
                .whereEqualTo("branchId", branchId)

                .get()
                .await()

            val tasks = snapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(Task::class.java)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to parse task document: ${document.id}", e)
                    null
                }
            }

            Log.d(TAG, "Retrieved ${tasks.size} tasks for branch: $branchId")
            Result.success(tasks)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting tasks for branch: $branchId", e)
            Result.failure(e)
        }
    }

    override suspend fun getTaskIdByReportId(reportId: String): Result<String> {
        return try {
            val snapshot = firestore.collection("reports")
                .whereEqualTo("reportId", reportId)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                // take the first matching document
                val taskId = snapshot.documents[0].getString("taskId")
                if (taskId != null) {
                    Result.success(taskId)
                } else {
                    Result.failure(Exception("taskId not found for reportId: $reportId"))
                }
            } else {
                Result.failure(Exception("No report found with reportId: $reportId"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}