package com.mpvideopro.player.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpvideopro.storage.database.PlaybackHistoryDao
import com.mpvideopro.storage.database.PlaybackHistoryWithVideoInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the recently played screen.
 * Loads and manages recently played videos with playback progress.
 */
@HiltViewModel
class RecentlyPlayedViewModel @Inject constructor(
    private val playbackHistoryDao: PlaybackHistoryDao
) : ViewModel() {
    
    private val _recentlyPlayed = MutableStateFlow<List<PlaybackHistoryWithVideoInfo>>(emptyList())
    val recentlyPlayed: StateFlow<List<PlaybackHistoryWithVideoInfo>> = _recentlyPlayed.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    /**
     * Load recently played videos from the last 30 days
     */
    fun loadRecentlyPlayed() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val cutoffTime = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
                playbackHistoryDao.getRecentlyPlayed(cutoffTime).collect { videos ->
                    _recentlyPlayed.value = videos
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _recentlyPlayed.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
