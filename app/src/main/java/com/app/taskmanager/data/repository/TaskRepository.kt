package com.app.taskmanager.data.repository

import com.app.taskmanager.data.local.entity.Priority
import com.app.taskmanager.data.local.entity.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getAllTasks(): Flow<List<Task>>
    fun getActiveTasks(): Flow<List<Task>>
    fun getCompletedTasks(): Flow<List<Task>>
    fun getTasksByPriority(priority: Priority): Flow<List<Task>>
    fun searchTasks(query: String): Flow<List<Task>>
    fun getActiveTaskCount(): Flow<Int>
    suspend fun getTaskById(taskId: Int): Task?
    suspend fun insertTask(task: Task): Long
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
    suspend fun deleteAllCompletedTasks()
    suspend fun updateTaskCompletion(taskId: Int, isCompleted: Boolean)
}
