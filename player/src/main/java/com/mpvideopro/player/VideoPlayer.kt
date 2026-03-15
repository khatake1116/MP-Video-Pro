package com.mpvideopro.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Core video player class that wraps ExoPlayer functionality.
 * Provides a clean API for video playback with state management.
 */
@Singleton
class VideoPlayer @Inject constructor(
    private val context: Context
) {
    private var exoPlayer: ExoPlayer? = null
    
    // Player state flows
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()
    
    private val _isFullscreen = MutableStateFlow(false)
    val isFullscreen: StateFlow<Boolean> = _isFullscreen.asStateFlow()
    
    private val _tracks = MutableStateFlow<Tracks?>(null)
    val tracks: StateFlow<Tracks?> = _tracks.asStateFlow()
    
    private val _playerError = MutableStateFlow<PlaybackException?>(null)
    val playerError: StateFlow<PlaybackException?> = _playerError.asStateFlow()
    
    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }
        
        override fun onPlaybackStateChanged(playbackState: Int) {
            _isLoading.value = playbackState == Player.STATE_BUFFERING
        }
        
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            mediaItem?.let {
                _duration.value = exoPlayer?.duration ?: 0L
            }
        }
        
        override fun onTracksChanged(tracks: Tracks) {
            _tracks.value = tracks
        }
        
        override fun onPlayerError(error: PlaybackException) {
            _playerError.value = error
        }
    }
    
    /**
     * Initialize the player with optional configuration
     */
    fun initialize() {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                addListener(playerListener)
            }
        }
    }
    
    /**
     * Prepare and play a video from the given URI
     */
    fun playVideo(uri: Uri) {
        val player = exoPlayer ?: return
        
        val mediaSource = createMediaSource(uri)
        player.setMediaSource(mediaSource)
        player.prepare()
        player.play()
    }
    
    /**
     * Create a MediaSource from the given URI
     */
    private fun createMediaSource(uri: Uri): MediaSource {
        val dataSourceFactory = DefaultDataSource.Factory(context)
        return ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(uri))
    }
    
    /**
     * Toggle play/pause state
     */
    fun togglePlayPause() {
        val player = exoPlayer ?: return
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }
    
    /**
     * Seek to the specified position
     */
    fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
    }
    
    /**
     * Set playback speed (0.25x to 2.0x)
     */
    fun setPlaybackSpeed(speed: Float) {
        exoPlayer?.setPlaybackSpeed(speed.coerceIn(0.25f, 2.0f))
        _playbackSpeed.value = speed
    }
    
    /**
     * Skip forward by the specified milliseconds
     */
    fun skipForward(milliseconds: Long = 10000) {
        val player = exoPlayer ?: return
        val newPosition = (player.currentPosition + milliseconds).coerceAtMost(player.duration)
        player.seekTo(newPosition)
    }
    
    /**
     * Skip backward by the specified milliseconds
     */
    fun skipBackward(milliseconds: Long = 10000) {
        val player = exoPlayer ?: return
        val newPosition = (player.currentPosition - milliseconds).coerceAtLeast(0)
        player.seekTo(newPosition)
    }
    
    /**
     * Select audio track by group index and track index
     */
    fun selectAudioTrack(groupIndex: Int, trackIndex: Int) {
        val player = exoPlayer ?: return
        val parameters = player.trackSelectionParameters
            .buildUpon()
            .setOverrideForType(
                androidx.media3.common.C.TrackType.AUDIO,
                groupIndex,
                trackIndex
            )
            .build()
        player.trackSelectionParameters = parameters
    }
    
    /**
     * Select subtitle track by group index and track index
     */
    fun selectSubtitleTrack(groupIndex: Int, trackIndex: Int) {
        val player = exoPlayer ?: return
        val parameters = player.trackSelectionParameters
            .buildUpon()
            .setOverrideForType(
                androidx.media3.common.C.TrackType.TEXT,
                groupIndex,
                trackIndex
            )
            .build()
        player.trackSelectionParameters = parameters
    }
    
    /**
     * Disable subtitles
     */
    fun disableSubtitles() {
        val player = exoPlayer ?: return
        val parameters = player.trackSelectionParameters
            .buildUpon()
            .setDisabledTrackTypes(setOf(C.TRACK_TYPE_TEXT))
            .build()
        player.trackSelectionParameters = parameters
    }
    
    /**
     * Toggle fullscreen mode
     */
    fun toggleFullscreen() {
        _isFullscreen.value = !_isFullscreen.value
    }
    
    /**
     * Attach this player to a PlayerView
     */
    fun attachToView(playerView: PlayerView) {
        playerView.player = exoPlayer
    }
    
    /**
     * Detach from current PlayerView
     */
    fun detachFromView(playerView: PlayerView) {
        if (playerView.player == exoPlayer) {
            playerView.player = null
        }
    }
    
    /**
     * Update current position (should be called regularly from UI)
     */
    fun updateCurrentPosition() {
        exoPlayer?.let { player ->
            _currentPosition.value = player.currentPosition
        }
    }
    
    /**
     * Get current playback state
     */
    fun getPlaybackState(): Int {
        return exoPlayer?.playbackState ?: Player.STATE_IDLE
    }
    
    /**
     * Check if player is currently playing
     */
    fun isCurrentlyPlaying(): Boolean {
        return exoPlayer?.isPlaying == true
    }

    /**
     * Expose application context for components that need it (e.g., gesture controller).
     */
    fun getContext(): Context = context

    /**
     * Expose the underlying Player instance when needed by UI/helpers.
     */
    fun getExoPlayer(): Player? = exoPlayer
    
    /**
     * Get available audio tracks
     */
    fun getAudioTracks(): List<AudioTrackInfo> {
        val tracks = _tracks.value ?: return emptyList()
        val audioTracks = mutableListOf<AudioTrackInfo>()
        
        tracks.groups.forEachIndexed { groupIndex, trackGroup ->
            if (trackGroup.type == C.TRACK_TYPE_AUDIO && trackGroup.isSelected) {
                trackGroup.forEachIndexed { trackIndex, trackFormat ->
                    audioTracks.add(
                        AudioTrackInfo(
                            groupIndex = groupIndex,
                            trackIndex = trackIndex,
                            label = trackFormat.label ?: "Audio ${trackIndex + 1}",
                            language = trackFormat.language ?: "unknown",
                            isSelected = trackGroup.getSelectedTrackIndex() == trackIndex
                        )
                    )
                }
            }
        }
        
        return audioTracks
    }
    
    /**
     * Get available subtitle tracks
     */
    fun getSubtitleTracks(): List<SubtitleTrackInfo> {
        val tracks = _tracks.value ?: return emptyList()
        val subtitleTracks = mutableListOf<SubtitleTrackInfo>()
        
        tracks.groups.forEachIndexed { groupIndex, trackGroup ->
            if (trackGroup.type == C.TRACK_TYPE_TEXT) {
                trackGroup.forEachIndexed { trackIndex, trackFormat ->
                    subtitleTracks.add(
                        SubtitleTrackInfo(
                            groupIndex = groupIndex,
                            trackIndex = trackIndex,
                            label = trackFormat.label ?: "Subtitle ${trackIndex + 1}",
                            language = trackFormat.language ?: "unknown",
                            isSelected = trackGroup.getSelectedTrackIndex() == trackIndex
                        )
                    )
                }
            }
        }
        
        return subtitleTracks
    }
    
    /**
     * Release the player and clean up resources
     */
    fun release() {
        exoPlayer?.let { player ->
            player.removeListener(playerListener)
            player.release()
            exoPlayer = null
        }
    }
}

/**
 * Data class for audio track information
 */
data class AudioTrackInfo(
    val groupIndex: Int,
    val trackIndex: Int,
    val label: String,
    val language: String,
    val isSelected: Boolean
)

/**
 * Data class for subtitle track information
 */
data class SubtitleTrackInfo(
    val groupIndex: Int,
    val trackIndex: Int,
    val label: String,
    val language: String,
    val isSelected: Boolean
)
