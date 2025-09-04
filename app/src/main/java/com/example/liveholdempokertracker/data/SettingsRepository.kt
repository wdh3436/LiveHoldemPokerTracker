package com.example.liveholdempokertracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private object PreferencesKeys {
        val TABLE_COLOR = longPreferencesKey("table_color")
    }

    val tableColorFlow: Flow<Long?> = context.dataStore.data
        .map {
            it[PreferencesKeys.TABLE_COLOR]
        }

    suspend fun updateTableColor(color: Long) {
        context.dataStore.edit {
            it[PreferencesKeys.TABLE_COLOR] = color
        }
    }
}