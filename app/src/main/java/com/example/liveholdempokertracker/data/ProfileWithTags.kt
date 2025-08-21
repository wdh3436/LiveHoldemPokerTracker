package com.example.liveholdempokertracker.data

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class ProfileWithTags(
    @Embedded val profile: PlayerProfile,
    @Relation(
        parentColumn = "id",
        entityColumn = "tagId",
        associateBy = Junction(PlayerProfileTagCrossRef::class)
    )
    val tags: List<Tag>
)
