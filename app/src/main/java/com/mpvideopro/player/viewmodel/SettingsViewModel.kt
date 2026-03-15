package com.mpvideopro.player.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpvideopro.storage.database.AppSettingsDao
import com.mpvideopro.storage.database.AppSettingsEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing app settings and preferences.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSettingsDao: AppSettingsDao
) : ViewModel() {
    
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()
    
    init {
        loadSettings()
    }
    
    /**
     * Load settings from database
     */
    fun loadSettings() {
        viewModelScope.launch {
            try {
                appSettingsDao.getSettings().collect { entity ->
                    _settings.value = entity.toAppSettings()
                }
            } catch (e: Exception) {
                // Use default settings if database is empty
                _settings.value = AppSettings()
            }
        }
    }
    
    /**
     * Update background playback setting
     */
    fun setBackgroundPlayback(enabled: Boolean) {
        updateSettings { it.copy(backgroundPlayback = enabled) }
    }
    
    /**
     * Update PiP mode setting
     */
    fun setPipMode(enabled: Boolean) {
        updateSettings { it.copy(pipMode = enabled) }
    }
    
    /**
     * Update hardware decoding setting
     */
    fun setHardwareDecoding(enabled: Boolean) {
        updateSettings { it.copy(hardwareDecoding = enabled) }
    }
    
    /**
     * Update resume playback setting
     */
    fun setResumePlayback(enabled: Boolean) {
        updateSettings { it.copy(resumePlayback = enabled) }
    }
    
    /**
     * Update dark mode setting
     */
    fun setDarkMode(enabled: Boolean) {
        updateSettings { it.copy(darkMode = enabled) }
    }
    
    /**
     * Update auto-hide controls setting
     */
    fun setAutoHideControls(enabled: Boolean) {
        updateSettings { it.copy(autoHideControls = enabled) }
    }
    
    /**
     * Update gesture controls setting
     */
    fun setGestureControls(enabled: Boolean) {
        updateSettings { it.copy(gestureControls = enabled) }
    }
    
    /**
     * Update double tap play/pause setting
     */
    fun setDoubleTapPlayPause(enabled: Boolean) {
        updateSettings { it.copy(doubleTapPlayPause = enabled) }
    }
    
    /**
     * Generic method to update settings
     */
    private fun updateSettings(update: (AppSettings) -> AppSettings) {
        val newSettings = update(_settings.value)
        _settings.value = newSettings
        
        viewModelScope.launch {
            appSettingsDao.insertOrUpdate(newSettings.toEntity())
        }
    }
}

/**
 * Data class representing app settings
 */
data class AppSettings(
    val backgroundPlayback: Boolean = true,
    val pipMode: Boolean = true,
    val hardwareDecoding: Boolean = true,
    val resumePlayback: Boolean = true,
    val darkMode: Boolean = true,
    val autoHideControls: Boolean = true,
    val gestureControls: Boolean = true,
    val doubleTapPlayPause: Boolean = true
)

/**
 * Extension functions for converting between AppSettings and AppSettingsEntity
 */
private fun AppSettings.toEntity(): AppSettingsEntity {
    return AppSettingsEntity(
        id = 1,
        backgroundPlayback = backgroundPlayback,
        pipMode = pipMode,
        hardwareDecoding = hardwareDecoding,
        resumePlayback = resumePlayback,
        darkMode = darkMode,
        autoHideControls = autoHideControls,
        gestureControls = gestureControls,
        doubleTapPlayPause = doubleTapPlayPause
    )
}

private fun AppSettingsEntity.toAppSettings(): AppSettings {
    return AppSettings(
        backgroundPlayback = backgroundPlayback,
        pipMode = pipMode,
        hardwareDecoding = hardwareDecoding,
        resumePlayback = resumePlayback,
        darkMode = darkMode,
        autoHideControls = autoHideControls,
        gestureControls = gestureControls,
        doubleTapPlayPause = doubleTapPlayPause
    )
}
