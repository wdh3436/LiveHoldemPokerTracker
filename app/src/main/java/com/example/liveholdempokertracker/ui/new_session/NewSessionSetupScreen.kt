package com.example.liveholdempokertracker.ui.new_session

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.liveholdempokertracker.ui.session.SessionViewModel

@Composable
fun NewSessionSetupScreen(navController: NavController, viewModel: SessionViewModel) {
    var seatCount by viewModel.seatCount
    var selectedColor by viewModel.selectedColor
    val seatAssignments = viewModel.seatAssignments
    val seatCountInt = seatCount.toIntOrNull() ?: 0

    var showProfileDialog by remember { mutableStateOf(false) }
    var selectedSeatIndex by remember { mutableStateOf(-1) }

    val profiles = listOf("GUEST", "Hero", "Villain 1", "Villain 2") // 임시 프로필 목록

    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text("프로필 선택") },
            text = {
                LazyColumn {
                    items(profiles) { profile ->
                        Text(
                            text = profile,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    viewModel.assignProfile(selectedSeatIndex, profile)
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

        Text("테이블 컬러 선택")
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(viewModel.colors) { color ->
                val isSelected = color == selectedColor
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = 2.dp,
                            color = if (isSelected) Color.Black else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { selectedColor = color }
                )
            }
        }

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
                            Text(seatAssignments.getOrDefault(index, "프로필 할당"))
                        }
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }


        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("current_session") }, enabled = seatCountInt > 0) {
            Text("세션 시작")
        }
    }
}