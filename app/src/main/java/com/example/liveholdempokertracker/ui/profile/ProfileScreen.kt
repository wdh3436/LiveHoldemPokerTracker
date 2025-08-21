package com.example.liveholdempokertracker.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.selectedProfile.collectAsState()
    var memoText by remember(profile.memo) { mutableStateOf(profile.memo) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(profile.name, style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(24.dp))

        StatsCard(profile = profile)

        Spacer(modifier = Modifier.height(24.dp))

        Text("메모", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = memoText,
            onValueChange = { memoText = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            label = { Text("플레이어에 대한 정보를 입력하세요") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.updateMemo(memoText) },
            modifier = Modifier.align(Alignment.End),
            enabled = memoText != profile.memo
        ) {
            Text("메모 저장")
        }
    }
}

@Composable
fun StatsCard(profile: com.example.liveholdempokertracker.data.PlayerProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatRow("VPIP", "${profile.getVpip()}%")
            StatRow("PFR", "${profile.getPfr()}%")
            StatRow("총 핸드 수", "${profile.handsPlayed}")
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(text = value, style = MaterialTheme.typography.headlineSmall)
    }
}
