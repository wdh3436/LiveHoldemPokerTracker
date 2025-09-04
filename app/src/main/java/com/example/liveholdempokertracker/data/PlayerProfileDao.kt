package com.example.liveholdempokertracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerProfileDao {

    // PlayerProfile specific queries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: PlayerProfile): Long

    @Delete
    suspend fun deleteProfile(profile: PlayerProfile): Int

    @Query("SELECT * FROM player_profiles WHERE name = :name")
    suspend fun getProfileByName(name: String): PlayerProfile?

    @Query("SELECT * FROM player_profiles ORDER BY name ASC")
    fun getAllProfiles(): Flow<List<PlayerProfile>>

    @Transaction
    @Query("SELECT * FROM player_profiles ORDER BY name ASC")
    fun getAllProfilesWithTags(): Flow<List<ProfileWithTags>>

    @Transaction
    @Query("SELECT * FROM player_profiles WHERE id = :id")
    fun getProfileWithTags(id: Int): Flow<ProfileWithTags?>

    // Tag specific queries
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: Tag): Long

    @Delete
    suspend fun deleteTag(tag: Tag): Int

    @Query("SELECT * FROM tags ORDER BY tagName ASC")
    fun getAllTags(): Flow<List<Tag>>

    // Profile-Tag cross reference queries
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTagToProfile(crossRef: PlayerProfileTagCrossRef): Unit

    @Delete
    suspend fun removeTagFromProfile(crossRef: PlayerProfileTagCrossRef): Unit

    @Transaction
    suspend fun mergeProfiles(sourceProfile: PlayerProfile, destinationProfile: PlayerProfile) {
        val mergedProfile = destinationProfile.copy(
            handsPlayed = destinationProfile.handsPlayed + sourceProfile.handsPlayed,
            vpipActionCount = destinationProfile.vpipActionCount + sourceProfile.vpipActionCount,
            pfrActionCount = destinationProfile.pfrActionCount + sourceProfile.pfrActionCount,
            threeBetOpportunityCount = destinationProfile.threeBetOpportunityCount + sourceProfile.threeBetOpportunityCount,
            threeBetActionCount = destinationProfile.threeBetActionCount + sourceProfile.threeBetActionCount,
            cBetOpportunityCount = destinationProfile.cBetOpportunityCount + sourceProfile.cBetOpportunityCount,
            cBetActionCount = destinationProfile.cBetActionCount + sourceProfile.cBetActionCount
        )
        insertOrUpdateProfile(mergedProfile)
        deleteProfile(sourceProfile)
    }
}
