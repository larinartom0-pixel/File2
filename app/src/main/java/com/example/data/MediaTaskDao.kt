package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaTaskDao {
    @Query("SELECT * FROM media_tasks ORDER BY timestamp DESC")
    fun getAllTasks(): Flow<List<MediaTask>>

    @Query("SELECT * FROM media_tasks WHERE id = :id")
    suspend fun getTaskById(id: Int): MediaTask?

    @Query("SELECT * FROM media_tasks WHERE status = 'QUEUED' ORDER BY timestamp ASC")
    suspend fun getPendingTasks(): List<MediaTask>

    @Query("SELECT * FROM media_tasks WHERE status = 'CONVERTING'")
    suspend fun getActiveTasks(): List<MediaTask>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: MediaTask): Long

    @Update
    suspend fun update(task: MediaTask)

    @Delete
    suspend fun delete(task: MediaTask)

    @Query("DELETE FROM media_tasks")
    suspend fun clearAll()
}
