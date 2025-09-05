package com.example.liveholdempokertracker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.liveholdempokertracker.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val tableColor: StateFlow<Long?> = settingsRepository.tableColorFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val cardBackColor: StateFlow<Long?> = settingsRepository.cardBackColorFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun updateTableColor(color: Long) {
        viewModelScope.launch {
            settingsRepository.updateTableColor(color)
        }
    }

    fun updateCardBackColor(color: Long) {
        viewModelScope.launch {
            settingsRepository.updateCardBackColor(color)
        }
    }
}