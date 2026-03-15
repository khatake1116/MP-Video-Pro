package com.mpvideopro.storage.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for playback history operations.
 * Manages saving and retrieving video playback positions and history.
 */
@Dao
interface PlaybackHistoryDao {
    
    /**
     * Insert or update a playback history entry
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(playbackHistory: PlaybackHistoryEntity)
    
    /**
     * Get playback history for a specific video
     */
    @Query("SELECT * FROM playback_history WHERE video_uri = :videoUri LIMIT 1")
    suspend fun getPlaybackHistory(videoUri: String): PlaybackHistoryEntity?
    
    /**
     * Get all playback history entries ordered by last played
     */
    @Query("SELECT * FROM playback_history ORDER BY last_played DESC")
    fun getAllPlaybackHistory(): Flow<List<PlaybackHistoryEntity>>
    
    /**
     * Get recently played videos (within specified days)
     */
    @Query("SELECT * FROM playback_history WHERE last_played > :cutoffTime ORDER BY last_played DESC")
    fun getRecentlyPlayed(cutoffTime: Long): Flow<List<PlaybackHistoryEntity>>
    
    /**
     * Delete playback history for a specific video
     */
    @Query("DELETE FROM playback_history WHERE video_uri = :videoUri")
    suspend fun deletePlaybackHistory(videoUri: String)
    
    /**
     * Clear all playback history
     */
    @Query("DELETE FROM playback_history")
    suspend fun clearAllPlaybackHistory()
    
    /**
     * Get playback history with video information
     */
    @Query("""
        SELECT ph.*, v.display_name, v.duration, v.size, v.width, v.height 
        FROM playback_history ph 
        LEFT JOIN video_files v ON ph.video_uri = v.path 
        ORDER BY ph.last_played DESC
    """)
    fun getPlaybackHistoryWithVideoInfo(): Flow<List<PlaybackHistoryWithVideoInfo>>
    
    /**
     * Get videos with playback positions
     */
    @Query("""
        SELECT ph.*, v.display_name, v.duration, v.size, v.width, v.height 
        FROM playback_history ph 
        LEFT JOIN video_files v ON ph.video_uri = v.path 
        WHERE ph.position > 0 
        ORDER BY ph.last_played DESC
    """)
    fun getVideosWithPositions(): Flow<List<PlaybackHistoryWithVideoInfo>>
}

/**
 * Entity for storing playback history
 */
@Entity(tableName = "playback_history")
data class PlaybackHistoryEntity(
    @PrimaryKey val videoUri: String,
    val position: Long,           // Last playback position in milliseconds
    val duration: Long,           // Total video duration in milliseconds
    val lastPlayed: Long,         // Timestamp when last played
    val playCount: Int,          // Number of times played
    val isCompleted: Boolean,    // Whether video was watched to completion
    val watchTime: Long          // Total watch time in milliseconds
)

/**
 * Data class for joining playback history with video information
 */
data class PlaybackHistoryWithVideoInfo(
    @Embedded val playbackHistory: PlaybackHistoryEntity,
    val displayName: String?,
    val videoDuration: Long?,
    val size: Long?,
    val width: Int?,
    val height: Int?
) {
    /**
     * Get the completion percentage
     */
    fun getCompletionPercentage(): Float {
        return if (playbackHistory.duration > 0) {
            (playbackHistory.position.toFloat() / playbackHistory.duration) * 100
        } else {
            0f
        }
    }
    
    /**
     * Check if the video is nearly completed (90% or more)
     */
    fun isNearlyCompleted(): Boolean = getCompletionPercentage() >= 90f
    
    /**
     * Get formatted last played date
     */
    fun getLastPlayedText(): String {
        val now = System.currentTimeMillis()
        val diff = now - playbackHistory.lastPlayed
        
        return when {
            diff < 60 * 1000 -> "Just now"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} minutes ago"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
            diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} days ago"
            else -> "More than a week ago"
        }
    }
}
