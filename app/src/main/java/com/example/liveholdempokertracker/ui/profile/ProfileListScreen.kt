package com.example.liveholdempokertracker.ui.profile

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp

@Composable
fun ProfileListScreen() {
    val profiles = listOf("프로필 1", "프로필 2", "프로필 3", "프로필 4", "프로필 5")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(text = "저장된 프로필 목록")
        }
        items(profiles) {
            profile -> Text(text = profile, modifier = Modifier.padding(vertical = 4.dp))
        }
    }
}
