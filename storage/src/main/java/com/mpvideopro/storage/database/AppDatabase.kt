package com.mpvideopro.storage.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

/**
 * Main Room database for the application.
 * Contains all database entities and DAOs.
 */
@Database(
    entities = [
        PlaybackHistoryEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun playbackHistoryDao(): PlaybackHistoryDao
    abstract fun appSettingsDao(): AppSettingsDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mp_video_pro_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
