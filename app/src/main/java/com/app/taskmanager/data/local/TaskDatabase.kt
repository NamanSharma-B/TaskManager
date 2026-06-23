package com.app.taskmanager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.app.taskmanager.data.local.dao.TaskDao
import com.app.taskmanager.data.local.entity.Task

@Database(
    entities = [Task::class],
    version = 1,
    exportSchema = false
)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        const val DATABASE_NAME = "task_manager_db"
    }
}
