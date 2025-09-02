package com.example.liveholdempokertracker.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profiles")
data class PlayerProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(index = true)
    val name: String,

    val handsPlayed: Int = 0,
    val vpipActionCount: Int = 0,
    val pfrActionCount: Int = 0,

    // 3-Bet
    val threeBetOpportunityCount: Int = 0,
    val threeBetActionCount: Int = 0,

    // C-Bet
    val cBetOpportunityCount: Int = 0,
    val cBetActionCount: Int = 0,

    val memo: String = ""
) {
    fun getVpip(): Int {
        if (handsPlayed == 0) return 0
        return (vpipActionCount * 100) / handsPlayed
    }

    fun getPfr(): Int {
        if (handsPlayed == 0) return 0
        return (pfrActionCount * 100) / handsPlayed
    }

    fun get3Bet(): Int {
        if (threeBetOpportunityCount == 0) return 0
        return (threeBetActionCount * 100) / threeBetOpportunityCount
    }

    fun getCBet(): Int {
        if (cBetOpportunityCount == 0) return 0
        return (cBetActionCount * 100) / cBetOpportunityCount
    }
}
