package com.example.liveholdempokertracker.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.liveholdempokertracker.ui.navigation.Screen
import com.example.liveholdempokertracker.ui.session.SessionViewModel

@Composable
fun HomeScreen(navController: NavController, sessionViewModel: SessionViewModel) {
    val hasActiveSession by sessionViewModel.hasActiveSession.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { navController.navigate(Screen.NewSession.route) }) {
            Text("신규 세션 시작")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                sessionViewModel.loadActiveSession {
                    navController.navigate(Screen.CurrentSession.route)
                }
            },
            enabled = hasActiveSession
        ) {
            Text("진행중인 세션")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate(Screen.ProfileList.route) }) {
            Text("프로필 관리")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate(Screen.Settings.route) }) {
            Text("설정")
        }
    }
}
