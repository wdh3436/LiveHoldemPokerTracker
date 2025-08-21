package com.example.liveholdempokertracker.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.liveholdempokertracker.data.PlayerProfile
import com.example.liveholdempokertracker.data.PlayerProfileDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val playerProfileDao: PlayerProfileDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val profiles: StateFlow<List<PlayerProfile>> = playerProfileDao.getAllProfiles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _profileId = savedStateHandle.getStateFlow("profileId", 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedProfile: StateFlow<PlayerProfile> = _profileId
        .flatMapLatest { id ->
            playerProfileDao.getProfileById(id) ?: flowOf(null)
        }
        .filterNotNull()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PlayerProfile(name = "Loading...")
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

    fun updateMemo(memo: String) {
        viewModelScope.launch {
            val currentProfile = selectedProfile.value
            val updatedProfile = currentProfile.copy(memo = memo)
            withContext(Dispatchers.IO) {
                playerProfileDao.insertOrUpdate(updatedProfile)
            }
        }
    }
}

