package com.mpvideopro.player.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import com.mpvideopro.player.gesture.GestureEvent
import com.mpvideopro.player.ui.components.PlayerControls
import com.mpvideopro.player.ui.components.VideoPlayerView
import com.mpvideopro.player.viewmodel.PlayerViewModel

/**
 * Main player screen with gesture controls and video playback.
 * Provides MX Player-like functionality with modern Material Design 3.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    videoUri: String,
    onNavigateBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    
    // Player state
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isFullscreen by viewModel.isFullscreen.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    val isScreenLocked by viewModel.isScreenLocked.collectAsState()
    val zoomLevel by viewModel.zoomLevel.collectAsState()
    
    // UI state
    var showControls by remember { mutableStateOf(true) }
    var showBrightnessIndicator by remember { mutableStateOf(false) }
    var showVolumeIndicator by remember { mutableStateOf(false) }
    var showSeekIndicator by remember { mutableStateOf(false) }
    var brightnessValue by remember { mutableFloatStateOf(0f) }
    var volumeValue by remember { mutableFloatStateOf(0f) }
    var seekValue by remember { mutableLongStateOf(0L) }
    
    // Initialize player with video URI
    LaunchedEffect(videoUri) {
        viewModel.initializePlayer(Uri.parse(videoUri))
    }
    
    // Handle gesture events
    LaunchedEffect(Unit) {
        viewModel.gestureEvents.collect { event ->
            when (event) {
                is GestureEvent.Tap -> {
                    showControls = !showControls
                }
                is GestureEvent.DoubleTap -> {
                    viewModel.togglePlayPause()
                }
                is GestureEvent.LongPress -> {
                    viewModel.setTemporarySpeed(2.0f)
                }
                is GestureEvent.ScreenLocked -> {
                    showControls = false
                }
                is GestureEvent.ScreenUnlocked -> {
                    showControls = true
                }
                is GestureEvent.BrightnessChanged -> {
                    brightnessValue = event.brightness
                    showBrightnessIndicator = true
                }
                is GestureEvent.VolumeChanged -> {
                    volumeValue = event.volume
                    showVolumeIndicator = true
                }
                is GestureEvent.SeekChanged -> {
                    seekValue = event.position
                    showSeekIndicator = true
                }
                is GestureEvent.ZoomChanged -> {
                    // Zoom is handled by the video player view
                }
            }
        }
    }
    
    // Hide indicators after delay
    LaunchedEffect(showBrightnessIndicator) {
        if (showBrightnessIndicator) {
            kotlinx.coroutines.delay(1000)
            showBrightnessIndicator = false
        }
    }
    
    LaunchedEffect(showVolumeIndicator) {
        if (showVolumeIndicator) {
            kotlinx.coroutines.delay(1000)
            showVolumeIndicator = false
        }
    }
    
    LaunchedEffect(showSeekIndicator) {
        if (showSeekIndicator) {
            kotlinx.coroutines.delay(1000)
            showSeekIndicator = false
        }
    }
    
    // Handle back press
    BackHandler {
        if (isFullscreen) {
            viewModel.toggleFullscreen()
        } else {
            onNavigateBack()
        }
    }
    
    // Auto-hide controls
    LaunchedEffect(showControls) {
        if (showControls) {
            kotlinx.coroutines.delay(3000)
            showControls = false
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        var urlInput by remember { mutableStateOf("https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4") }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                label = { Text("Stream URL") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )

            Button(onClick = {
                if (urlInput.isNotBlank()) {
                    viewModel.initializePlayer(Uri.parse(urlInput))
                }
            }) {
                Text("Play URL")
            }
        }
        // Video player view
        VideoPlayerView(
            modifier = Modifier.fillMaxSize(),
            viewModel = viewModel,
            zoomLevel = zoomLevel,
            onGesture = { event ->
                viewModel.handleGesture(event)
            }
        )
        
        // Gesture indicators
        AnimatedVisibility(
            visible = showBrightnessIndicator,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            GestureIndicator(
                modifier = Modifier.align(Alignment.CenterStart),
                icon = Icons.Default.BrightnessHigh,
                value = brightnessValue,
                maxValue = 1f
            )
        }
        
        AnimatedVisibility(
            visible = showVolumeIndicator,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            GestureIndicator(
                modifier = Modifier.align(Alignment.CenterEnd),
                icon = Icons.Default.VolumeUp,
                value = volumeValue,
                maxValue = 1f
            )
        }
        
        AnimatedVisibility(
            visible = showSeekIndicator,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            SeekIndicator(
                modifier = Modifier.align(Alignment.Center),
                position = seekValue,
                duration = duration
            )
        }
        
        // Player controls
        AnimatedVisibility(
            visible = showControls && !isScreenLocked,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            PlayerControls(
                modifier = Modifier.fillMaxSize(),
                isPlaying = isPlaying,
                currentPosition = currentPosition,
                duration = duration,
                isLoading = isLoading,
                playbackSpeed = playbackSpeed,
                isFullscreen = isFullscreen,
                onPlayPause = { viewModel.togglePlayPause() },
                onSeek = { viewModel.seekTo(it) },
                onSkipForward = { viewModel.skipForward() },
                onSkipBackward = { viewModel.skipBackward() },
                onSpeedChange = { viewModel.setPlaybackSpeed(it) },
                onFullscreen = { viewModel.toggleFullscreen() },
                onLockScreen = { viewModel.toggleScreenLock() },
                onBack = onNavigateBack
            )
        }
        
        // Lock screen indicator
        AnimatedVisibility(
            visible = isScreenLocked,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Screen Locked",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Screen Locked",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Double tap center to unlock",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun GestureIndicator(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: Float,
    maxValue: Float
) {
    Box(
        modifier = modifier
            .padding(24.dp)
            .size(80.dp)
            .background(
                Color.Black.copy(alpha = 0.7f),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${(value / maxValue * 100).toInt()}%",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SeekIndicator(
    modifier: Modifier = Modifier,
    position: Long,
    duration: Long
) {
    Box(
        modifier = modifier
            .padding(24.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = formatTime(position),
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
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
