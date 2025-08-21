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
}
