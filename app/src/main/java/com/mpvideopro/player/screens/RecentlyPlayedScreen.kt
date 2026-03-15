package com.mpvideopro.player.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mpvideopro.player.viewmodel.RecentlyPlayedViewModel

/**
 * Screen showing recently played videos with playback progress.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentlyPlayedScreen(
    onVideoClick: (com.mpvideopro.storage.model.VideoFile) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RecentlyPlayedViewModel = hiltViewModel()
) {
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadRecentlyPlayed()
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Recently Played") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (recentlyPlayed.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No recently played videos",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recentlyPlayed) { item ->
                    RecentlyPlayedItem(
                        videoInfo = item,
                        onClick = { 
                            // Convert to VideoFile and navigate
                            onVideoClick(createVideoFileFromInfo(item))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentlyPlayedItem(
    videoInfo: com.mpvideopro.storage.database.PlaybackHistoryWithVideoInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail placeholder
            Box(
                modifier = Modifier
                    .size(80.dp, 60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (videoInfo.displayName != null) {
                    AsyncImage(
                        model = videoInfo.playbackHistory.videoUri,
                        contentDescription = videoInfo.displayName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Video info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = videoInfo.displayName ?: "Unknown Video",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = videoInfo.getLastPlayedText(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Progress bar
                if (videoInfo.videoDuration != null && videoInfo.videoDuration > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = videoInfo.getCompletionPercentage() / 100f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

private fun createVideoFileFromInfo(
    info: com.mpvideopro.storage.database.PlaybackHistoryWithVideoInfo
): com.mpvideopro.storage.model.VideoFile {
    return com.mpvideopro.storage.model.VideoFile(
        id = 0L, // Placeholder
        uri = android.net.Uri.parse(info.playbackHistory.videoUri),
        name = info.displayName ?: "Unknown",
        displayName = info.displayName ?: "Unknown",
        extension = "mp4", // Placeholder
        mimeType = "video/mp4", // Placeholder
        size = info.size ?: 0L,
        duration = info.videoDuration ?: 0L,
        width = info.width ?: 0,
        height = info.height ?: 0,
        dateAdded = info.playbackHistory.lastPlayed,
        dateModified = info.playbackHistory.lastPlayed,
        path = info.playbackHistory.videoUri
    )
}
