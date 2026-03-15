package com.mpvideopro.player.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mpvideopro.player.viewmodel.SettingsViewModel

/**
 * Settings screen for configuring app preferences.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadSettings()
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Settings") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SettingsSection(title = "Playback") {
                    // Background playback
                    SettingsSwitchItem(
                        title = "Background Playback",
                        subtitle = "Continue playing audio when app is in background",
                        checked = settings.backgroundPlayback,
                        onCheckedChange = viewModel::setBackgroundPlayback
                    )
                    
                    // Picture-in-Picture
                    SettingsSwitchItem(
                        title = "Picture-in-Picture",
                        subtitle = "Show video in a small window when switching apps",
                        checked = settings.pipMode,
                        onCheckedChange = viewModel::setPipMode
                    )
                    
                    // Hardware decoding
                    SettingsSwitchItem(
                        title = "Hardware Decoding",
                        subtitle = "Use hardware acceleration for better performance",
                        checked = settings.hardwareDecoding,
                        onCheckedChange = viewModel::setHardwareDecoding
                    )
                    
                    // Resume playback
                    SettingsSwitchItem(
                        title = "Resume Playback",
                        subtitle = "Continue from where you left off",
                        checked = settings.resumePlayback,
                        onCheckedChange = viewModel::setResumePlayback
                    )
                }
            }
            
            item {
                SettingsSection(title = "Interface") {
                    // Dark mode
                    SettingsSwitchItem(
                        title = "Dark Mode",
                        subtitle = "Use dark theme",
                        checked = settings.darkMode,
                        onCheckedChange = viewModel::setDarkMode
                    )
                    
                    // Auto-hide controls
                    SettingsSwitchItem(
                        title = "Auto-hide Controls",
                        subtitle = "Hide player controls automatically during playback",
                        checked = settings.autoHideControls,
                        onCheckedChange = viewModel::setAutoHideControls
                    )
                }
            }
            
            item {
                SettingsSection(title = "Gestures") {
                    // Gesture controls
                    SettingsSwitchItem(
                        title = "Gesture Controls",
                        subtitle = "Enable swipe gestures for brightness, volume, and seek",
                        checked = settings.gestureControls,
                        onCheckedChange = viewModel::setGestureControls
                    )
                    
                    // Double tap to play/pause
                    SettingsSwitchItem(
                        title = "Double Tap to Play/Pause",
                        subtitle = "Double tap on video to toggle playback",
                        checked = settings.doubleTapPlayPause,
                        onCheckedChange = viewModel::setDoubleTapPlayPause
                    )
                }
            }
            
            item {
                SettingsSection(title = "About") {
                    SettingsItem(
                        title = "Version",
                        subtitle = "1.0.0",
                        onClick = { }
                    )
                    
                    SettingsItem(
                        title = "About MP Video Pro",
                        subtitle = "A powerful, ad-free video player",
                        onClick = { }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
