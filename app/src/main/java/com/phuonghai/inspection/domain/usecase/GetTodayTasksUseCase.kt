package com.phuonghai.inspection.domain.usecase

import com.phuonghai.inspection.domain.model.Task
import com.phuonghai.inspection.domain.model.TaskStatus
import com.phuonghai.inspection.domain.repository.ITaskRepository
import java.util.*
import javax.inject.Inject

class GetTodayTasksUseCase @Inject constructor(
    private val taskRepository: ITaskRepository
) {
    suspend operator fun invoke(inspectorId: String): Result<List<Task>> {
        return try {
            val result = taskRepository.getTasksByInspectorId(inspectorId)
            result.map { allTasks ->
                val today = Calendar.getInstance()
                val todayStart = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val todayEnd = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }

                allTasks.filter { task ->
                    // Filter for today's tasks
                    task.dueDate?.let { dueDate ->
                        val taskDate = dueDate.toDate().time
                        taskDate >= todayStart.timeInMillis && taskDate <= todayEnd.timeInMillis
                    } ?: false
                }.filter { task ->
                    // Only show assigned or in-progress tasks
                    task.status == TaskStatus.ASSIGNED || task.status == TaskStatus.IN_PROGRESS
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}