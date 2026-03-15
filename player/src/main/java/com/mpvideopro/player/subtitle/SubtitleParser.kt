package com.mpvideopro.player.subtitle

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.regex.Pattern

/**
 * Parser for various subtitle formats (.srt, .ass, .ssa)
 * Provides subtitle data with timing and text information
 */
class SubtitleParser {
    
    /**
     * Parse subtitle file and return list of subtitle cues
     */
    suspend fun parseSubtitles(uri: Uri): Result<List<SubtitleCue>> = withContext(Dispatchers.IO) {
        try {
            val content = readSubtitleFile(uri)
            val cues = when {
                uri.toString().lowercase().endsWith(".srt") -> parseSRT(content)
                uri.toString().lowercase().endsWith(".ass") -> parseASS(content)
                uri.toString().lowercase().endsWith(".ssa") -> parseSSA(content)
                else -> throw IllegalArgumentException("Unsupported subtitle format")
            }
            Result.success(cues)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Read subtitle file content from URI
     */
    private suspend fun readSubtitleFile(uri: Uri): String = withContext(Dispatchers.IO) {
        // This would need to be implemented with proper content resolver
        // For now, return empty string as placeholder
        ""
    }
    
    /**
     * Parse SRT (SubRip) subtitle format
     * Format:
     * 1
     * 00:00:01,000 --> 00:00:04,000
     * Subtitle text here
     */
    private fun parseSRT(content: String): List<SubtitleCue> {
        val cues = mutableListOf<SubtitleCue>()
        val blocks = content.split("\n\n".toRegex()).filter { it.isNotBlank() }
        
        for (block in blocks) {
            val lines = block.trim().split("\n")
            if (lines.size >= 3) {
                try {
                    val timeLine = lines[1]
                    val timeMatch = timePattern.matcher(timeLine)
                    
                    if (timeMatch.find()) {
                        val startTime = parseTime(timeMatch.group(1)!!)
                        val endTime = parseTime(timeMatch.group(2)!!)
                        val text = lines.drop(2).joinToString("\n").trim()
                        
                        cues.add(
                            SubtitleCue(
                                startTime = startTime,
                                endTime = endTime,
                                text = text,
                                format = SubtitleFormat.SRT
                            )
                        )
                    }
                } catch (e: Exception) {
                    // Skip malformed blocks
                    continue
                }
            }
        }
        
        return cues
    }
    
    /**
     * Parse ASS (Advanced SubStation Alpha) subtitle format
     */
    private fun parseASS(content: String): List<SubtitleCue> {
        val cues = mutableListOf<SubtitleCue>()
        val lines = content.split("\n")
        var inEventsSection = false
        
        for (line in lines) {
            val trimmedLine = line.trim()
            
            if (trimmedLine.equals("[events]", ignoreCase = true)) {
                inEventsSection = true
                continue
            }
            
            if (trimmedLine.startsWith("[") && !trimmedLine.equals("[events]", ignoreCase = true)) {
                inEventsSection = false
                continue
            }
            
            if (inEventsSection && trimmedLine.startsWith("Dialogue:")) {
                try {
                    val cue = parseASSDialogue(trimmedLine)
                    if (cue != null) {
                        cues.add(cue)
                    }
                } catch (e: Exception) {
                    // Skip malformed lines
                    continue
                }
            }
        }
        
        return cues
    }
    
    /**
     * Parse SSA (SubStation Alpha) subtitle format
     */
    private fun parseSSA(content: String): List<SubtitleCue> {
        // SSA is similar to ASS but with slightly different format
        return parseASS(content).map { cue ->
            cue.copy(format = SubtitleFormat.SSA)
        }
    }
    
    /**
     * Parse individual ASS dialogue line
     * Format: Dialogue: Layer,Start,End,Style,Name,MarginL,MarginR,MarginV,Effect,Text
     */
    private fun parseASSDialogue(line: String): SubtitleCue? {
        val parts = line.split(":", limit = 2)
        if (parts.size != 2 || parts[0].trim() != "Dialogue") {
            return null
        }
        
        val fields = parts[1].split(",")
        if (fields.size < 10) {
            return null
        }
        
        val startTime = parseASSTime(fields[1].trim())
        val endTime = parseASSTime(fields[2].trim())
        val text = fields.drop(9).joinToString(",").trim()
            .replace("\\N", "\n") // Handle line breaks in ASS
            .replace("{[^}]*}".toRegex(), "") // Remove ASS formatting tags
        
        return SubtitleCue(
            startTime = startTime,
            endTime = endTime,
            text = text,
            format = SubtitleFormat.ASS
        )
    }
    
    /**
     * Parse time in HH:MM:SS,mmm format (SRT)
     */
    private fun parseTime(timeString: String): Long {
        val parts = timeString.split(":")
        if (parts.size != 3) return 0L
        
        val hours = parts[0].toLongOrNull() ?: 0L
        val minutes = parts[1].toLongOrNull() ?: 0L
        val secondsAndMillis = parts[2].split(",")
        val seconds = secondsAndMillis[0].toLongOrNull() ?: 0L
        val millis = if (secondsAndMillis.size > 1) {
            secondsAndMillis[1].toLongOrNull() ?: 0L
        } else {
            0L
        }
        
        return (hours * 3600 + minutes * 60 + seconds) * 1000 + millis
    }
    
    /**
     * Parse time in H:MM:SS.cc format (ASS)
     */
    private fun parseASSTime(timeString: String): Long {
        val parts = timeString.split(":")
        if (parts.size != 3) return 0L
        
        val hours = parts[0].toLongOrNull() ?: 0L
        val minutes = parts[1].toLongOrNull() ?: 0L
        val secondsAndCentis = parts[2].split(".")
        val seconds = secondsAndCentis[0].toLongOrNull() ?: 0L
        val centis = if (secondsAndCentis.size > 1) {
            secondsAndCentis[1].toLongOrNull() ?: 0L
        } else {
            0L
        }
        
        return (hours * 3600 + minutes * 60 + seconds) * 1000 + centis * 10
    }
    
    companion object {
        // Pattern for matching SRT time format: 00:00:01,000 --> 00:00:04,000
        private val timePattern = Pattern.compile(
            "(\\d{1,2}:\\d{2}:\\d{2},\\d{3})\\s*-->\\s*(\\d{1,2}:\\d{2}:\\d{2},\\d{3})"
        )
    }
}

/**
 * Data class representing a single subtitle cue
 */
data class SubtitleCue(
    val startTime: Long, // Start time in milliseconds
    val endTime: Long,   // End time in milliseconds
    val text: String,    // Subtitle text
    val format: SubtitleFormat
) {
    /**
     * Check if this cue should be displayed at the given time
     */
    fun isActiveAt(timeMs: Long): Boolean {
        return timeMs in startTime..endTime
    }
    
    /**
     * Get the duration of this cue
     */
    val duration: Long
        get() = endTime - startTime
}

/**
 * Enum for supported subtitle formats
 */
enum class SubtitleFormat {
    SRT,
    ASS,
    SSA
}

/**
 * Subtitle track information
 */
data class SubtitleTrack(
    val id: String,
    val name: String,
    val language: String,
    val format: SubtitleFormat,
    val cues: List<SubtitleCue>
) {
    /**
     * Get the subtitle cue that should be displayed at the given time
     */
    fun getCueAt(timeMs: Long): SubtitleCue? {
        return cues.find { it.isActiveAt(timeMs) }
    }
    
    /**
     * Get all cues that should be displayed at the given time
     * (useful for overlapping subtitles)
     */
    fun getCuesAt(timeMs: Long): List<SubtitleCue> {
        return cues.filter { it.isActiveAt(timeMs) }
    }
}
