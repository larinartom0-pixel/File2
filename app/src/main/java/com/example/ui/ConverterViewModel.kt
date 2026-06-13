package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.MediaTask
import com.example.data.MediaTaskRepository
import com.example.media.MediaEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class ConverterViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = MediaTaskRepository(db.mediaTaskDao())
    val mediaEngine = MediaEngine(application, repository)

    // Collect all database tasks
    val allTasks: StateFlow<List<MediaTask>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val isProcessing: StateFlow<Boolean> = mediaEngine.isProcessing
    val consoleLogs: StateFlow<List<String>> = mediaEngine.consoleLogs

    // Output Zip placeholder for the user to export
    private val _createdZipResult = MutableStateFlow<File?>(null)
    val createdZipResult: StateFlow<File?> = _createdZipResult.asStateFlow()

    private val _zipProgress = MutableStateFlow<Boolean>(false)
    val zipProgress: StateFlow<Boolean> = _zipProgress.asStateFlow()

    // Skin Selector State: Gingerbread vs Holo Dark Retro style
    private val _currentSkin = MutableStateFlow("Gingerbread") // GINGERBREAD, HOLO_DARK
    val currentSkin: StateFlow<String> = _currentSkin.asStateFlow()

    // Active Concurrency setting
    private val _concurrencyLimit = MutableStateFlow(mediaEngine.getConcurrency())
    val concurrencyLimit: StateFlow<Int> = _concurrencyLimit.asStateFlow()

    fun selectSkin(skinName: String) {
        _currentSkin.value = skinName
        mediaEngine.addLog("Theme skin switched to: ${skinName.uppercase()}")
    }

    fun setConcurrency(limit: Int) {
        mediaEngine.setConcurrency(limit)
        _concurrencyLimit.value = limit
    }

    fun addSingleFile(uri: Uri, targetExt: String) {
        mediaEngine.queueSingleFile(uri, targetExt)
    }

    fun processZipArchive(uri: Uri, targetExt: String) {
        viewModelScope.launch {
            _zipProgress.value = true
            _createdZipResult.value = null
            mediaEngine.processZipFile(uri, targetExt) { compiledZip ->
                _createdZipResult.value = compiledZip
                _zipProgress.value = false
            }
        }
    }

    fun deleteTask(task: MediaTask) {
        viewModelScope.launch {
            repository.deleteTask(task)
            mediaEngine.addLog("Deleted task entry: ${task.fileName}")
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _createdZipResult.value = null
            mediaEngine.addLog("History log database cleared.")
        }
    }

    fun dismissZipResult() {
        _createdZipResult.value = null
    }
}
