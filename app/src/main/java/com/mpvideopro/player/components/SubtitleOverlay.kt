package com.mpvideopro.player.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpvideoplayer.subtitle.SubtitleCue

/**
 * Subtitle overlay component for displaying subtitles over video.
 * Supports multiple subtitle formats and customizable styling.
 */
@Composable
fun SubtitleOverlay(
    subtitleCue: SubtitleCue?,
    modifier: Modifier = Modifier,
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge.copy(
        color = Color.White,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Center
    ),
    backgroundColor: Color = Color.Black.copy(alpha = 0.7f),
    padding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 80.dp), // Leave space for controls
        contentAlignment = Alignment.BottomCenter
    ) {
        subtitleCue?.let { cue ->
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(backgroundColor),
                color = Color.Transparent
            ) {
                Text(
                    text = cue.text,
                    style = textStyle,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

/**
 * Advanced subtitle overlay with multiple lines support
 */
@Composable
fun AdvancedSubtitleOverlay(
    subtitleCues: List<SubtitleCue>,
    modifier: Modifier = Modifier,
    maxLines: Int = 3,
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge.copy(
        color = Color.White,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Center
    ),
    backgroundColor: Color = Color.Black.copy(alpha = 0.7f)
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        if (subtitleCues.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(backgroundColor)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                subtitleCues.take(maxLines).forEach { cue ->
                    Text(
                        text = cue.text,
                        style = textStyle,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}
