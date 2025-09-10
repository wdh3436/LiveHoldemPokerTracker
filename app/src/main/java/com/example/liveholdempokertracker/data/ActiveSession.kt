package com.example.liveholdempokertracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_session")
data class ActiveSession(
    @PrimaryKey
    val id: Int = 1, // 항상 하나의 세션만 저장하므로 ID를 1로 고정
    val gameStateJson: String
)
