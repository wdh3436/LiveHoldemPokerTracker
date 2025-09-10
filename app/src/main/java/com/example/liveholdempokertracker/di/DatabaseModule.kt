package com.example.liveholdempokertracker.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `active_session` (`id` INTEGER NOT NULL, `gameStateJson` TEXT NOT NULL, PRIMARY KEY(`id`))")
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "live_holdem_poker_tracker.db"
        )
        .addMigrations(MIGRATION_1_2)
        .build()
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
