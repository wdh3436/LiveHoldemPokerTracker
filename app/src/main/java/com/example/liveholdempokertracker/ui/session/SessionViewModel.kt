package com.example.liveholdempokertracker.ui.session

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.liveholdempokertracker.data.ActiveSession
import com.example.liveholdempokertracker.data.PlayerProfile
import com.example.liveholdempokertracker.data.PlayerProfileDao
import com.example.liveholdempokertracker.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@Serializable
data class Player(
    val name: String,
    val isDealer: Boolean = false,
    val lastAction: String = "",
    val holeCards: List<String> = emptyList(),
    val handsPlayed: Int = 0,
    val vpipActionCount: Int = 0,
    val pfrActionCount: Int = 0,
    val threeBetOpportunityCount: Int = 0,
    val threeBetActionCount: Int = 0,
    val cBetOpportunityCount: Int = 0,
    val cBetActionCount: Int = 0
)

@Serializable
data class GameState(
    val seatAssignments: Map<Int, Player>,
    val communityCards: List<String>,
    val gamePhase: String,
    val activePlayerIndex: Int,
    val lastRaiserIndex: Int?,
    val isBetMadeThisRound: Boolean,
    val canCheck: Boolean,
    val isPreFlopBbOption: Boolean,
    val vpipPlayersThisHand: Set<Int>,
    val pfrPlayersThisHand: Set<Int>,
    val threeBetOpportunityPlayersThisHand: Set<Int>,
    val cBetOpportunityPlayerThisHand: Int?,
    val preFlopRaiser: Int?
)

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val playerProfileDao: PlayerProfileDao,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    var seatCount = mutableStateOf("")
    val seatAssignments = mutableStateMapOf<Int, Player>()
    val communityCards = mutableStateListOf<String>()
    var gamePhase = mutableStateOf("Pre-Flop")
    var activePlayerIndex = mutableStateOf(0)
    var lastRaiserIndex = mutableStateOf<Int?>(null)
    var isBetMadeThisRound = mutableStateOf(false)
    val canCheck = mutableStateOf(false)
    val canUndo = mutableStateOf(false)

    val tableColor: StateFlow<Color> = settingsRepository.tableColorFlow
        .map { Color(it ?: 0xFF2E7D32) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = Color(0xFF2E7D32)
        )

    val cardBackColor: StateFlow<Color> = settingsRepository.cardBackColorFlow
        .map { Color(it ?: 0xFF1565C0) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = Color(0xFF1565C0)
        )

    val hasActiveSession: StateFlow<Boolean> = playerProfileDao.getActiveSession()
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val hasPreviousSetup: StateFlow<Boolean> = settingsRepository.lastSessionSetupFlow
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun loadPreviousSetupAndStart(onLoaded: () -> Unit) {
        viewModelScope.launch {
            val setup = settingsRepository.lastSessionSetupFlow.first()
            if (setup != null) {
                val profilesToAssign = withContext(Dispatchers.IO) {
                    setup.assignments.map { (index, name) ->
                        val profile = playerProfileDao.getProfileByName(name)
                        index to (profile ?: PlayerProfile(name = "GUEST"))
                    }
                }
                clearSetup()
                seatCount.value = setup.seatCount.toString()
                profilesToAssign.forEach { (index, profile) ->
                    assignProfile(index, profile)
                }
                startFirstGame()
                onLoaded()
            }
        }
    }

    private fun saveGameState() {
        viewModelScope.launch(Dispatchers.IO) {
            val state = captureState()
            val jsonState = Json.encodeToString(state)
            playerProfileDao.insertOrUpdateActiveSession(ActiveSession(gameStateJson = jsonState))
        }
    }

    fun loadActiveSession(onLoaded: () -> Unit) {
        viewModelScope.launch {
            val activeSession = playerProfileDao.getActiveSession().first()
            if (activeSession != null) {
                val state = Json.decodeFromString<GameState>(activeSession.gameStateJson)
                restoreState(state)
                onLoaded()
            }
        }
    }

    private fun clearActiveSession() {
        viewModelScope.launch(Dispatchers.IO) {
            playerProfileDao.deleteActiveSession()
        }
    }

    private fun restoreState(state: GameState) {
        seatAssignments.clear()
        seatAssignments.putAll(state.seatAssignments)
        communityCards.clear()
        communityCards.addAll(state.communityCards)
        gamePhase.value = state.gamePhase
        activePlayerIndex.value = state.activePlayerIndex
        lastRaiserIndex.value = state.lastRaiserIndex
        isBetMadeThisRound.value = state.isBetMadeThisRound
        canCheck.value = state.canCheck
        isPreFlopBbOption = state.isPreFlopBbOption
        vpipPlayersThisHand.clear()
        vpipPlayersThisHand.addAll(state.vpipPlayersThisHand)
        pfrPlayersThisHand.clear()
        pfrPlayersThisHand.addAll(state.pfrPlayersThisHand)
        threeBetOpportunityPlayersThisHand.clear()
        threeBetOpportunityPlayersThisHand.addAll(state.threeBetOpportunityPlayersThisHand)
        cBetOpportunityPlayerThisHand = state.cBetOpportunityPlayerThisHand
        preFlopRaiser = state.preFlopRaiser
        seatCount.value = state.seatAssignments.size.toString()
    }

    private val history = mutableListOf<GameState>()
    private val vpipPlayersThisHand = mutableSetOf<Int>()
    private val pfrPlayersThisHand = mutableSetOf<Int>()
    private val threeBetOpportunityPlayersThisHand = mutableSetOf<Int>()
    private var cBetOpportunityPlayerThisHand: Int? = null
    private var preFlopRaiser: Int? = null
    private var isPreFlopBbOption = true

    private fun captureState(): GameState {
        return GameState(
            seatAssignments = seatAssignments.toMap(),
            communityCards = communityCards.toList(),
            gamePhase = gamePhase.value,
            activePlayerIndex = activePlayerIndex.value,
            lastRaiserIndex = lastRaiserIndex.value,
            isBetMadeThisRound = isBetMadeThisRound.value,
            canCheck = canCheck.value,
            isPreFlopBbOption = isPreFlopBbOption,
            vpipPlayersThisHand = vpipPlayersThisHand.toSet(),
            pfrPlayersThisHand = pfrPlayersThisHand.toSet(),
            threeBetOpportunityPlayersThisHand = threeBetOpportunityPlayersThisHand.toSet(),
            cBetOpportunityPlayerThisHand = cBetOpportunityPlayerThisHand,
            preFlopRaiser = preFlopRaiser
        )
    }

    fun undoLastAction() {
        if (history.isEmpty()) return
        val lastState = history.removeLast()
        canUndo.value = history.isNotEmpty()
        restoreState(lastState)
        saveGameState()
    }

    fun assignProfile(seatIndex: Int, profile: PlayerProfile) {
        val tempHoleCards = listOf("A", "K")
        seatAssignments[seatIndex] = Player(
            name = profile.name,
            holeCards = tempHoleCards,
            handsPlayed = profile.handsPlayed,
            vpipActionCount = profile.vpipActionCount,
            pfrActionCount = profile.pfrActionCount,
            threeBetOpportunityCount = profile.threeBetOpportunityCount,
            threeBetActionCount = profile.threeBetActionCount,
            cBetOpportunityCount = profile.cBetOpportunityCount,
            cBetActionCount = profile.cBetActionCount
        )
    }

    fun saveSessionData(onSessionSaved: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                seatAssignments.values.forEach { player ->
                    if (player.name != "GUEST") {
                        val existingProfile = playerProfileDao.getProfileByName(player.name)
                        val profileToSave = PlayerProfile(
                            id = existingProfile?.id ?: 0,
                            name = player.name,
                            handsPlayed = player.handsPlayed,
                            vpipActionCount = player.vpipActionCount,
                            pfrActionCount = player.pfrActionCount,
                            threeBetOpportunityCount = player.threeBetOpportunityCount,
                            threeBetActionCount = player.threeBetActionCount,
                            cBetOpportunityCount = player.cBetOpportunityCount,
                            cBetActionCount = player.cBetActionCount
                        )
                        playerProfileDao.insertOrUpdateProfile(profileToSave)
                    }
                }
                playerProfileDao.deleteActiveSession()
            }
            onSessionSaved()
        }
    }

    fun clearSetup() {
        seatCount.value = ""
        seatAssignments.clear()
        communityCards.clear()
        gamePhase.value = "Pre-Flop"
        activePlayerIndex.value = 0
        vpipPlayersThisHand.clear()
        pfrPlayersThisHand.clear()
        threeBetOpportunityPlayersThisHand.clear()
        cBetOpportunityPlayerThisHand = null
        preFlopRaiser = null
        history.clear()
        canUndo.value = false
        updateActionFlags()
        clearActiveSession()
    }

    fun nextPhase() {
        isPreFlopBbOption = false
        if (gamePhase.value == "Pre-Flop" && preFlopRaiser != null) {
            val player = seatAssignments[preFlopRaiser!!]
            if (player != null) {
                cBetOpportunityPlayerThisHand = preFlopRaiser
                seatAssignments[preFlopRaiser!!] = player.copy(cBetOpportunityCount = player.cBetOpportunityCount + 1)
            }
        }
        gamePhase.value = when (gamePhase.value) {
            "Pre-Flop" -> { addFlopCards(); "Flop" }
            "Flop" -> { addTurnCard(); "Turn" }
            "Turn" -> { addRiverCard(); "River" }
            "River" -> "Showdown"
            else -> "Pre-Flop"
        }
        saveGameState()
    }

    fun addFlopCards() { communityCards.addAll(listOf("A♠", "K♥", "Q♣")) }
    fun addTurnCard() { communityCards.add("J♦") }
    fun addRiverCard() { communityCards.add("10♠") }

    fun startFirstGame() {
        val assignedSeats = seatAssignments.keys.sorted()
        if (assignedSeats.isNotEmpty()) {
            startGame(assignedSeats.first())
        }
    }

    fun newHand() {
        val assignedSeats = seatAssignments.keys.sorted()
        if (assignedSeats.isEmpty()) return
        val currentDealerKey = seatAssignments.entries.find { it.value.isDealer }?.key ?: (assignedSeats.lastOrNull() ?: -1)
        if (currentDealerKey != -1) {
            seatAssignments[currentDealerKey] = seatAssignments[currentDealerKey]!!.copy(isDealer = false)
        }
        val currentDealerListIndex = assignedSeats.indexOf(currentDealerKey)
        val nextDealerListIndex = (currentDealerListIndex + 1) % assignedSeats.size
        val nextDealerIndex = assignedSeats[nextDealerListIndex]
        seatAssignments.keys.forEach { index ->
            val player = seatAssignments[index]
            if (player != null) {
                seatAssignments[index] = player.copy(lastAction = "", holeCards = emptyList())
            }
        }
        communityCards.clear()
        gamePhase.value = "Pre-Flop"
        startGame(nextDealerIndex)
        history.clear()
        canUndo.value = false
        saveGameState()
    }

    fun startGame(dealerIndex: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.updateLastSessionSetup(seatCount.value.toIntOrNull() ?: 0, seatAssignments)
        }
        isPreFlopBbOption = true
        vpipPlayersThisHand.clear()
        pfrPlayersThisHand.clear()
        threeBetOpportunityPlayersThisHand.clear()
        cBetOpportunityPlayerThisHand = null
        preFlopRaiser = null
        seatAssignments.keys.forEach { index ->
            val player = seatAssignments[index]
            if (player != null) {
                seatAssignments[index] = player.copy(handsPlayed = player.handsPlayed + 1)
            }
        }
        val seatCount = seatAssignments.size
        if (seatCount == 0) return
        seatAssignments[dealerIndex] = seatAssignments[dealerIndex]!!.copy(isDealer = true)
        val assignedSeats = seatAssignments.keys.sorted()
        val dealerListIndex = assignedSeats.indexOf(dealerIndex)
        val sbListIndex = (dealerListIndex + 1) % assignedSeats.size
        val bbListIndex = (dealerListIndex + 2) % assignedSeats.size
        val sbIndex = assignedSeats[sbListIndex]
        val bbIndex = assignedSeats[bbListIndex]
        seatAssignments[sbIndex] = seatAssignments[sbIndex]!!.copy(lastAction = "SB")
        seatAssignments[bbIndex] = seatAssignments[bbIndex]!!.copy(lastAction = "BB")
        lastRaiserIndex.value = bbIndex
        val utgListIndex = (dealerListIndex + 3) % assignedSeats.size
        val utgIndex = assignedSeats[utgListIndex]
        activePlayerIndex.value = utgIndex
        isBetMadeThisRound.value = true
        updateActionFlags()
        saveGameState()
    }

    fun handleAction(playerIndex: Int, action: String) {
        var player = seatAssignments[playerIndex]
        if (player == null || player.lastAction == "폴드" || playerIndex != activePlayerIndex.value) return
        history.add(captureState())
        canUndo.value = true
        val isRaise = action == "베팅/레이즈"
        if (gamePhase.value == "Pre-Flop") {
            val isVpipAction = action == "체크/콜" || isRaise
            if (isVpipAction && vpipPlayersThisHand.add(playerIndex)) {
                player = player.copy(vpipActionCount = player.vpipActionCount + 1)
            }
            if (isRaise && pfrPlayersThisHand.add(playerIndex)) {
                player = player.copy(pfrActionCount = player.pfrActionCount + 1)
            }
            if (threeBetOpportunityPlayersThisHand.contains(playerIndex) && isRaise) {
                player = player.copy(threeBetActionCount = player.threeBetActionCount + 1)
            }
        } else if (gamePhase.value == "Flop") {
            if (cBetOpportunityPlayerThisHand == playerIndex && isRaise) {
                player = player.copy(cBetActionCount = player.cBetActionCount + 1)
                cBetOpportunityPlayerThisHand = null
            }
        }
        seatAssignments[playerIndex] = player
        seatAssignments[playerIndex] = player.copy(lastAction = action)
        if (action == "폴드") {
            val activePlayers = seatAssignments.values.count { it.lastAction != "폴드" }
            if (activePlayers <= 1) {
                gamePhase.value = "Showdown"
                saveGameState()
                return
            }
        }
        if (isRaise) {
            lastRaiserIndex.value = playerIndex
            isPreFlopBbOption = false
            isBetMadeThisRound.value = true
        }
        if (gamePhase.value == "Pre-Flop" && isPreFlopBbOption && playerIndex == lastRaiserIndex.value && !isRaise) {
            endBettingRound()
        } else {
            moveToNextPlayer(startFrom = playerIndex)
        }
        saveGameState()
    }

    private fun moveToNextPlayer(startFrom: Int) {
        var nextIndex = startFrom
        do {
            nextIndex = (nextIndex + 1) % seatAssignments.size
        } while (seatAssignments[nextIndex]?.lastAction == "폴드" || seatAssignments[nextIndex] == null)
        if (gamePhase.value == "Pre-Flop" && lastRaiserIndex.value != null) {
            val nextPlayer = seatAssignments[nextIndex]
            if (nextPlayer != null && threeBetOpportunityPlayersThisHand.add(nextIndex)) {
                seatAssignments[nextIndex] = nextPlayer.copy(threeBetOpportunityCount = nextPlayer.threeBetOpportunityCount + 1)
            }
        }
        if (nextIndex == lastRaiserIndex.value) {
            if (gamePhase.value == "Pre-Flop" && isPreFlopBbOption) {
                activePlayerIndex.value = nextIndex
            } else {
                endBettingRound()
            }
        }
        else {
            activePlayerIndex.value = nextIndex
        }
        updateActionFlags()
    }

    private fun endBettingRound() {
        if (gamePhase.value == "Pre-Flop") {
            preFlopRaiser = lastRaiserIndex.value
        }
        val activePlayers = seatAssignments.values.count { it.lastAction != "폴드" }
        if (activePlayers <= 1) {
            gamePhase.value = "Showdown"
        } else {
            nextPhase()
            resetForNewRound()
        }
        saveGameState()
    }

    private fun updateActionFlags() {
        val assignedSeats = seatAssignments.keys.sorted()
        if (assignedSeats.isEmpty()) {
            canCheck.value = false
            return
        }
        val dealerKey = seatAssignments.entries.find { it.value.isDealer }?.key ?: -1
        val dealerListIndex = assignedSeats.indexOf(dealerKey)
        if (dealerListIndex == -1) {
            canCheck.value = false
            return
        }
        val bbListIndex = (dealerListIndex + 2) % assignedSeats.size
        val bbIndex = assignedSeats[bbListIndex]
        val currentPlayerIsBB = activePlayerIndex.value == bbIndex
        val bbCanCheckPreflop = gamePhase.value == "Pre-Flop" &&
                                 currentPlayerIsBB &&
                                 isPreFlopBbOption
        canCheck.value = !isBetMadeThisRound.value || bbCanCheckPreflop
    }

    private fun resetForNewRound() {
        isBetMadeThisRound.value = false
        val dealerIndex = seatAssignments.entries.find { it.value.isDealer }?.key ?: 0
        lastRaiserIndex.value = null
        var firstToAct = (dealerIndex + 1) % seatAssignments.size
        while(seatAssignments[firstToAct]?.lastAction == "폴드") {
            firstToAct = (firstToAct + 1) % seatAssignments.size
        }
        activePlayerIndex.value = firstToAct
        lastRaiserIndex.value = firstToAct
        updateActionFlags()
    }
}
