package com.example.liveholdempokertracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.liveholdempokertracker.ui.session.Player
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_v2")

data class LastSessionSetup(
    val seatCount: Int,
    val assignments: Map<Int, String> // Seat Index to Player Name
)

@Singleton
class SettingsRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private object PreferencesKeys {
        val TABLE_COLOR = longPreferencesKey("table_color")
        val CARD_BACK_COLOR = longPreferencesKey("card_back_color")
        val LAST_SEAT_COUNT = intPreferencesKey("last_seat_count")
        val LAST_PLAYER_ASSIGNMENTS = stringSetPreferencesKey("last_player_assignments")
    }

    val tableColorFlow: Flow<Long?> = context.dataStore.data
        .map {
            it[PreferencesKeys.TABLE_COLOR]
        }

    val cardBackColorFlow: Flow<Long?> = context.dataStore.data
        .map {
            it[PreferencesKeys.CARD_BACK_COLOR]
        }
    
    val lastSessionSetupFlow: Flow<LastSessionSetup?> = context.dataStore.data
        .map { preferences ->
            try {
                val seatCount = preferences[PreferencesKeys.LAST_SEAT_COUNT]
                val assignmentsSet = preferences[PreferencesKeys.LAST_PLAYER_ASSIGNMENTS]

                if (seatCount == null || assignmentsSet == null) {
                    null
                } else {
                    val assignmentsMap = assignmentsSet.mapNotNull { str ->
                        try {
                            val parts = str.split(":", limit = 2)
                            if (parts.size == 2) {
                                parts[0].toInt() to parts[1]
                            } else {
                                null // Ignore malformed entries
                            }
                        } catch (e: Exception) {
                            null // Ignore entries that cause errors
                        }
                    }.toMap()
                    LastSessionSetup(seatCount, assignmentsMap)
                }
            } catch (e: ClassCastException) {
                null // Return null to prevent crash
            }
        }

    suspend fun updateTableColor(color: Long) {
        context.dataStore.edit {
            it[PreferencesKeys.TABLE_COLOR] = color
        }
    }

    suspend fun updateCardBackColor(color: Long) {
        context.dataStore.edit {
            it[PreferencesKeys.CARD_BACK_COLOR] = color
        }
    }

    suspend fun updateLastSessionSetup(seatCount: Int, assignments: Map<Int, Player>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SEAT_COUNT] = seatCount
            val assignmentSet = assignments.map { (index, player) ->
                "$index:${player.name}"
            }.toSet()
            preferences[PreferencesKeys.LAST_PLAYER_ASSIGNMENTS] = assignmentSet
        }
    }
}