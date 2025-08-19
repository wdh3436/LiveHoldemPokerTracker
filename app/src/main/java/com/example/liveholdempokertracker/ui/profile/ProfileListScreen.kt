package com.example.liveholdempokertracker.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ProfileListScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val profiles by viewModel.profiles.collectAsState()
    var newProfileName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("프로필 관리", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Add new profile UI
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newProfileName,
                onValueChange = { newProfileName = it },
                label = { Text("새 프로필 이름") },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    viewModel.addProfile(newProfileName)
                    newProfileName = "" // Clear input field
                },
                enabled = newProfileName.isNotBlank()
            ) {
                Text("추가")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // List of profiles
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(profiles) { profile ->
                ProfileListItem(profile = profile, onDelete = { viewModel.deleteProfile(profile) })
                Divider()
            }
        }
    }
}

@Composable
fun ProfileListItem(profile: com.example.liveholdempokertracker.data.PlayerProfile, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = profile.name, style = MaterialTheme.typography.bodyLarge)
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Profile")
        }
    }
}
