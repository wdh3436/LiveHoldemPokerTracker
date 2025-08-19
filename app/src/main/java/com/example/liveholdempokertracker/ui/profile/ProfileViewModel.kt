package com.example.liveholdempokertracker.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.liveholdempokertracker.data.PlayerProfile
import com.example.liveholdempokertracker.data.PlayerProfileDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val playerProfileDao: PlayerProfileDao
) : ViewModel() {

    val profiles: StateFlow<List<PlayerProfile>> = playerProfileDao.getAllProfiles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addProfile(name: String) {
        if (name.isBlank()) return

        viewModelScope.launch {
            val existingProfile = withContext(Dispatchers.IO) {
                playerProfileDao.getProfileByName(name)
            }
            if (existingProfile == null) {
                withContext(Dispatchers.IO) {
                    playerProfileDao.insertOrUpdate(PlayerProfile(name = name))
                }
            }
        }
    }

    fun deleteProfile(profile: PlayerProfile) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                playerProfileDao.delete(profile)
            }
        }
    }
}

