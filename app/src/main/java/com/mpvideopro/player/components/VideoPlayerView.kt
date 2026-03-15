package com.mpvideopro.player.components

import android.net.Uri
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import com.mpvideopro.player.VideoPlayer
import com.mpvideopro.player.viewmodel.PlayerViewModel

/**
 * Composable wrapper for ExoPlayer's PlayerView.
 * Handles video rendering and gesture detection.
 */
@Composable
fun VideoPlayerView(
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel,
    zoomLevel: Float,
    onGesture: (android.view.MotionEvent) -> Unit
) {
    val context = LocalContext.current
    
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                // Configure PlayerView
                useController = false // We use custom controls
                resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                setKeepContentOnPlayerReset(true)
                
                // Enable surface view for better performance
                surfaceType = PlayerView.SURFACE_TYPE_SURFACE_VIEW
                
                // Set player
                viewModel.videoPlayer?.attachToView(this)
                
                // Configure gesture detection
                setOnTouchListener { _, event ->
                    onGesture(event)
                    true
                }
            }
        },
        modifier = modifier.background(Color.Black),
        update = { playerView ->
            // Update zoom level
            updateZoomLevel(playerView, zoomLevel)
            
            // Update player if needed
            viewModel.videoPlayer?.attachToView(playerView)
        },
        release = { playerView ->
            viewModel.videoPlayer?.detachFromView(playerView)
        }
    )
}

/**
 * Update the zoom level of the player view
 */
private fun updateZoomLevel(playerView: PlayerView, zoomLevel: Float) {
    // Apply zoom transformation to the video surface view
    val surfaceView = playerView.videoSurfaceView as? ViewGroup
    surfaceView?.let { viewGroup ->
        if (viewGroup.childCount > 0) {
            val child = viewGroup.getChildAt(0)
            child.scaleX = zoomLevel
            child.scaleY = zoomLevel
        }
    }
}
