package com.example.liveholdempokertracker.ui.navigation

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.liveholdempokertracker.ui.home.HomeScreen
import com.example.liveholdempokertracker.ui.new_session.NewSessionScreen
import com.example.liveholdempokertracker.ui.new_session.NewSessionSetupScreen
import com.example.liveholdempokertracker.ui.profile.ProfileScreen
import com.example.liveholdempokertracker.ui.profile.ProfileListScreen
import com.example.liveholdempokertracker.ui.profile.MergeProfileScreen
import com.example.liveholdempokertracker.ui.settings.SettingsScreen
import com.example.liveholdempokertracker.ui.settings.AppSettingsScreen
import com.example.liveholdempokertracker.ui.settings.BackupRestoreScreen
import com.example.liveholdempokertracker.ui.session.CurrentSessionScreen
import com.example.liveholdempokertracker.ui.session.SessionViewModel
import com.example.liveholdempokertracker.ui.navigation.Screen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val sessionViewModel: SessionViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.NewSession.route) {
            NewSessionScreen(navController = navController)
        }
        composable(Screen.NewSessionSetup.route) {
            NewSessionSetupScreen(
                navController = navController,
                viewModel = sessionViewModel
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        composable(Screen.ProfileList.route) {
            ProfileListScreen()
        }
        composable(Screen.MergeProfile.route) {
            MergeProfileScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        composable(Screen.AppSettings.route) {
            AppSettingsScreen()
        }
        composable(Screen.BackupRestore.route) {
            BackupRestoreScreen()
        }
        composable(Screen.CurrentSession.route) {
            CurrentSessionScreen(viewModel = sessionViewModel)
        }
    }
}
