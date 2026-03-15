package com.mpvideopro.storage.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for app settings.
 */
@Dao
interface AppSettingsDao {
    
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<AppSettingsEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: AppSettingsEntity)
    
    @Query("DELETE FROM app_settings")
    suspend fun clearSettings()
}

/**
 * Entity for storing app settings
 */
@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val backgroundPlayback: Boolean = true,
    val pipMode: Boolean = true,
    val hardwareDecoding: Boolean = true,
    val resumePlayback: Boolean = true,
    val darkMode: Boolean = true,
    val autoHideControls: Boolean = true,
    val gestureControls: Boolean = true,
    val doubleTapPlayPause: Boolean = true
)
