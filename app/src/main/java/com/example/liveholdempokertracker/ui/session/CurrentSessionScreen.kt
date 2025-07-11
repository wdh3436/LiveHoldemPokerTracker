package com.example.liveholdempokertracker.ui.session

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.liveholdempokertracker.ui.session.SessionViewModel

@Composable
fun CurrentSessionScreen(viewModel: SessionViewModel) {
    val seatCount by viewModel.seatCount
    val selectedColor by viewModel.selectedColor
    val seatAssignments = viewModel.seatAssignments
    val seatCountInt = seatCount.toIntOrNull() ?: 0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(selectedColor.copy(alpha = 0.3f))
            .padding(16.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("현재 세션 정보", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text("테이블 색상: ${selectedColor.value}")
            Spacer(modifier = Modifier.height(8.dp))
            Text("좌석 수: $seatCountInt")
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(seatCountInt) { index ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("좌석 ${index + 1}")
                        Text(seatAssignments.getOrDefault(index, "(미지정)"))
                    }
                }
            }
        }
    }
}