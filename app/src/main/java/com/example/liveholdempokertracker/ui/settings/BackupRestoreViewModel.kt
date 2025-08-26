package com.example.liveholdempokertracker.ui.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.liveholdempokertracker.data.AppDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    private val app: Application,
    private val db: AppDatabase
) : ViewModel() {

    private val _backupState = MutableStateFlow<BackupRestoreState>(BackupRestoreState.Idle)
    val backupState: StateFlow<BackupRestoreState> = _backupState

    private val _restoreState = MutableStateFlow<BackupRestoreState>(BackupRestoreState.Idle)
    val restoreState: StateFlow<BackupRestoreState> = _restoreState

    private val dbName = "live_holdem_poker_tracker.db"

    fun backupDatabase(uri: Uri) {
        viewModelScope.launch {
            _backupState.value = BackupRestoreState.InProgress
            try {
                withContext(Dispatchers.IO) {
                    db.close()
                    val dbFile = app.getDatabasePath(dbName)
                    if (!dbFile.exists() || dbFile.length() == 0L) {
                        throw Exception("Database file is empty or does not exist.")
                    }
                    app.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        FileInputStream(dbFile).use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    } ?: throw Exception("Failed to open output stream.")
                }
                _backupState.value = BackupRestoreState.Success("Backup successful! Please restart the app to apply changes.")
            } catch (e: Exception) {
                _backupState.value = BackupRestoreState.Error("Backup failed: ${e.message}")
            }
        }
    }

    fun restoreDatabase(uri: Uri) {
        viewModelScope.launch {
            _restoreState.value = BackupRestoreState.InProgress
            try {
                withContext(Dispatchers.IO) {
                    db.close()
                    val dbFile = app.getDatabasePath(dbName)
                    app.contentResolver.openInputStream(uri)?.use { inputStream ->
                        if (inputStream.available() == 0) {
                            throw Exception("Selected file is empty.")
                        }
                        FileOutputStream(dbFile).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    } ?: throw Exception("Failed to open input stream.")
                }
                _restoreState.value = BackupRestoreState.Success("Restore successful! Please restart the app to load the restored data.")
            } catch (e: Exception) {
                _restoreState.value = BackupRestoreState.Error("Restore failed: ${e.message}")
            }
        }
    }

    fun resetState() {
        _backupState.value = BackupRestoreState.Idle
        _restoreState.value = BackupRestoreState.Idle
    }
}

sealed class BackupRestoreState {
    object Idle : BackupRestoreState()
    object InProgress : BackupRestoreState()
    data class Success(val message: String) : BackupRestoreState()
    data class Error(val message: String) : BackupRestoreState()
}