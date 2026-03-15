package com.mpvideopro.player.pip

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Build
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import com.mpvideoplayer.player.VideoPlayer
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Picture-in-Picture activity for video playback.
 * Shows video in a small floating window when user navigates away.
 */
@AndroidEntryPoint
class PipActivity : ComponentActivity() {
    
    @Inject
    lateinit var videoPlayer: VideoPlayer
    
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MPVideoProTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    PipPlayerScreen()
                }
            }
        }
    }
    
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        enterPipMode()
    }
    
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        
        if (isInPictureInPictureMode) {
            // Hide UI controls in PiP mode
            // This would typically hide the controls overlay
        } else {
            // Show UI controls when exiting PiP mode
            // This would typically show the controls overlay
        }
    }
    
    /**
     * Enter Picture-in-Picture mode
     */
    private fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val aspectRatio = Rational(16, 9) // Default aspect ratio
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(aspectRatio)
                .build()
            
            val result = enterPictureInPictureMode(params)
            if (!result) {
                // PiP mode not available or failed
                finish()
            }
        }
    }
}

@Composable
private fun PipPlayerScreen() {
    val configuration = LocalConfiguration.current
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Video player view for PiP
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    player = videoPlayer.getExoPlayer() // This would need to be added to VideoPlayer
                    useController = false // No controls in PiP mode
                    resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
