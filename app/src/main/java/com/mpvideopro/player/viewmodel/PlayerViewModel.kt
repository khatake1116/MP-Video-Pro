package com.mpvideopro.player.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpvideopro.player.gesture.GestureController
import com.mpvideopro.player.gesture.GestureEvent
import com.mpvideopro.player.VideoPlayer
import com.mpvideopro.storage.database.PlaybackHistoryDao
import com.mpvideopro.storage.database.PlaybackHistoryEntity
import kotlinx.coroutines.isActive
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the video player screen.
 * Manages player state, gesture handling, and playback history.
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val videoPlayer: VideoPlayer,
    private val playbackHistoryDao: PlaybackHistoryDao
) : ViewModel() {
    
    // Player state flows
    val isPlaying = videoPlayer.isPlaying
    val isLoading = videoPlayer.isLoading
    val currentPosition = videoPlayer.currentPosition
    val duration = videoPlayer.duration
    val playbackSpeed = videoPlayer.playbackSpeed
    val isFullscreen = videoPlayer.isFullscreen
    
    // Gesture controller
    private lateinit var gestureController: GestureController
    val gestureEvents = MutableSharedFlow<GestureEvent>()
    
    // Additional state
    private val _isScreenLocked = MutableStateFlow(false)
    val isScreenLocked = _isScreenLocked.asStateFlow()
    
    private val _zoomLevel = MutableStateFlow(1.0f)
    val zoomLevel = _zoomLevel.asStateFlow()
    
    // Current video URI
    private var currentVideoUri: Uri? = null
    private var lastPositionUpdate = 0L
    
    init {
        // Start position updates
        startPositionUpdates()
    }
    
    /**
     * Initialize the player with a video URI
     */
    fun initializePlayer(uri: Uri) {
        if (currentVideoUri != uri) {
            currentVideoUri = uri
            
            // Initialize video player
            videoPlayer.initialize()
            
            // Initialize gesture controller if underlying player is available
            val exo = videoPlayer.getExoPlayer()
            if (exo != null) {
                gestureController = GestureController(
                    context = videoPlayer.getContext(),
                    player = exo
                )
            }
            
            // Load playback position from history
            loadPlaybackPosition(uri)
            
            // Start playback
            videoPlayer.playVideo(uri)
        }
    }
    
    /**
     * Handle gesture events from the UI
     */
    fun handleGesture(motionEvent: android.view.MotionEvent) {
        if (::gestureController.isInitialized) {
            // This would need to be implemented with proper view passing
            // gestureController.handleTouchEvent(motionEvent, view)
        }
    }
    
    /**
     * Toggle play/pause
     */
    fun togglePlayPause() {
        videoPlayer.togglePlayPause()
    }
    
    /**
     * Seek to specific position
     */
    fun seekTo(position: Long) {
        videoPlayer.seekTo(position)
    }
    
    /**
     * Skip forward by default amount
     */
    fun skipForward(milliseconds: Long = 10000) {
        videoPlayer.skipForward(milliseconds)
    }
    
    /**
     * Skip backward by default amount
     */
    fun skipBackward(milliseconds: Long = 10000) {
        videoPlayer.skipBackward(milliseconds)
    }
    
    /**
     * Set playback speed
     */
    fun setPlaybackSpeed(speed: Float) {
        videoPlayer.setPlaybackSpeed(speed)
    }
    
    /**
     * Set temporary speed (for long press)
     */
    fun setTemporarySpeed(speed: Float) {
        setPlaybackSpeed(speed)
        viewModelScope.launch {
            delay(500) // Reset after 500ms
            setPlaybackSpeed(1.0f)
        }
    }
    
    /**
     * Toggle fullscreen mode
     */
    fun toggleFullscreen() {
        videoPlayer.toggleFullscreen()
    }
    
    /**
     * Toggle screen lock
     */
    fun toggleScreenLock() {
        _isScreenLocked.value = !_isScreenLocked.value
    }
    
    /**
     * Select audio track
     */
    fun selectAudioTrack(groupIndex: Int, trackIndex: Int) {
        videoPlayer.selectAudioTrack(groupIndex, trackIndex)
    }
    
    /**
     * Select subtitle track
     */
    fun selectSubtitleTrack(groupIndex: Int, trackIndex: Int) {
        videoPlayer.selectSubtitleTrack(groupIndex, trackIndex)
    }
    
    /**
     * Disable subtitles
     */
    fun disableSubtitles() {
        videoPlayer.disableSubtitles()
    }
    
    /**
     * Get available audio tracks
     */
    fun getAudioTracks() = videoPlayer.getAudioTracks()
    
    /**
     * Get available subtitle tracks
     */
    fun getSubtitleTracks() = videoPlayer.getSubtitleTracks()
    
    /**
     * Start regular position updates for playback history
     */
    private fun startPositionUpdates() {
        viewModelScope.launch {
            while (isActive) {
                delay(1000) // Update every second

                if (videoPlayer.isCurrentlyPlaying()) {
                    videoPlayer.updateCurrentPosition()
                    savePlaybackPosition()
                }
            }
        }
    }
    
    /**
     * Load playback position from history
     */
    private fun loadPlaybackPosition(uri: Uri) {
        viewModelScope.launch {
            val history = playbackHistoryDao.getPlaybackHistory(uri.toString())
            history?.let {
                if (it.position > 0 && !it.isCompleted) {
                    videoPlayer.seekTo(it.position)
                }
            }
        }
    }
    
    /**
     * Save current playback position to history
     */
    private fun savePlaybackPosition() {
        val uri = currentVideoUri?.toString() ?: return
        val position = currentPosition.value
        val totalDuration = duration.value
        
        // Throttle saves to avoid too frequent database writes
        val now = System.currentTimeMillis()
        if (now - lastPositionUpdate < 5000) return // Save at most every 5 seconds
        lastPositionUpdate = now
        
        viewModelScope.launch {
            val existingHistory = playbackHistoryDao.getPlaybackHistory(uri)
            
            if (existingHistory != null) {
                // Update existing history
                val updatedHistory = existingHistory.copy(
                    position = position,
                    duration = totalDuration,
                    lastPlayed = now,
                    playCount = existingHistory.playCount + 1,
                    isCompleted = position >= totalDuration * 0.95, // Consider completed at 95%
                    watchTime = existingHistory.watchTime + 1000 // Add 1 second
                )
                playbackHistoryDao.insertOrUpdate(updatedHistory)
            } else {
                // Create new history entry
                val newHistory = PlaybackHistoryEntity(
                    videoUri = uri,
                    position = position,
                    duration = totalDuration,
                    lastPlayed = now,
                    playCount = 1,
                    isCompleted = false,
                    watchTime = 1000
                )
                playbackHistoryDao.insertOrUpdate(newHistory)
            }
        }
    }
    
    /**
     * Clean up resources when ViewModel is destroyed
     */
    override fun onCleared() {
        super.onCleared()
        // Save final position
        savePlaybackPosition()
        // Release player
        videoPlayer.release()
    }
}
