package com.example.liveholdempokertracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(profile: PlayerProfile): Long

    @Delete
    fun delete(profile: PlayerProfile): Int

    @Query("SELECT * FROM player_profiles WHERE name = :name")
    fun getProfileByName(name: String): PlayerProfile?

    @Query("SELECT * FROM player_profiles ORDER BY name ASC")
    fun getAllProfiles(): Flow<List<PlayerProfile>>
}
