package com.example.liveholdempokertracker.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AppSettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val selectedColorValue by viewModel.tableColor.collectAsState()

    val colors = listOf(
        Color(0xFF2E7D32), // Green
        Color(0xFF1565C0), // Blue
        Color(0xFFC62828), // Red
        Color(0xFF555555)  // Dark Grey
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("앱 설정", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        Text("테이블 색상", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            colors.forEach { color ->
                val isSelected = color.value.toLong() == selectedColorValue
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(color)
                        .clickable { viewModel.updateTableColor(color.value.toLong()) }
                        .border(
                            width = 3.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}
