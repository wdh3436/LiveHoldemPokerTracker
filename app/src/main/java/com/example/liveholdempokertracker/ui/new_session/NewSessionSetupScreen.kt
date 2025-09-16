package com.example.liveholdempokertracker.ui.new_session

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.liveholdempokertracker.ui.profile.ProfileViewModel
import com.example.liveholdempokertracker.ui.session.SessionViewModel

@Composable
fun NewSessionSetupScreen(navController: NavController, viewModel: SessionViewModel) {
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val profiles by profileViewModel.profiles.collectAsState()

    var seatCount by viewModel.seatCount
    val seatAssignments = viewModel.seatAssignments
    val seatCountInt = seatCount.toIntOrNull() ?: 0

    var showProfileDialog by remember { mutableStateOf(false) }
    var selectedSeatIndex by remember { mutableStateOf(-1) }

    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text("프로필 선택") },
            text = {
                LazyColumn {
                    item {
                        Text(
                            text = "좌석 비우기",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.removePlayer(selectedSeatIndex)
                                    showProfileDialog = false
                                }
                                .padding(16.dp)
                        )
                    }
                    item {
                        Text(
                            text = "GUEST",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.assignProfile(selectedSeatIndex, com.example.liveholdempokertracker.data.PlayerProfile(name = "GUEST"))
                                    showProfileDialog = false
                                }
                                .padding(16.dp)
                        )
                    }
                    items(profiles) { profileWithTags ->
                        Text(
                            text = profileWithTags.profile.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.assignProfile(selectedSeatIndex, profileWithTags.profile)
                                    showProfileDialog = false
                                }
                                .padding(16.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = seatCount,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() }) {
                    seatCount = newValue
                }
            },
            label = { Text("좌석 수 입력 (최대 10)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (seatCountInt > 0) {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(seatCountInt) { index ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("좌석 ${index + 1}")
                        Button(onClick = {
                            selectedSeatIndex = index
                            showProfileDialog = true
                        }) {
                            Text(seatAssignments[index]?.name ?: "프로필 할당")
                        }
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }


        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                viewModel.startFirstGame()
                navController.navigate("current_session")
            },
            enabled = seatCountInt > 0 && seatAssignments.size >= 2
        ) {
            Text("세션 시작")
        }
    }
}