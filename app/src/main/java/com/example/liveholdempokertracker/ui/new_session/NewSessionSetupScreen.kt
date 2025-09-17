package com.example.liveholdempokertracker.ui.new_session

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.liveholdempokertracker.data.PlayerProfile
import com.example.liveholdempokertracker.ui.navigation.Screen
import com.example.liveholdempokertracker.ui.profile.ProfileViewModel
import com.example.liveholdempokertracker.ui.session.SessionViewModel
import kotlinx.coroutines.launch

@Composable
fun NewSessionSetupScreen(navController: NavController, viewModel: SessionViewModel) {
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()

    var seatCount by viewModel.seatCount
    val seatAssignments = viewModel.seatAssignments
    val seatCountInt = seatCount.toIntOrNull() ?: 0

    var selectedSeatIndex by viewModel.selectedSeatIndex

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val selectedProfileName by savedStateHandle?.getStateFlow<String?>("selectedProfileName", null)?.collectAsState() ?: remember { mutableStateOf(null) }

    LaunchedEffect(selectedProfileName) {
        println("Debug: LaunchedEffect triggered. Profile name: $selectedProfileName")
        selectedProfileName?.let { profileName ->
            val seatIndex = viewModel.selectedSeatIndex.value
            println("Debug: Processing profile '$profileName' for seat index $seatIndex")
            if (seatIndex != -1) {
                when (profileName) {
                    "GUEST" -> {
                        println("Debug: Assigning GUEST to seat $seatIndex")
                        viewModel.assignProfile(seatIndex, PlayerProfile(name = "GUEST"))
                    }
                    "EMPTY" -> {
                        println("Debug: Removing player from seat $seatIndex")
                        viewModel.removePlayer(seatIndex)
                    }
                    else -> {
                        coroutineScope.launch {
                            val profile = profileViewModel.getProfileByName(profileName)
                            println("Debug: Fetched profile: ${profile?.name}")
                            if (profile != null) {
                                println("Debug: Assigning ${profile.name} to seat $seatIndex")
                                viewModel.assignProfile(seatIndex, profile)
                            }
                        }
                    }
                }
                // Reset state after processing
                println("Debug: Resetting state.")
                savedStateHandle?.set("selectedProfileName", null)
                viewModel.selectedSeatIndex.value = -1
            }
        }
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
                            navController.navigate(Screen.ProfileList.createRoute(seatNumber = index))
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
                navController.navigate(Screen.CurrentSession.route)
            },
            enabled = seatCountInt > 0 && seatAssignments.size >= 2
        ) {
            Text("세션 시작")
        }
    }
}
