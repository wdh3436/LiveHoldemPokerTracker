package com.example.liveholdempokertracker.ui.session

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor() : ViewModel() {
    var seatCount = mutableStateOf("")
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Cyan, Color.Magenta)
    var selectedColor = mutableStateOf(colors.first())
    val seatAssignments = mutableStateMapOf<Int, String>()

    fun assignProfile(seatIndex: Int, profile: String) {
        seatAssignments[seatIndex] = profile
    }

    fun clearSetup() {
        seatCount.value = ""
        selectedColor.value = colors.first()
        seatAssignments.clear()
    }
}
