package com.example.liveholdempokertracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey(autoGenerate = true)
    val tagId: Int = 0,
    val tagName: String,
    val tagColor: Long // Storing color as a Long (e.g., 0xFFFF0000 for red)
)
