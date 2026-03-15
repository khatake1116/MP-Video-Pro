package com.mpvideopro.player.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Custom player controls with Material Design 3 styling.
 * Provides play/pause, seek bar, speed control, and other player functions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerControls(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    isLoading: Boolean,
    playbackSpeed: Float,
    isFullscreen: Boolean,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSkipForward: () -> Unit,
    onSkipBackward: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onFullscreen: () -> Unit,
    onLockScreen: () -> Unit,
    onBack: () -> Unit
) {
    Box(modifier = modifier) {
        // Top controls
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { -it }),
            exit = slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            TopControls(
                onBack = onBack,
                onLockScreen = onLockScreen
            )
        }
        
        // Center controls
        CenterControls(
            isPlaying = isPlaying,
            isLoading = isLoading,
            onPlayPause = onPlayPause,
            onSkipForward = onSkipForward,
            onSkipBackward = onSkipBackward,
            modifier = Modifier.align(Alignment.Center)
        )
        
        // Bottom controls
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            BottomControls(
                currentPosition = currentPosition,
                duration = duration,
                playbackSpeed = playbackSpeed,
                isFullscreen = isFullscreen,
                onSeek = onSeek,
                onSpeedChange = onSpeedChange,
                onFullscreen = onFullscreen
            )
        }
    }
}

@Composable
private fun TopControls(
    onBack: () -> Unit,
    onLockScreen: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.Black.copy(alpha = 0.5f),
                RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        
        IconButton(
            onClick = onLockScreen,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Lock Screen",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun CenterControls(
    isPlaying: Boolean,
    isLoading: Boolean,
    onPlayPause: () -> Unit,
    onSkipForward: () -> Unit,
    onSkipBackward: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Skip backward button
        IconButton(
            onClick = onSkipBackward,
            modifier = Modifier
                .size(56.dp)
                .background(
                    Color.Black.copy(alpha = 0.5f),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Replay10,
                contentDescription = "Skip Backward",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        
        // Play/Pause button
        IconButton(
            onClick = onPlayPause,
            modifier = Modifier
                .size(80.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    CircleShape
                )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = Color.White,
                    strokeWidth = 3.dp
                )
            } else {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        
        // Skip forward button
        IconButton(
            onClick = onSkipForward,
            modifier = Modifier
                .size(56.dp)
                .background(
                    Color.Black.copy(alpha = 0.5f),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Forward10,
                contentDescription = "Skip Forward",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun BottomControls(
    currentPosition: Long,
    duration: Long,
    playbackSpeed: Float,
    isFullscreen: Boolean,
    onSeek: (Long) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onFullscreen: () -> Unit
) {
    var showSpeedMenu by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.Black.copy(alpha = 0.5f),
                RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Progress bar
        PlayerSeekBar(
            currentPosition = currentPosition,
            duration = duration,
            onSeek = onSeek,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Time and controls row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time display
            Text(
                text = "${formatTime(currentPosition)} / ${formatTime(duration)}",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            
            // Control buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Playback speed
                Box {
                    IconButton(
                        onClick = { showSpeedMenu = !showSpeedMenu }
                    ) {
                        Text(
                            text = "${playbackSpeed}x",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showSpeedMenu,
                        onDismissRequest = { showSpeedMenu = false }
                    ) {
                        listOf(0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f).forEach { speed ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${speed}x",
                                        color = if (speed == playbackSpeed) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            Color.Unspecified
                                        }
                                    )
                                },
                                onClick = {
                                    onSpeedChange(speed)
                                    showSpeedMenu = false
                                }
                            )
                        }
                    }
                }
                
                // Fullscreen button
                IconButton(
                    onClick = onFullscreen
                ) {
                    Icon(
                        imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = if (isFullscreen) "Exit Fullscreen" else "Fullscreen",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerSeekBar(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragPosition by remember { mutableLongStateOf(currentPosition) }
    
    val progress = if (duration > 0) {
        if (isDragging) dragPosition.toFloat() / duration else currentPosition.toFloat() / duration
    } else {
        0f
    }
    
    Box(
        modifier = modifier.height(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Slider(
            value = progress.coerceIn(0f, 1f),
            onValueChange = { value ->
                if (isDragging) {
                    dragPosition = (value * duration).toLong()
                }
            },
            onValueChangeFinished = {
                if (isDragging) {
                    onSeek(dragPosition)
                    isDragging = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            )
        )
    }
}

private fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}
