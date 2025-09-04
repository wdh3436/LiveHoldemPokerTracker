package com.example.liveholdempokertracker.di

import android.content.Context
import androidx.room.Room
import com.example.liveholdempokertracker.data.AppDatabase
import com.example.liveholdempokertracker.data.PlayerProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "live_holdem_poker_tracker.db"
        ).build()
    }

    @Provides
    @Singleton
    fun providePlayerProfileDao(appDatabase: AppDatabase): PlayerProfileDao {
        return appDatabase.playerProfileDao()
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context): com.example.liveholdempokertracker.data.SettingsRepository {
        return com.example.liveholdempokertracker.data.SettingsRepository(context)
    }
}
