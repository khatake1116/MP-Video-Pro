package com.mpvideopro.player.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpvideopro.storage.MediaScanner
import com.mpvideopro.storage.model.SortOrder
import com.mpvideopro.storage.model.VideoFile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the file browser screen.
 * Manages video loading, sorting, and filtering.
 */
@HiltViewModel
class FileBrowserViewModel @Inject constructor(
    private val mediaScanner: MediaScanner
) : ViewModel() {
    
    private val _videos = MutableStateFlow<List<VideoFile>>(emptyList())
    val videos: StateFlow<List<VideoFile>> = _videos.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _sortOrder = MutableStateFlow(SortOrder.DATE)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()
    
    private val allVideos = MutableStateFlow<List<VideoFile>>(emptyList())
    
    init {
        // Combine all videos with sort order to produce sorted list
        combine(allVideos, sortOrder) { videos, order ->
            sortVideos(videos, order)
        }.onEach { sortedVideos ->
            _videos.value = sortedVideos
        }.launchIn(viewModelScope)
    }
    
    /**
     * Load all videos from device storage
     */
    fun loadVideos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                mediaScanner.scanForVideos().collect { videoList ->
                    allVideos.value = videoList
                }
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Set the sort order for videos
     */
    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }
    
    /**
     * Sort videos according to the specified order
     */
    private fun sortVideos(videos: List<VideoFile>, order: SortOrder): List<VideoFile> {
        return when (order) {
            SortOrder.NAME -> videos.sortedBy { it.displayName.lowercase() }
            SortOrder.DATE -> videos.sortedByDescending { it.dateAdded }
            SortOrder.DURATION -> videos.sortedByDescending { it.duration }
            SortOrder.SIZE -> videos.sortedByDescending { it.size }
        }
    }
}
