package com.mpvideopro.storage.model

import android.net.Uri

/**
 * Data class representing a video file in the storage system.
 * Contains all metadata about a video file including URI, duration, size, etc.
 */
data class VideoFile(
    val id: Long,
    val uri: Uri,
    val name: String,           // Full filename with extension
    val displayName: String,     // Filename without extension
    val extension: String,      // File extension (e.g., "mp4", "mkv")
    val mimeType: String,        // MIME type (e.g., "video/mp4")
    val size: Long,             // File size in bytes
    val duration: Long,         // Duration in milliseconds
    val width: Int,             // Video width in pixels
    val height: Int,            // Video height in pixels
    val dateAdded: Long,         // Date added to device (timestamp)
    val dateModified: Long,     // Last modified date (timestamp)
    val path: String            // File path or URI string
) {
    /**
     * Check if the video has a known resolution
     */
    fun hasResolution(): Boolean = width > 0 && height > 0
    
    /**
     * Get the aspect ratio of the video
     */
    fun getAspectRatio(): Float {
        return if (height > 0) width.toFloat() / height else 0f
    }
    
    /**
     * Check if the video is in portrait orientation
     */
    fun isPortrait(): Boolean = height > width
    
    /**
     * Check if the video is in landscape orientation
     */
    fun isLandscape(): Boolean = width > height
    
    /**
     * Get formatted duration string (e.g., "1:23:45")
     */
    fun getFormattedDuration(): String {
        val totalSeconds = duration / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }
    
    /**
     * Get formatted file size string (e.g., "1.5 GB")
     */
    fun getFormattedSize(): String {
        val kb = size / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        
        return when {
            gb >= 1 -> String.format("%.1f GB", gb)
            mb >= 1 -> String.format("%.1f MB", mb)
            kb >= 1 -> String.format("%.1f KB", kb)
            else -> String.format("%d B", size)
        }
    }
    
    /**
     * Get resolution string (e.g., "1920x1080")
     */
    fun getResolutionString(): String {
        return if (hasResolution()) "${width}x${height}" else "Unknown"
    }
    
    /**
     * Check if this is a high definition video (720p or higher)
     */
    fun isHD(): Boolean = height >= 720
    
    /**
     * Check if this is a 4K video
     */
    fun is4K(): Boolean = height >= 2160
    
    /**
     * Get video quality label
     */
    fun getQualityLabel(): String {
        return when {
            height >= 2160 -> "4K"
            height >= 1440 -> "2K"
            height >= 1080 -> "1080p"
            height >= 720 -> "720p"
            height >= 480 -> "480p"
            height >= 360 -> "360p"
            else -> "SD"
        }
    }
}
