package com.example.liveholdempokertracker.ui.session


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.liveholdempokertracker.data.PlayerProfile
import com.example.liveholdempokertracker.ui.navigation.Screen

@Composable
fun CurrentSessionScreen(navController: NavController, viewModel: SessionViewModel) {
    val seatCount by viewModel.seatCount
    val tableColor by viewModel.tableColor.collectAsState()
    val cardBackColor by viewModel.cardBackColor.collectAsState()
    val seatAssignments = viewModel.seatAssignments
    val seatCountInt = seatCount.toIntOrNull() ?: 0
    val gamePhase by viewModel.gamePhase
    val canCheck by viewModel.canCheck
    val canUndo by viewModel.canUndo
    val showWinnerSelection by viewModel.showWinnerSelection
    val isHandInProgress by viewModel.isHandInProgress

    val showRemovePlayerDialog = remember { mutableStateOf(false) }
    val seatToRemove = remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(tableColor)
            .padding(16.dp)
    ) {
        IconButton(
            onClick = {
                viewModel.saveSessionData {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            },
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "End Session and Save",
                tint = Color.White
            )
        }

        IconButton(
            onClick = { viewModel.undoLastAction() },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Undo,
                contentDescription = "Undo Last Action",
                tint = if (canUndo) Color.White else Color.Gray
            )
        }

        val playerPositions = listOf(
            Alignment.TopCenter,
            Alignment.TopEnd,
            Alignment.CenterEnd,
            Alignment.BottomEnd,
            Alignment.BottomCenter,
            Alignment.BottomStart,
            Alignment.CenterStart,
            Alignment.TopStart
        )

        val dealerIndex = viewModel.seatAssignments.entries.find { it.value.isDealer }?.key ?: 0
        val sbIndex = (dealerIndex + 1) % seatCountInt
        val bbIndex = (dealerIndex + 2) % seatCountInt

        for (i in 0 until seatCountInt) {
            val player = seatAssignments[i]
            val alignment = playerPositions.getOrNull(i) ?: Alignment.Center
            val isSelectedForWin = viewModel.selectedWinners.contains(i)

            Box(modifier = Modifier.align(alignment)) {
                if (player != null) {
                    val canBeClicked = (showWinnerSelection && player.lastAction != "폴드") || (!isHandInProgress && !showWinnerSelection)
                    PlayerSeat(
                        player = player,
                        seatNumber = i + 1,
                        isActive = i == viewModel.activePlayerIndex.value,
                        isDealer = i == dealerIndex,
                        isSmallBlind = i == sbIndex,
                        isBigBlind = i == bbIndex,
                        cardBackColor = cardBackColor,
                        isSelected = isSelectedForWin,
                        modifier = Modifier.clickable(enabled = canBeClicked) {
                            if (showWinnerSelection) {
                                viewModel.onWinnerSelected(i)
                            } else {
                                seatToRemove.value = i
                                showRemovePlayerDialog.value = true
                            }
                        }
                    )
                } else {
                    EmptySeat(
                        seatNumber = i + 1,
                        modifier = Modifier.clickable(enabled = !isHandInProgress) {
                            // TODO: Navigate to profile list to select a player
                            viewModel.addPlayer(i, PlayerProfile(name = "Player ${i + 1}"))
                        }
                    )
                }
            }
        }

        if (showRemovePlayerDialog.value) {
            val seatIndex = seatToRemove.value
            if (seatIndex != null) {
                val playerToRemove = viewModel.seatAssignments[seatIndex]
                AlertDialog(
                    onDismissRequest = { showRemovePlayerDialog.value = false },
                    title = { Text("플레이어 내보내기") },
                    text = { Text("${playerToRemove?.name}님을 좌석에서 내보내시겠습니까?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.removePlayer(seatIndex)
                                showRemovePlayerDialog.value = false
                            }
                        ) {
                            Text("확인")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showRemovePlayerDialog.value = false }) {
                            Text("취소")
                        }
                    }
                )
            }
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("현재 단계: $gamePhase", color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                viewModel.communityCards.forEach { card ->
                    CardPlaceholder(text = card)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (gamePhase != "Showdown" && !showWinnerSelection) {
                Button(onClick = { viewModel.nextPhase() }) {
                    Text("다음 단계")
                }
            }
        }

        if (showWinnerSelection) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { viewModel.confirmWinnersAndStartNewHand() },
                    enabled = viewModel.selectedWinners.isNotEmpty()
                ) {
                    Text("승자 확정 및 다음 핸드")
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = {
                    viewModel.handleAction(viewModel.activePlayerIndex.value, "폴드")
                }) {
                    Text("폴드")
                }

                if (canCheck) {
                    Button(onClick = {
                        viewModel.handleAction(viewModel.activePlayerIndex.value, "체크/콜")
                    }) {
                        Text("체크")
                    }
                    Button(onClick = {
                        viewModel.handleAction(viewModel.activePlayerIndex.value, "베팅/레이즈")
                    }) {
                        Text("베팅")
                    }
                } else {
                    Button(onClick = {
                        viewModel.handleAction(viewModel.activePlayerIndex.value, "체크/콜")
                    }) {
                        Text("콜")
                    }
                    Button(onClick = {
                        viewModel.handleAction(viewModel.activePlayerIndex.value, "베팅/레이즈")
                    }) {
                        Text("레이즈")
                    }
                }
            }
        }
    }
}

@Composable
fun EmptySeat(
    seatNumber: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(44.dp)) // Placeholder for cards
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color.DarkGray.copy(alpha = 0.5f))
                .border(
                    width = 2.dp,
                    color = Color.Gray,
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Player",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text("좌석 $seatNumber", color = Color.White)
    }
}

@Composable
fun PlayerSeat(
    player: Player,
    seatNumber: Int,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    isDealer: Boolean,
    isSmallBlind: Boolean,
    isBigBlind: Boolean,
    isSelected: Boolean,
    cardBackColor: Color
) {
    val vpip = if (player.handsPlayed > 0) (player.vpipActionCount * 100) / player.handsPlayed else 0
    val pfr = if (player.handsPlayed > 0) (player.pfrActionCount * 100) / player.handsPlayed else 0
    val threeBet = if (player.threeBetOpportunityCount > 0) (player.threeBetActionCount * 100) / player.threeBetOpportunityCount else 0
    val cBet = if (player.cBetOpportunityCount > 0) (player.cBetActionCount * 100) / player.cBetOpportunityCount else 0
    val wmsd = if (player.handsPlayed > 0) (player.wentToShowdownCount * 100) / player.handsPlayed else 0
    val wtsd = if (player.wentToShowdownCount > 0) (player.wonAtShowdownCount * 100) / player.wentToShowdownCount else 0

    val borderColor = when {
        isSelected -> Color.Cyan
        isActive -> Color.Yellow
        else -> Color.DarkGray
    }

    Column(
        modifier = modifier.padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            player.holeCards.forEach { _ ->
                CardPlaceholder(width = 30.dp, height = 40.dp, text = "", cardColor = cardBackColor)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color.DarkGray)
                .border(
                    width = if (isActive || isSelected) 3.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(player.name, color = Color.White, fontSize = 12.sp)

                if (player.lastAction.isNotEmpty()) {
                    Text("(${player.lastAction})", color = Color.White, fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("VPIP: $vpip% / PFR: $pfr%", color = Color.White, fontSize = 10.sp)
                Text("3B: $threeBet% / CB: $cBet%", color = Color.White, fontSize = 10.sp)
                Text("WMSD: $wmsd% / WTSD: $wtsd%", color = Color.White, fontSize = 10.sp)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text("좌석 $seatNumber", color = Color.White)

        Row {
            if (isDealer) {
                Text("D", color = Color.White, fontSize = 10.sp, modifier = Modifier.background(Color.Blue).padding(2.dp))
            }
            if (isSmallBlind) {
                Text("SB", color = Color.White, fontSize = 10.sp, modifier = Modifier.background(Color.Green).padding(2.dp))
            }
            if (isBigBlind) {
                Text("BB", color = Color.White, fontSize = 10.sp, modifier = Modifier.background(Color.Red).padding(2.dp))
            }
        }
    }
}

@Composable
fun CardPlaceholder(width: Dp = 50.dp, height: Dp = 70.dp, text: String = "", cardColor: Color? = null) {
    Box(
        modifier = Modifier
            .size(width, height)
            .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (cardColor != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(cardColor)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(text = text, color = Color.Black, fontSize = 16.sp)
            }
        }
    }
}