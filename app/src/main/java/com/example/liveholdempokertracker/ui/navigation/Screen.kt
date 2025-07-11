package com.example.liveholdempokertracker.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object NewSession : Screen("new_session")
    object NewSessionSetup : Screen("new_session_setup")
    object Profile : Screen("profile")
    object ProfileList : Screen("profile_list")
    object MergeProfile : Screen("merge_profile")
    object Settings : Screen("settings")
    object AppSettings : Screen("app_settings")
    object BackupRestore : Screen("backup_restore")
    object CurrentSession : Screen("current_session")
}