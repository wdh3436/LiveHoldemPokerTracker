package com.example.liveholdempokertracker.ui.session

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class Player(
    val name: String,
    val stack: Int = 0,
    val isDealer: Boolean = false,
    val lastAction: String = "", // 예: "폴드", "체크", "콜", "베팅", "레이즈"
    val holeCards: List<String> = emptyList() // 플레이어의 홀덤 패
)

@HiltViewModel
class SessionViewModel @Inject constructor() : ViewModel() {
    var seatCount = mutableStateOf("")
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Cyan, Color.Magenta)
    var selectedColor = mutableStateOf(colors.first())
    val seatAssignments = mutableStateMapOf<Int, Player>()
    val communityCards = mutableStateListOf<String>()
    var gamePhase = mutableStateOf("Pre-Flop")
    var activePlayerIndex = mutableStateOf(0)
    var lastRaiserIndex = mutableStateOf<Int?>(null) // 마지막으로 베팅/레이즈한 플레이어

    fun assignProfile(seatIndex: Int, profileName: String) {
        val tempHoleCards = listOf("A", "K")
        seatAssignments[seatIndex] = Player(name = profileName, holeCards = tempHoleCards)
    }

    fun clearSetup() {
        seatCount.value = ""
        selectedColor.value = colors.first()
        seatAssignments.clear()
        communityCards.clear()
        gamePhase.value = "Pre-Flop"
        activePlayerIndex.value = 0
    }

    fun nextPhase() {
        gamePhase.value = when (gamePhase.value) {
            "Pre-Flop" -> {
                addFlopCards()
                "Flop"
            }
            "Flop" -> {
                addTurnCard()
                "Turn"
            }
            "Turn" -> {
                addRiverCard()
                "River"
            }
            "River" -> "Showdown"
            else -> "Pre-Flop"
        }
    }

    fun addFlopCards() {
        communityCards.addAll(listOf("A♠", "K♥", "Q♣"))
    }

    fun addTurnCard() {
        communityCards.add("J♦")
    }

    fun addRiverCard() {
        communityCards.add("10♠")
    }

    fun startGame() {
        val seatCount = seatAssignments.size
        if (seatCount == 0) return

        // User request: Fix dealer to seat 1 (index 0)
        val dealerIndex = 0
        seatAssignments[dealerIndex] = seatAssignments[dealerIndex]!!.copy(isDealer = true)

        // Set SB and BB based on the dealer
        val sbIndex = (dealerIndex + 1) % seatCount
        val bbIndex = (dealerIndex + 2) % seatCount

        seatAssignments[sbIndex] = seatAssignments[sbIndex]!!.copy(lastAction = "SB")
        seatAssignments[bbIndex] = seatAssignments[bbIndex]!!.copy(lastAction = "BB")

        // The last raiser is the BB before the flop
        lastRaiserIndex.value = bbIndex

        // Pre-flop action starts to the left of the BB (UTG)
        val utgIndex = (bbIndex + 1) % seatCount
        activePlayerIndex.value = utgIndex
    }

    fun handleAction(playerIndex: Int, action: String) {
        val player = seatAssignments[playerIndex]
        if (player == null || player.lastAction == "폴드" || playerIndex != activePlayerIndex.value) return

        seatAssignments[playerIndex] = player.copy(lastAction = action)

        val isRaise = (action == "베팅" || action == "레이즈")
        if (isRaise) {
            lastRaiserIndex.value = playerIndex
        }

        // Check if the action has returned to the last raiser and they didn't raise again.
        val roundEnded = (playerIndex == lastRaiserIndex.value && !isRaise)

        if (roundEnded) {
            endBettingRound()
        } else {
            moveToNextPlayer(startFrom = playerIndex)
        }
    }

    private fun moveToNextPlayer(startFrom: Int) {
        val seatCount = seatAssignments.size
        if (seatCount == 0) return

        var nextIndex = startFrom
        do {
            nextIndex = (nextIndex + 1) % seatCount
        } while (seatAssignments[nextIndex]?.lastAction == "폴드")
        activePlayerIndex.value = nextIndex
    }

    private fun endBettingRound() {
        val activePlayers = seatAssignments.values.count { it.lastAction != "폴드" }
        if (activePlayers <= 1) {
            // End of game - handle winner
        } else {
            nextPhase()
            resetForNewRound()
        }
    }

    private fun resetForNewRound() {
        val dealerIndex = seatAssignments.entries.find { it.value.isDealer }?.key ?: 0
        lastRaiserIndex.value = null // Reset for the new round

        // Find the first active player to the left of the dealer
        var firstToAct = (dealerIndex + 1) % seatAssignments.size
        while(seatAssignments[firstToAct]?.lastAction == "폴드") {
            firstToAct = (firstToAct + 1) % seatAssignments.size
        }
        activePlayerIndex.value = firstToAct
        lastRaiserIndex.value = firstToAct // In post-flop, the first player to act is the initial "last raiser"
    }
}