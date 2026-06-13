package com.example.data

import kotlinx.coroutines.flow.Flow

class MediaTaskRepository(private val dao: MediaTaskDao) {
    val allTasks: Flow<List<MediaTask>> = dao.getAllTasks()

    suspend fun getTaskById(id: Int): MediaTask? = dao.getTaskById(id)

    suspend fun getPendingTasks(): List<MediaTask> = dao.getPendingTasks()

    suspend fun getActiveTasks(): List<MediaTask> = dao.getActiveTasks()

    suspend fun insertTask(task: MediaTask): Long = dao.insert(task)

    suspend fun updateTask(task: MediaTask) = dao.update(task)

    suspend fun deleteTask(task: MediaTask) = dao.delete(task)

    suspend fun clearHistory() = dao.clearAll()
}
