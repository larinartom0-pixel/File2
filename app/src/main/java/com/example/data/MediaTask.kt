package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_tasks")
data class MediaTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fileName: String,
    val fileSize: Long,
    val originalExtension: String,
    val targetExtension: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String, // QUEUED, CONVERTING, COMPLETED, FAILED
    val progress: Float = 0f,
    val outputFileName: String = "",
    val outputSize: Long = 0,
    val error: String? = null,
    val isArchiveTask: Boolean = false,
    val associatedZipName: String? = null
)
