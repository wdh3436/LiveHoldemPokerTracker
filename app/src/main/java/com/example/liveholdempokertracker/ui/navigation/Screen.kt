package com.example.liveholdempokertracker.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object NewSession : Screen("new_session")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object AppSettings : Screen("app_settings")
    object BackupRestore : Screen("backup_restore")
}