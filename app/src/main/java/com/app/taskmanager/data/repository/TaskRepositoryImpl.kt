package com.app.taskmanager.data.repository

import com.app.taskmanager.data.local.dao.TaskDao
import com.app.taskmanager.data.local.entity.Priority
import com.app.taskmanager.data.local.entity.Task
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {

    override fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    override fun getActiveTasks(): Flow<List<Task>> = taskDao.getActiveTasks()

    override fun getCompletedTasks(): Flow<List<Task>> = taskDao.getCompletedTasks()

    override fun getTasksByPriority(priority: Priority): Flow<List<Task>> =
        taskDao.getTasksByPriority(priority)

    override fun searchTasks(query: String): Flow<List<Task>> = taskDao.searchTasks(query)

    override fun getActiveTaskCount(): Flow<Int> = taskDao.getActiveTaskCount()

    override suspend fun getTaskById(taskId: Int): Task? = taskDao.getTaskById(taskId)

    override suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)

    override suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    override suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    override suspend fun deleteAllCompletedTasks() = taskDao.deleteAllCompletedTasks()

    override suspend fun updateTaskCompletion(taskId: Int, isCompleted: Boolean) =
        taskDao.updateTaskCompletion(taskId, isCompleted)
}
