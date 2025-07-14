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
    val isSmallBlind: Boolean = false,
    val isBigBlind: Boolean = false,
    val lastAction: String = "", // 예: "폴드", "체크", "콜", "베팅", "레이즈"
    val holeCards: List<String> = emptyList() // 플레이어의 홀덤 패
)

@HiltViewModel
class SessionViewModel @Inject constructor() : ViewModel() {
    var seatCount = mutableStateOf("")
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Cyan, Color.Magenta)
    var selectedColor = mutableStateOf(colors.first())
    val seatAssignments = mutableStateMapOf<Int, Player>()
    val communityCards = mutableStateListOf<String>() // 커뮤니티 카드
    var gamePhase = mutableStateOf("Pre-Flop") // 초기 게임 단계
    var activePlayerIndex = mutableStateOf(0) // 현재 액션을 취할 플레이어의 인덱스
    val playerActionStatus = mutableStateMapOf<Int, Boolean>() // 각 플레이어의 현재 단계 액션 완료 여부
    var currentBet = mutableStateOf(0) // 현재 라운드의 최고 베팅 금액
    val playerContributions = mutableStateMapOf<Int, Int>() // 각 플레이어가 현재 라운드에 기여한 금액
    var lastBetterIndex = mutableStateOf<Int?>(null) // 마지막으로 베팅/레이즈를 한 플레이어의 인덱스

    fun assignProfile(seatIndex: Int, profileName: String) {
        // 임시로 홀덤 패 할당 (실제로는 카드 덱에서 가져와야 함)
        val tempHoleCards = listOf("A", "K") // 임시 카드
        seatAssignments[seatIndex] = Player(name = profileName, holeCards = tempHoleCards)
        playerActionStatus[seatIndex] = false // 초기화
        playerContributions[seatIndex] = 0 // 초기화
    }

    fun clearSetup() {
        seatCount.value = ""
        selectedColor.value = colors.first()
        seatAssignments.clear()
        communityCards.clear()
        gamePhase.value = "Pre-Flop"
        activePlayerIndex.value = 0
        playerActionStatus.clear()
        resetBettingRound()
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
        resetPlayerActionStatus()
        resetBettingRound()
    }

    fun moveToNextPlayer() {
        val currentSeatCount = seatCount.value.toIntOrNull() ?: 0
        if (currentSeatCount == 0) return

        var nextIndex = activePlayerIndex.value
        var playersChecked = 0

        do {
            nextIndex = (nextIndex + 1) % currentSeatCount
            playersChecked++
            val player = seatAssignments[nextIndex]
            // 폴드하지 않았고, 아직 액션을 취하지 않은 플레이어를 찾거나, 모든 플레이어를 확인했을 경우 중단
        } while ((player?.lastAction == "폴드" || playerActionStatus[nextIndex] == true) && playersChecked < currentSeatCount)

        activePlayerIndex.value = nextIndex
    }

    fun updatePlayerAction(seatIndex: Int, action: String, amount: Int = 0) {
        val currentPlayer = seatAssignments[seatIndex]
        if (currentPlayer != null) {
            seatAssignments[seatIndex] = currentPlayer.copy(lastAction = action)
            playerActionStatus[seatIndex] = true // 액션 완료 상태 업데이트

            when (action) {
                "폴드" -> {
                    // 폴드 시 특별한 베팅 관련 처리 없음
                }
                "체크/콜" -> {
                    // 현재 베팅 금액만큼 기여
                    playerContributions[seatIndex] = currentBet.value
                }
                "베팅/레이즈" -> {
                    // 새로운 베팅 금액 설정
                    currentBet.value = amount
                    playerContributions[seatIndex] = amount
                    lastBetterIndex.value = seatIndex // 마지막 베팅/레이즈 플레이어 설정
                    // 다른 모든 플레이어의 액션 상태 초기화 (새로운 베팅에 응답해야 하므로)
                    playerActionStatus.keys.forEach { index ->
                        if (index != seatIndex && seatAssignments[index]?.lastAction != "폴드") {
                            playerActionStatus[index] = false
                        }
                    }
                }
            }
        }
    }

    fun resetPlayerActionStatus() {
        seatAssignments.keys.forEach { index ->
            playerActionStatus[index] = false
        }
    }

    fun areAllActivePlayersDone(): Boolean {
        val currentSeatCount = seatCount.value.toIntOrNull() ?: 0
        if (currentSeatCount == 0) return true

        val activePlayers = (0 until currentSeatCount).filter { index ->
            seatAssignments[index]?.lastAction != "폴드"
        }

        if (activePlayers.isEmpty()) return true // 모든 플레이어가 폴드한 경우

        // 베팅이 없는 경우 (모두 체크)
        if (currentBet.value == 0) {
            return activePlayers.all { index ->
                playerActionStatus[index] == true // 모든 활성 플레이어가 액션을 취했는지 확인
            }
        }

        // 베팅이 있는 경우
        val allCalledOrFolded = activePlayers.all { index ->
            val playerContribution = playerContributions.getOrDefault(index, 0)
            playerContribution >= currentBet.value || seatAssignments[index]?.stack == 0 // 콜했거나 올인
        }

        // 마지막 베팅/레이즈 플레이어에게 액션이 돌아왔는지 확인
        val actionReturnedToLastBetter = if (lastBetterIndex.value != null) {
            activePlayerIndex.value == lastBetterIndex.value
        } else {
            true // 베팅이 없었으면 항상 true
        }

        return allCalledOrFolded && actionReturnedToLastBetter
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

    private fun resetBettingRound() {
        currentBet.value = 0
        playerContributions.keys.forEach { index ->
            playerContributions[index] = 0
        }
        lastBetterIndex.value = null // 마지막 베팅/레이즈 플레이어 초기화
    }
}