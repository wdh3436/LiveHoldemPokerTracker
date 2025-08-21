package com.example.liveholdempokertracker.data

import androidx.room.Entity

@Entity(primaryKeys = ["id", "tagId"])
data class PlayerProfileTagCrossRef(
    val id: Int,
    val tagId: Int
)
