package com.example.liveholdempokertracker.ui.profile

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.liveholdempokertracker.data.*
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
    private val settingsRepository: SettingsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

    private val _allProfiles = playerProfileDao.getAllProfilesWithTags()

    val profiles: StateFlow<List<ProfileWithTags>> = searchText
        .combine(_allProfiles) { text, profiles ->
            if (text.isBlank()) {
                profiles
            } else {
                profiles.filter {
                    it.profile.name.contains(text, ignoreCase = true)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val recentProfiles: StateFlow<List<ProfileWithTags>> = settingsRepository.lastSessionSetupFlow
        .flatMapLatest { setup ->
            if (setup == null) {
                flowOf(emptyList())
            } else {
                playerProfileDao.getProfilesByNames(setup.assignments.values.toList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _profileId = savedStateHandle.getStateFlow("profileId", 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedProfileWithTags: StateFlow<ProfileWithTags?> = _profileId
        .flatMapLatest { id ->
            if (id > 0) playerProfileDao.getProfileWithTags(id) else flowOf(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val allTags: StateFlow<List<Tag>> = playerProfileDao.getAllTags()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addProfile(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            if (playerProfileDao.getProfileByName(name) == null) {
                playerProfileDao.insertOrUpdateProfile(PlayerProfile(name = name))
            }
        }
    }

    fun deleteProfile(profile: PlayerProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            playerProfileDao.deleteProfile(profile)
        }
    }

    fun updateMemo(memo: String) {
        val currentProfile = selectedProfileWithTags.value?.profile ?: return
        viewModelScope.launch(Dispatchers.IO) {
            playerProfileDao.insertOrUpdateProfile(currentProfile.copy(memo = memo))
        }
    }

    fun addTagToCurrentProfile(tag: Tag) {
        val profileId = _profileId.value
        if (profileId == 0) return
        viewModelScope.launch(Dispatchers.IO) {
            playerProfileDao.addTagToProfile(PlayerProfileTagCrossRef(id = profileId, tagId = tag.tagId))
        }
    }

    fun removeTagFromCurrentProfile(tag: Tag) {
        val profileId = _profileId.value
        if (profileId == 0) return
        viewModelScope.launch(Dispatchers.IO) {
            playerProfileDao.removeTagFromProfile(PlayerProfileTagCrossRef(id = profileId, tagId = tag.tagId))
        }
    }

    fun createNewTag(tagName: String, color: Color) {
        viewModelScope.launch(Dispatchers.IO) {
            val newTag = Tag(tagName = tagName, tagColor = color.value.toLong())
            playerProfileDao.insertTag(newTag)
        }
    }

    fun deleteTag(tag: Tag) {
        viewModelScope.launch(Dispatchers.IO) {
            playerProfileDao.deleteTag(tag)
        }
    }

    fun mergeProfiles(sourceProfile: PlayerProfile, destinationProfile: PlayerProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            // Ensure we don't merge a profile with itself
            if (sourceProfile.id == destinationProfile.id) return@launch

            playerProfileDao.mergeProfiles(sourceProfile, destinationProfile)
        }
    }

    suspend fun getProfileByName(name: String): PlayerProfile? {
        return withContext(Dispatchers.IO) {
            playerProfileDao.getProfileByName(name)
        }
    }
}