package com.example.liveholdempokertracker.ui.profile

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.liveholdempokertracker.data.PlayerProfile

@Composable
fun MergeProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profiles by viewModel.profiles.collectAsState()
    var selectedDestination by remember { mutableStateOf<PlayerProfile?>(null) }
    var selectedSource by remember { mutableStateOf<PlayerProfile?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("프로필 합치기", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("1. 데이터를 남길 프로필을 선택하세요 (대상).", style = MaterialTheme.typography.bodyLarge)
        Text("2. 데이터를 합칠 프로필을 선택하세요 (소스).", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(profiles.map { it.profile }) { profile ->
                val isDestination = selectedDestination?.id == profile.id
                val isSource = selectedSource?.id == profile.id

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (selectedDestination == null || isDestination) {
                                selectedDestination = if (isDestination) null else profile
                                if (isSource) selectedSource = null // Deselect if same
                            } else if (selectedSource == null || isSource) {
                                if (!isDestination) {
                                    selectedSource = if (isSource) null else profile
                                }
                            }
                        }
                        .border(
                            width = 2.dp,
                            color = when {
                                isDestination -> Color.Blue
                                isSource -> Color.Red
                                else -> Color.Transparent
                            }
                        )
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(profile.name, style = MaterialTheme.typography.bodyLarge)
                        Text("핸드: ${profile.handsPlayed}, VPIP: ${profile.getVpip()}%, PFR: ${profile.getPfr()}%", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { showDialog = true },
            enabled = selectedDestination != null && selectedSource != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("프로필 합치기")
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("프로필 합치기 확인") },
            text = { Text("'${selectedSource?.name}' 프로필을 '${selectedDestination?.name}' 프로필에 합치시겠습니까? 합친 후 기존 데이터는 복구할 수 없습니다.") },
            confirmButton = {
                Button(
                    onClick = {
                        selectedSource?.let { source ->
                            selectedDestination?.let { dest ->
                                viewModel.mergeProfiles(source, dest)
                            }
                        }
                        showDialog = false
                        navController.popBackStack()
                    }
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}
