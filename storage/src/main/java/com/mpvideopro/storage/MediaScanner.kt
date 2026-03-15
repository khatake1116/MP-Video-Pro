package com.mpvideopro.storage

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Media scanner for finding video files on the device.
 * Supports Android 10+ scoped storage and legacy storage for older versions.
 */
@Singleton
class MediaScanner @Inject constructor(
    private val context: Context
) {
    
    // Supported video formats
    private val supportedFormats = setOf(
        "mp4", "mkv", "avi", "mov", "flv", "webm", "m4v", "3gp", "wmv", "rmvb"
    )
    
    /**
     * Scan for all video files on the device
     */
    fun scanForVideos(): Flow<List<VideoFile>> = flow {
        val videos = mutableListOf<VideoFile>()
        
        try {
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }
            
            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.DATE_MODIFIED,
                MediaStore.Video.Media.RESOLUTION,
                MediaStore.Video.Media.MIME_TYPE
            )
            
            val selection = "${MediaStore.Video.Media.MIME_TYPE} LIKE ?"
            val selectionArgs = arrayOf("video/%")
            val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"
            
            context.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)
                val resolutionColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.RESOLUTION)
                val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
                
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn) ?: continue
                    val duration = cursor.getLong(durationColumn)
                    val size = cursor.getLong(sizeColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn) * 1000 // Convert to milliseconds
                    val dateModified = cursor.getLong(dateModifiedColumn) * 1000
                    val resolution = cursor.getString(resolutionColumn)
                    val mimeType = cursor.getString(mimeTypeColumn) ?: continue
                    
                    // Check if format is supported
                    val extension = name.substringAfterLast('.', "").lowercase()
                    if (extension !in supportedFormats) continue
                    
                    val contentUri = ContentUris.withAppendedId(collection, id)
                    
                    // Parse resolution if available
                    val (width, height) = parseResolution(resolution)
                    
                    val videoFile = VideoFile(
                        id = id,
                        uri = contentUri,
                        name = name,
                        displayName = name.substringBeforeLast('.'),
                        extension = extension,
                        mimeType = mimeType,
                        size = size,
                        duration = duration,
                        width = width,
                        height = height,
                        dateAdded = dateAdded,
                        dateModified = dateModified,
                        path = contentUri.toString()
                    )
                    
                    videos.add(videoFile)
                }
            }
        } catch (e: Exception) {
            // Log error and continue with empty list
            e.printStackTrace()
        }
        
        emit(videos)
    }.flowOn(Dispatchers.IO)
    
    /**
     * Scan for recently played videos based on date modified
     */
    fun scanRecentlyPlayed(days: Int = 7): Flow<List<VideoFile>> = flow {
        val videos = mutableListOf<VideoFile>()
        val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        
        try {
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }
            
            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.DATE_MODIFIED,
                MediaStore.Video.Media.RESOLUTION,
                MediaStore.Video.Media.MIME_TYPE
            )
            
            val selection = "${MediaStore.Video.Media.MIME_TYPE} LIKE ? AND ${MediaStore.Video.Media.DATE_MODIFIED} > ?"
            val selectionArgs = arrayOf("video/%", (cutoffTime / 1000).toString())
            val sortOrder = "${MediaStore.Video.Media.DATE_MODIFIED} DESC"
            
            context.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)
                val resolutionColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.RESOLUTION)
                val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
                
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn) ?: continue
                    val duration = cursor.getLong(durationColumn)
                    val size = cursor.getLong(sizeColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn) * 1000
                    val dateModified = cursor.getLong(dateModifiedColumn) * 1000
                    val resolution = cursor.getString(resolutionColumn)
                    val mimeType = cursor.getString(mimeTypeColumn) ?: continue
                    
                    val extension = name.substringAfterLast('.', "").lowercase()
                    if (extension !in supportedFormats) continue
                    
                    val contentUri = ContentUris.withAppendedId(collection, id)
                    val (width, height) = parseResolution(resolution)
                    
                    val videoFile = VideoFile(
                        id = id,
                        uri = contentUri,
                        name = name,
                        displayName = name.substringBeforeLast('.'),
                        extension = extension,
                        mimeType = mimeType,
                        size = size,
                        duration = duration,
                        width = width,
                        height = height,
                        dateAdded = dateAdded,
                        dateModified = dateModified,
                        path = contentUri.toString()
                    )
                    
                    videos.add(videoFile)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        emit(videos)
    }.flowOn(Dispatchers.IO)
    
    /**
     * Get a specific video file by its URI
     */
    suspend fun getVideoFile(uri: Uri): VideoFile? = withContext(Dispatchers.IO) {
        try {
            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.DATE_MODIFIED,
                MediaStore.Video.Media.RESOLUTION,
                MediaStore.Video.Media.MIME_TYPE
            )
            
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                    val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                    val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                    val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                    val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                    val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)
                    val resolutionColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.RESOLUTION)
                    val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
                    
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn) ?: return@withContext null
                    val duration = cursor.getLong(durationColumn)
                    val size = cursor.getLong(sizeColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn) * 1000
                    val dateModified = cursor.getLong(dateModifiedColumn) * 1000
                    val resolution = cursor.getString(resolutionColumn)
                    val mimeType = cursor.getString(mimeTypeColumn) ?: return@withContext null
                    
                    val extension = name.substringAfterLast('.', "").lowercase()
                    if (extension !in supportedFormats) return@withContext null
                    
                    val (width, height) = parseResolution(resolution)
                    
                    VideoFile(
                        id = id,
                        uri = uri,
                        name = name,
                        displayName = name.substringBeforeLast('.'),
                        extension = extension,
                        mimeType = mimeType,
                        size = size,
                        duration = duration,
                        width = width,
                        height = height,
                        dateAdded = dateAdded,
                        dateModified = dateModified,
                        path = uri.toString()
                    )
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Parse resolution string into width and height
     */
    private fun parseResolution(resolution: String?): Pair<Int, Int> {
        if (resolution.isNullOrBlank()) return Pair(0, 0)
        
        return try {
            val parts = resolution.split("x", "×")
            if (parts.size == 2) {
                val width = parts[0].trim().toIntOrNull() ?: 0
                val height = parts[1].trim().toIntOrNull() ?: 0
                Pair(width, height)
            } else {
                Pair(0, 0)
            }
        } catch (e: Exception) {
            Pair(0, 0)
        }
    }
    
    /**
     * Check if a file format is supported
     */
    fun isFormatSupported(fileName: String): Boolean {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return extension in supportedFormats
    }
}
