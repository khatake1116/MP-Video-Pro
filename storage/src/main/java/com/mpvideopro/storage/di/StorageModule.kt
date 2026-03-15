package com.mpvideopro.storage.di

import android.content.Context
import androidx.room.Room
import com.mpvideopro.storage.AppDatabase
import com.mpvideopro.storage.AppSettingsDao
import com.mpvideopro.storage.MediaScanner
import com.mpvideopro.storage.PlaybackHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for storage dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object StorageModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "mp_video_pro_database"
        ).build()
    }
    
    @Provides
    fun providePlaybackHistoryDao(database: AppDatabase): PlaybackHistoryDao {
        return database.playbackHistoryDao()
    }
    
    @Provides
    fun provideAppSettingsDao(database: AppDatabase): AppSettingsDao {
        return database.appSettingsDao()
    }
    
    @Provides
    @Singleton
    fun provideMediaScanner(@ApplicationContext context: Context): MediaScanner {
        return MediaScanner(context)
    }
}
