package com.example.liveholdempokertracker.ui.session

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import com.example.liveholdempokertracker.ui.session.SessionViewModel
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@Composable
fun CurrentSessionScreen(viewModel: SessionViewModel) {
    val seatCount by viewModel.seatCount
    val selectedColor by viewModel.selectedColor
    val seatAssignments = viewModel.seatAssignments
    val seatCountInt = seatCount.toIntOrNull() ?: 0
    val gamePhase by viewModel.gamePhase
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(selectedColor) // 테이블 색상 적용
            .padding(16.dp)
    ) {
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

        for (i in 0 until seatCountInt) {
            val player = seatAssignments.getOrDefault(i, Player(name = "GUEST"))
            val alignment = playerPositions.getOrNull(i) ?: Alignment.Center // 기본값 설정

            Box(modifier = Modifier.align(alignment)) {
                PlayerSeat(player = player, seatNumber = i + 1)
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
            Button(onClick = { viewModel.nextPhase() }) {
                Text("다음 단계")
            }
        }

        // 액션 버튼
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp), // 버튼이 화면 하단에 위치하도록 조정
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = {
                viewModel.updatePlayerAction(viewModel.activePlayerIndex.value, "폴드", 0)
                viewModel.moveToNextPlayer()
                if (viewModel.areAllActivePlayersDone()) {
                    viewModel.nextPhase()
                }
                Toast.makeText(context, "폴드 액션", Toast.LENGTH_SHORT).show()
            }) {
                Text("폴드")
            }
            Button(onClick = {
                viewModel.updatePlayerAction(viewModel.activePlayerIndex.value, "체크/콜", viewModel.currentBet.value)
                viewModel.moveToNextPlayer()
                if (viewModel.areAllActivePlayersDone()) {
                    viewModel.nextPhase()
                }
                Toast.makeText(context, "체크/콜 액션", Toast.LENGTH_SHORT).show()
            }) {
                Text("체크/콜")
            }
            Button(onClick = {
                viewModel.updatePlayerAction(viewModel.activePlayerIndex.value, "베팅/레이즈", 100) // 임시 베팅 금액 100
                viewModel.moveToNextPlayer()
                if (viewModel.areAllActivePlayersDone()) {
                    viewModel.nextPhase()
                }
                Toast.makeText(context, "베팅/레이즈 액션", Toast.LENGTH_SHORT).show()
            }) {
                Text("베팅/레이즈")
            }
        }
    }
}

@Composable
fun PlayerSeat(player: Player, seatNumber: Int, isActive: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // 플레이어 카드
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            player.holeCards.forEach { card ->
                CardPlaceholder(width = 30.dp, height = 40.dp, text = card)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color.DarkGray) // 플레이어 아바타/프로필 이미지 Placeholder
                .border(
                    width = if (isActive) 4.dp else 1.dp,
                    color = if (isActive) Color.Yellow else Color.DarkGray,
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(player.name, color = Color.White)
                Text("스택: ${player.stack}", color = Color.White, fontSize = 12.sp)
                if (player.lastAction.isNotEmpty()) {
                    Text("(${player.lastAction})", color = Color.White, fontSize = 10.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text("좌석 $seatNumber", color = Color.White)

        // 딜러, 스몰 블라인드, 빅 블라인드 표시 (임시)
        Row {
            if (player.isDealer) {
                Text("D", color = Color.White, fontSize = 10.sp, modifier = Modifier.background(Color.Blue).padding(2.dp))
            }
            if (player.isSmallBlind) {
                Text("SB", color = Color.White, fontSize = 10.sp, modifier = Modifier.background(Color.Green).padding(2.dp))
            }
            if (player.isBigBlind) {
                Text("BB", color = Color.White, fontSize = 10.sp, modifier = Modifier.background(Color.Red).padding(2.dp))
            }
        }
    }
}

@Composable
fun CardPlaceholder(width: Dp = 50.dp, height: Dp = 70.dp, text: String = "") {
    Box(
        modifier = Modifier
            .size(width, height)
            .background(Color.White, shape = RoundedCornerShape(4.dp))
            .border(1.dp, Color.Black, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.Black, fontSize = 16.sp)
    }
}