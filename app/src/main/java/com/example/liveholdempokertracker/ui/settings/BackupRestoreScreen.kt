package com.example.liveholdempokertracker.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    navController: NavController,
    viewModel: BackupRestoreViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val backupState by viewModel.backupState.collectAsState()
    val restoreState by viewModel.restoreState.collectAsState()
    var showRestoreDialog by remember { mutableStateOf(false) }

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.sqlite3"),
        onResult = { uri: Uri? ->
            uri?.let { viewModel.backupDatabase(it) }
        }
    )

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let { viewModel.restoreDatabase(it) }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.backupState.collectLatest { state ->
            when (state) {
                is BackupRestoreState.Success -> {
                    snackbarHostState.showSnackbar(state.message)
                    viewModel.resetState()
                }
                is BackupRestoreState.Error -> {
                    snackbarHostState.showSnackbar(state.message)
                    viewModel.resetState()
                }
                else -> {}
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.restoreState.collectLatest { state ->
            when (state) {
                is BackupRestoreState.Success -> {
                    snackbarHostState.showSnackbar(state.message)
                    viewModel.resetState()
                }
                is BackupRestoreState.Error -> {
                    snackbarHostState.showSnackbar(state.message)
                    viewModel.resetState()
                }
                else -> {}
            }
        }
    }

    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Restore Confirmation") },
            text = { Text("Restoring data will overwrite all current data. This action cannot be undone. Are you sure you want to continue?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestoreDialog = false
                        restoreLauncher.launch(arrayOf("application/vnd.sqlite3", "application/x-sqlite3", "application/octet-stream"))
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("Backup & Restore") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (backupState is BackupRestoreState.InProgress || restoreState is BackupRestoreState.InProgress) {
                CircularProgressIndicator()
            } else {
                Button(onClick = { backupLauncher.launch("live_holdem_poker_tracker_backup.db") }) {
                    Text("Backup Data")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { showRestoreDialog = true }) {
                    Text("Restore Data")
                }
            }
        }
    }
}