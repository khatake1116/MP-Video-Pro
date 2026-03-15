package com.mpvideopro.player.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mpvideoplayer.player.AudioTrackInfo
import com.mpvideoplayer.player.SubtitleTrackInfo

/**
 * Dialog for selecting audio tracks
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioTrackSelectionDialog(
    audioTracks: List<AudioTrackInfo>,
    currentTrack: AudioTrackInfo?,
    onTrackSelected: (AudioTrackInfo) -> Unit,
    onDismiss: () -> Unit,
    onDisable: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Audio Track",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.height(300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(audioTracks) { track ->
                    AudioTrackItem(
                        track = track,
                        isSelected = track == currentTrack,
                        onSelected = { 
                            onTrackSelected(track)
                            onDismiss()
                        }
                    )
                }
                
                item {
                    // Option to disable (if applicable)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                onDisable()
                                onDismiss()
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeOff,
                            contentDescription = "Disable Audio",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Disable Audio",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Dialog for selecting subtitle tracks
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubtitleTrackSelectionDialog(
    subtitleTracks: List<SubtitleTrackInfo>,
    currentTrack: SubtitleTrackInfo?,
    onTrackSelected: (SubtitleTrackInfo) -> Unit,
    onDismiss: () -> Unit,
    onDisable: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Subtitle Track",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.height(300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    // Option to disable subtitles
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                onDisable()
                                onDismiss()
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SubtitlesOff,
                            contentDescription = "Disable Subtitles",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Disable Subtitles",
                            fontWeight = FontWeight.Medium
                        )
                        if (currentTrack == null) {
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                items(subtitleTracks) { track ->
                    SubtitleTrackItem(
                        track = track,
                        isSelected = track == currentTrack,
                        onSelected = { 
                            onTrackSelected(track)
                            onDismiss()
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AudioTrackItem(
    track: AudioTrackInfo,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.VolumeUp,
            contentDescription = "Audio Track",
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.label,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Language: ${track.language.uppercase()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SubtitleTrackItem(
    track: SubtitleTrackInfo,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Subtitles,
            contentDescription = "Subtitle Track",
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.label,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Language: ${track.language.uppercase()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
