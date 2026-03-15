package com.mpvideopro.player.gesture

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.Player
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.math.abs

/**
 * Gesture controller for video player with MX Player-like functionality.
 * Handles swipe gestures for brightness/volume/seek, pinch to zoom, and tap controls.
 */
class GestureController(
    private val context: Context,
    private val player: Player
) {
    // Gesture detection
    private val gestureDetector: GestureDetector
    private val scaleGestureDetector: ScaleGestureDetector
    
    // State
    var isScreenLocked by mutableStateOf(false)
        private set
    
    // Gesture events
    private val _gestureEvents = MutableSharedFlow<GestureEvent>()
    val gestureEvents: SharedFlow<GestureEvent> = _gestureEvents.asSharedFlow()
    
    // Zoom state
    var zoomLevel by mutableFloatStateOf(1.0f)
        private set
    
    // Gesture tracking
    private var isGesturing = false
    private var gestureType: GestureType? = null
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var initialBrightness = -1f
    private var initialVolume = -1f
    private var initialSeekPosition = -1L
    
    // Screen dimensions
    private var screenWidth = 0
    private var screenHeight = 0
    
    init {
        gestureDetector = GestureDetector(context, PlayerGestureListener())
        scaleGestureDetector = ScaleGestureDetector(context, PlayerScaleListener())
    }
    
    /**
     * Set screen dimensions for gesture calculations
     */
    fun setScreenDimensions(width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
    }
    
    /**
     * Handle touch events from the player view
     */
    fun handleTouchEvent(event: MotionEvent, view: View): Boolean {
        if (isScreenLocked) {
            // Only allow unlock gesture when screen is locked
            return handleUnlockGesture(event)
        }
        
        val scaleResult = scaleGestureDetector.onTouchEvent(event)
        val gestureResult = gestureDetector.onTouchEvent(event)
        
        return scaleResult || gestureResult
    }
    
    /**
     * Handle unlock gesture (long press on center)
     */
    private fun handleUnlockGesture(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialTouchX = event.x
                initialTouchY = event.y
            }
            MotionEvent.ACTION_UP -> {
                val deltaX = abs(event.x - initialTouchX)
                val deltaY = abs(event.y - initialTouchY)
                val centerX = screenWidth / 2f
                val centerY = screenHeight / 2f
                
                // Check if touch was in center area
                if (deltaX < 50 && deltaY < 50 && 
                    abs(event.x - centerX) < 100 && abs(event.y - centerY) < 100) {
                    isScreenLocked = false
                    _gestureEvents.tryEmit(GestureEvent.ScreenUnlocked)
                    return true
                }
            }
        }
        return false
    }
    
    /**
     * Toggle screen lock state
     */
    fun toggleScreenLock() {
        isScreenLocked = !isScreenLocked
        _gestureEvents.tryEmit(
            if (isScreenLocked) GestureEvent.ScreenLocked 
            else GestureEvent.ScreenUnlocked
        )
    }
    
    /**
     * Reset zoom level
     */
    fun resetZoom() {
        zoomLevel = 1.0f
        _gestureEvents.tryEmit(GestureEvent.ZoomChanged(zoomLevel))
    }
    
    private inner class PlayerGestureListener : GestureDetector.SimpleOnGestureListener() {
        
        override fun onDown(e: MotionEvent): Boolean {
            initialTouchX = e.x
            initialTouchY = e.y
            isGesturing = false
            gestureType = null
            return true
        }
        
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            _gestureEvents.tryEmit(GestureEvent.Tap)
            return true
        }
        
        override fun onDoubleTap(e: MotionEvent): Boolean {
            _gestureEvents.tryEmit(GestureEvent.DoubleTap)
            return true
        }
        
        override fun onLongPress(e: MotionEvent) {
            if (!isScreenLocked) {
                _gestureEvents.tryEmit(GestureEvent.LongPress)
            }
        }
        
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (e1 == null || isGesturing && gestureType != GestureType.UNKNOWN) return false
            
            val deltaX = e2.x - e1.x
            val deltaY = e2.y - e1.y
            
            if (!isGesturing) {
                // Determine gesture type based on initial movement
                gestureType = when {
                    abs(deltaX) > abs(deltaY) -> GestureType.SEEK
                    e1.x < screenWidth / 3f -> GestureType.BRIGHTNESS
                    e1.x > 2 * screenWidth / 3f -> GestureType.VOLUME
                    else -> GestureType.UNKNOWN
                }
                
                if (gestureType != GestureType.UNKNOWN) {
                    isGesturing = true
                    initializeGestureValues(e2.x, e2.y)
                }
            }
            
            when (gestureType) {
                GestureType.BRIGHTNESS -> handleBrightnessGesture(e2.y)
                GestureType.VOLUME -> handleVolumeGesture(e2.y)
                GestureType.SEEK -> handleSeekGesture(e2.x)
                else -> {}
            }
            
            return isGesturing
        }
        
        private fun initializeGestureValues(x: Float, y: Float) {
            when (gestureType) {
                GestureType.BRIGHTNESS -> {
                    initialBrightness = getCurrentBrightness()
                }
                GestureType.VOLUME -> {
                    initialVolume = getCurrentVolume()
                }
                GestureType.SEEK -> {
                    initialSeekPosition = player.currentPosition
                }
                else -> {}
            }
        }
        
        private fun handleBrightnessGesture(currentY: Float) {
            if (initialBrightness < 0) return
            
            val deltaY = initialTouchY - currentY
            val brightnessChange = (deltaY / screenHeight) * 2f
            val newBrightness = (initialBrightness + brightnessChange).coerceIn(0f, 1f)
            
            setBrightness(newBrightness)
            _gestureEvents.tryEmit(GestureEvent.BrightnessChanged(newBrightness))
        }
        
        private fun handleVolumeGesture(currentY: Float) {
            if (initialVolume < 0) return
            
            val deltaY = initialTouchY - currentY
            val volumeChange = (deltaY / screenHeight) * 2f
            val newVolume = (initialVolume + volumeChange).coerceIn(0f, 1f)
            
            setVolume(newVolume)
            _gestureEvents.tryEmit(GestureEvent.VolumeChanged(newVolume))
        }
        
        private fun handleSeekGesture(currentX: Float) {
            if (initialSeekPosition < 0) return
            
            val deltaX = currentX - initialTouchX
            val seekAmount = (deltaX / screenWidth) * player.duration
            val newPosition = (initialSeekPosition + seekAmount).coerceIn(0, player.duration)
            
            _gestureEvents.tryEmit(GestureEvent.SeekChanged(newPosition))
        }
    }
    
    private inner class PlayerScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            val newZoomLevel = (zoomLevel * scaleFactor).coerceIn(1.0f, 3.0f)
            
            if (abs(newZoomLevel - zoomLevel) > 0.01f) {
                zoomLevel = newZoomLevel
                _gestureEvents.tryEmit(GestureEvent.ZoomChanged(zoomLevel))
            }
            
            return true
        }
        
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            return !isScreenLocked
        }
    }
    
    // Platform-specific implementations (to be overridden in platform-specific modules)
    private fun getCurrentBrightness(): Float {
        // This would need to be implemented with platform-specific code
        // For now, return a default value
        return 0.5f
    }
    
    private fun setBrightness(brightness: Float) {
        // Platform-specific implementation
        // This would typically involve adjusting the window attributes
    }
    
    private fun getCurrentVolume(): Float {
        // Platform-specific implementation
        // This would typically involve the AudioManager
        return 0.5f
    }
    
    private fun setVolume(volume: Float) {
        // Platform-specific implementation
        // This would typically involve the AudioManager
    }
}

/**
 * Types of gestures supported by the player
 */
private enum class GestureType {
    BRIGHTNESS,
    VOLUME,
    SEEK,
    UNKNOWN
}

/**
 * Sealed class representing all possible gesture events
 */
sealed class GestureEvent {
    object Tap : GestureEvent()
    object DoubleTap : GestureEvent()
    object LongPress : GestureEvent()
    object ScreenLocked : GestureEvent()
    object ScreenUnlocked : GestureEvent()
    
    data class BrightnessChanged(val brightness: Float) : GestureEvent()
    data class VolumeChanged(val volume: Float) : GestureEvent()
    data class SeekChanged(val position: Long) : GestureEvent()
    data class ZoomChanged(val zoomLevel: Float) : GestureEvent()
}
