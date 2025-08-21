package com.example.liveholdempokertracker.ui.profile

import androidx.compose.foundation.clickable
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
import androidx.navigation.NavController
import com.example.liveholdempokertracker.ui.navigation.Screen

@Composable
fun ProfileListScreen(navController: NavController, viewModel: ProfileViewModel = hiltViewModel()) {
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
                ProfileListItem(
                    profile = profile,
                    onDelete = { viewModel.deleteProfile(profile) },
                    onItemClick = { navController.navigate(Screen.Profile.createRoute(profile.id)) }
                )
                Divider()
            }
        }
    }
}

@Composable
fun ProfileListItem(
    profile: com.example.liveholdempokertracker.data.PlayerProfile,
    onDelete: () -> Unit,
    onItemClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = profile.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = "VPIP: ${profile.getVpip()}%", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "PFR: ${profile.getPfr()}%", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Profile")
        }
    }
}
