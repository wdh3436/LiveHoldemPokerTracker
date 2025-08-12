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
    val holeCards: List<String> = emptyList(), // 플레이어의 홀덤 패
    // HUD 통계 필드
    val handsPlayed: Int = 0,
    val vpipActionCount: Int = 0, // Voluntarily Put Money In Pot
    val pfrActionCount: Int = 0  // Pre-Flop Raise
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

    // 현재 핸드에서 VPIP/PFR 액션을 한 플레이어를 추적
    private val vpipPlayersThisHand = mutableSetOf<Int>()
    private val pfrPlayersThisHand = mutableSetOf<Int>()

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
        vpipPlayersThisHand.clear()
        pfrPlayersThisHand.clear()
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

    // 첫 핸드를 시작하기 위한 함수
    fun startFirstGame() {
        val assignedSeats = seatAssignments.keys.sorted()
        if (assignedSeats.isNotEmpty()) {
            startGame(assignedSeats.first()) // 첫번째 좌석에 딜러 버튼을 주고 시작
        }
    }

    fun newHand() {
        val assignedSeats = seatAssignments.keys.sorted()
        if (assignedSeats.isEmpty()) return

        // 1. 현재 딜러를 찾아 다음 딜러 인덱스 결정
        val currentDealerKey = seatAssignments.entries.find { it.value.isDealer }?.key ?: (assignedSeats.lastOrNull() ?: -1)
        if (currentDealerKey != -1) {
            seatAssignments[currentDealerKey] = seatAssignments[currentDealerKey]!!.copy(isDealer = false)
        }
        val currentDealerListIndex = assignedSeats.indexOf(currentDealerKey)
        val nextDealerListIndex = (currentDealerListIndex + 1) % assignedSeats.size
        val nextDealerIndex = assignedSeats[nextDealerListIndex]

        // 2. 모든 플레이어의 상태 초기화
        seatAssignments.keys.forEach { index ->
            val player = seatAssignments[index]
            if (player != null) {
                seatAssignments[index] = player.copy(lastAction = "", holeCards = emptyList())
            }
        }

        // 3. 커뮤니티 카드 초기화
        communityCards.clear()

        // 4. 게임 단계 초기화
        gamePhase.value = "Pre-Flop"

        // 5. 새 핸드 시작
        startGame(nextDealerIndex)
    }

    fun startGame(dealerIndex: Int) {
        // Reset hand-specific trackers
        vpipPlayersThisHand.clear()
        pfrPlayersThisHand.clear()

        // Increment handsPlayed for all players
        seatAssignments.keys.forEach { index ->
            val player = seatAssignments[index]
            if (player != null) {
                seatAssignments[index] = player.copy(handsPlayed = player.handsPlayed + 1)
            }
        }

        val seatCount = seatAssignments.size
        if (seatCount == 0) return

        // 새로운 딜러 설정
        seatAssignments[dealerIndex] = seatAssignments[dealerIndex]!!.copy(isDealer = true)

        // 딜러 기준으로 SB, BB 설정
        val assignedSeats = seatAssignments.keys.sorted()
        val dealerListIndex = assignedSeats.indexOf(dealerIndex)

        val sbListIndex = (dealerListIndex + 1) % assignedSeats.size
        val bbListIndex = (dealerListIndex + 2) % assignedSeats.size
        val sbIndex = assignedSeats[sbListIndex]
        val bbIndex = assignedSeats[bbListIndex]

        seatAssignments[sbIndex] = seatAssignments[sbIndex]!!.copy(lastAction = "SB")
        seatAssignments[bbIndex] = seatAssignments[bbIndex]!!.copy(lastAction = "BB")

        // The last raiser is the BB before the flop
        lastRaiserIndex.value = bbIndex

        // Pre-flop action starts to the left of the BB (UTG)
        val utgListIndex = (dealerListIndex + 3) % assignedSeats.size
        val utgIndex = assignedSeats[utgListIndex]
        activePlayerIndex.value = utgIndex
    }

    fun handleAction(playerIndex: Int, action: String) {
        var player = seatAssignments[playerIndex]
        if (player == null || player.lastAction == "폴드" || playerIndex != activePlayerIndex.value) return

        // --- HUD 통계 계산 로직 (변경 없음) ---
        if (gamePhase.value == "Pre-Flop") {
            val isVpipAction = action == "체크/콜" || action == "베팅/레이즈"
            if (isVpipAction && vpipPlayersThisHand.add(playerIndex)) {
                player = player.copy(vpipActionCount = player.vpipActionCount + 1)
                seatAssignments[playerIndex] = player
            }
            val isPfrAction = action == "베팅/레이즈"
            if (isPfrAction && pfrPlayersThisHand.add(playerIndex)) {
                player = player.copy(pfrActionCount = player.pfrActionCount + 1)
                seatAssignments[playerIndex] = player
            }
        }
        // --- HUD 통계 계산 로직 끝 ---

        // 액션 적용
        seatAssignments[playerIndex] = player.copy(lastAction = action)

        // 베팅/레이즈가 나오면 마지막 레이저를 업데이트
        val isRaise = action == "베팅/레이즈"
        if (isRaise) {
            lastRaiserIndex.value = playerIndex
        }

        // 다음 플레이어로 순서 이동 (라운드 종료 로직은 moveToNextPlayer로 이동)
        moveToNextPlayer(startFrom = playerIndex)
    }

    private fun moveToNextPlayer(startFrom: Int) {
        val seatCount = seatAssignments.size
        if (seatCount < 2) {
            endBettingRound()
            return
        }

        // 다음 액션 플레이어 찾기
        var nextIndex = startFrom
        do {
            nextIndex = (nextIndex + 1) % seatCount
        } while (seatAssignments[nextIndex]?.lastAction == "폴드")

        // 다음 플레이어가 마지막 레이저와 같으면 라운드 종료
        // 또는 액티브 플레이어가 1명 이하면 라운드 종료
        val activePlayers = seatAssignments.values.count { it.lastAction != "폴드" }
        if (nextIndex == lastRaiserIndex.value || activePlayers < 2) {
            endBettingRound()
        } else {
            activePlayerIndex.value = nextIndex
        }
    }

    private fun endBettingRound() {
        val activePlayers = seatAssignments.values.count { it.lastAction != "폴드" }
        if (activePlayers <= 1) {
            gamePhase.value = "Showdown" // 플레이어가 한 명만 남으면 쇼다운으로 이동
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