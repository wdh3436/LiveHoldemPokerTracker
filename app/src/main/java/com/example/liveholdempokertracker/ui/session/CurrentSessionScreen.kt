package com.example.liveholdempokertracker.ui.session


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.liveholdempokertracker.R
import com.example.liveholdempokertracker.ui.navigation.Screen

@Composable
fun CurrentSessionScreen(navController: NavController, viewModel: SessionViewModel) {
    val seatCount by viewModel.seatCount
    val tableColor by viewModel.tableColor.collectAsState()
    val cardBackColor by viewModel.cardBackColor.collectAsState()
    val seatAssignments = viewModel.seatAssignments
    val seatCountInt = seatCount.toIntOrNull() ?: 0
    val gamePhase by viewModel.gamePhase
    val context = LocalContext.current
    val canCheck by viewModel.canCheck
    val canUndo by viewModel.canUndo

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(tableColor) // 테이블 색상 적용
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

        // 플레이어 좌석 배치 (최대 10명)
        // 기획서 15페이지를 참고하여 좌석 배치
        // 현재는 임시로 8개 좌석만 배치 (나머지는 필요에 따라 추가)
        val playerPositions = listOf(
            Alignment.TopCenter, // 좌석 1
            Alignment.TopEnd,    // 좌석 2
            Alignment.CenterEnd,  // 좌석 3
            Alignment.BottomEnd,  // 좌석 4
            Alignment.BottomCenter, // 좌석 5
            Alignment.BottomStart, // 좌석 6
            Alignment.CenterStart, // 좌석 7
            Alignment.TopStart   // 좌석 8
        )

        val dealerIndex = viewModel.seatAssignments.entries.find { it.value.isDealer }?.key ?: 0
        val sbIndex = (dealerIndex + 1) % seatCountInt
        val bbIndex = (dealerIndex + 2) % seatCountInt

        for (i in 0 until seatCountInt) {
            val player = seatAssignments.getOrDefault(i, Player(name = "GUEST"))
            val alignment = playerPositions.getOrNull(i) ?: Alignment.Center

            Box(modifier = Modifier.align(alignment)) {
                PlayerSeat(
                    player = player,
                    seatNumber = i + 1,
                    isActive = i == viewModel.activePlayerIndex.value,
                    isDealer = i == dealerIndex,
                    isSmallBlind = i == sbIndex,
                    isBigBlind = i == bbIndex,
                    cardBackColor = cardBackColor
                )
            }
        }

        // 테이블 중앙 (커뮤니티 카드)
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
            // 게임 단계에 따라 버튼을 다르게 표시
            if (gamePhase == "Showdown") {
                Button(onClick = { viewModel.newHand() }) {
                    Text("새 핸드 시작")
                }
            } else {
                Button(onClick = { viewModel.nextPhase() }) {
                    Text("다음 단계")
                }
            }
        }

        // 액션 버튼
        if (gamePhase != "Showdown") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp), // 버튼이 화면 하단에 위치하도록 조정
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = {
                    viewModel.handleAction(viewModel.activePlayerIndex.value, "폴드")
                }) {
                    Text("폴드")
                }

                if (canCheck) {
                    // 베팅이 없는 상황: 체크, 베팅 버튼 표시
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
                    // 베팅이 나온 상황: 콜, 레이즈 버튼 표시
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
fun PlayerSeat(
    player: Player,
    seatNumber: Int,
    isActive: Boolean = false,
    isDealer: Boolean,
    isSmallBlind: Boolean,
    isBigBlind: Boolean,
    cardBackColor: Color
) {
    // --- HUD 통계 계산 ---
    val vpip = if (player.handsPlayed > 0) (player.vpipActionCount * 100) / player.handsPlayed else 0
    val pfr = if (player.handsPlayed > 0) (player.pfrActionCount * 100) / player.handsPlayed else 0
    val threeBet = if (player.threeBetOpportunityCount > 0) (player.threeBetActionCount * 100) / player.threeBetOpportunityCount else 0
    val cBet = if (player.cBetOpportunityCount > 0) (player.cBetActionCount * 100) / player.cBetOpportunityCount else 0

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // 플레이어 카드
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            player.holeCards.forEach { _ -> // 카드 내용은 무시하고 뒷면을 표시
                CardPlaceholder(width = 30.dp, height = 40.dp, text = "", cardColor = cardBackColor)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .size(120.dp) // HUD 표시를 위해 박스 크기 증가
                .background(Color.DarkGray) // 플레이어 아바타/프로필 이미지 Placeholder
                .border(
                    width = if (isActive) 4.dp else 1.dp,
                    color = if (isActive) Color.Yellow else Color.DarkGray,
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
                // --- HUD 통계 표시 ---
                Text("VPIP: $vpip%", color = Color.White, fontSize = 10.sp)
                Text("PFR: $pfr%", color = Color.White, fontSize = 10.sp)
                Text("3-Bet: $threeBet%", color = Color.White, fontSize = 10.sp)
                Text("C-Bet: $cBet%", color = Color.White, fontSize = 10.sp)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text("좌석 $seatNumber", color = Color.White)

        // 딜러, 스몰 블라인드, 빅 블라인드 표시
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
