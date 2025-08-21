package com.example.liveholdempokertracker.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PlayerProfile::class, Tag::class, PlayerProfileTagCrossRef::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerProfileDao(): PlayerProfileDao
}
