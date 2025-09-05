package com.example.liveholdempokertracker.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.liveholdempokertracker.R

@Composable
fun AppSettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val selectedColorValue by viewModel.tableColor.collectAsState()
    val selectedCardBackColorValue by viewModel.cardBackColor.collectAsState()

    val tableColorValues = listOf(
        0xFF2E7D32, // Green
        0xFF1565C0, // Blue
        0xFFC62828, // Red
        0xFF555555  // Dark Grey
    )

    val cardBackColorValues = listOf(
        0xFF1565C0, // Blue
        0xFFC62828, // Red
        0xFF212121  // Black
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
            tableColorValues.forEach { colorValue ->
                val isSelected = colorValue == selectedColorValue
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(colorValue))
                        .clickable { viewModel.updateTableColor(colorValue) }
                        .border(
                            width = 3.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                            shape = CircleShape
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("카드 뒷면 색상", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            cardBackColorValues.forEach { colorValue ->
                val isSelected = colorValue == selectedCardBackColorValue
                Box(
                    modifier = Modifier
                        .size(width = 60.dp, height = 90.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(colorValue))
                        .clickable { viewModel.updateCardBackColor(colorValue) }
                        .border(
                            width = 3.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }
        }
    }
}
