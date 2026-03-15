package com.mpvideopro.player.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.mpvideopro.player.MainActivity
import com.mpvideopro.player.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Background playback service for video audio.
 * Allows video audio to continue playing when app is in background.
 */
@AndroidEntryPoint
class VideoPlaybackService : MediaSessionService() {
    
    @Inject
    lateinit var exoPlayer: ExoPlayer
    
    private var mediaSession: MediaSession? = null
    private var notificationManager: NotificationManager? = null
    
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_CHANNEL_ID = "video_playback_channel"
        private const val ACTION_PLAY_PAUSE = "com.mpvideopro.player.PLAY_PAUSE"
        private const val ACTION_STOP = "com.mpvideopro.player.STOP"
        private const val ACTION_SKIP_FORWARD = "com.mpvideopro.player.SKIP_FORWARD"
        private const val ACTION_SKIP_BACKWARD = "com.mpvideopro.player.SKIP_BACKWARD"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initializeMediaSession()
    }
    
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> {
                if (exoPlayer.isPlaying) {
                    exoPlayer.pause()
                } else {
                    exoPlayer.play()
                }
            }
            ACTION_STOP -> {
                exoPlayer.stop()
                stopSelf()
            }
            ACTION_SKIP_FORWARD -> {
                exoPlayer.seekForward()
            }
            ACTION_SKIP_BACKWARD -> {
                exoPlayer.seekBack()
            }
        }
        
        updateNotification()
        return START_STICKY
    }
    
    override fun onDestroy() {
        mediaSession?.release()
        exoPlayer.release()
        super.onDestroy()
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return super.onBind(intent)
    }
    
    /**
     * Initialize media session for background playback
     */
    private fun initializeMediaSession() {
        mediaSession = MediaSession.Builder(this, exoPlayer)
            .build()
        
        // Set up media session callbacks
        mediaSession?.setCallback(object : MediaSession.Callback {
            override fun onPlay() {
                exoPlayer.play()
                updateNotification()
            }
            
            override fun onPause() {
                exoPlayer.pause()
                updateNotification()
            }
            
            override fun onStop() {
                exoPlayer.stop()
                stopSelf()
            }
            
            override fun onSeekForward() {
                exoPlayer.seekForward()
            }
            
            override fun onSeekBackward() {
                exoPlayer.seekBack()
            }
        })
        
        // Start foreground service
        startForeground(NOTIFICATION_ID, createNotification())
    }
    
    /**
     * Create notification channel for Android O and above
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Video Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controls for video playback"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            
            notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
    
    /**
     * Create notification for background playback
     */
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // Create action intents
        val playPauseIntent = Intent(this, VideoPlaybackService::class.java).apply {
            action = ACTION_PLAY_PAUSE
        }
        val playPausePendingIntent = PendingIntent.getService(
            this, 0, playPauseIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val stopIntent = Intent(this, VideoPlaybackService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val skipForwardIntent = Intent(this, VideoPlaybackService::class.java).apply {
            action = ACTION_SKIP_FORWARD
        }
        val skipForwardPendingIntent = PendingIntent.getService(
            this, 2, skipForwardIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val skipBackwardIntent = Intent(this, VideoPlaybackService::class.java).apply {
            action = ACTION_SKIP_BACKWARD
        }
        val skipBackwardPendingIntent = PendingIntent.getService(
            this, 3, skipBackwardIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("MP Video Pro")
            .setContentText("Playing video in background")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setShowWhen(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                R.drawable.ic_skip_backward,
                "Backward",
                skipBackwardPendingIntent
            )
            .addAction(
                if (exoPlayer.isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                if (exoPlayer.isPlaying) "Pause" else "Play",
                playPausePendingIntent
            )
            .addAction(
                R.drawable.ic_skip_forward,
                "Forward",
                skipForwardPendingIntent
            )
            .addAction(
                R.drawable.ic_stop,
                "Stop",
                stopPendingIntent
            )
        
        // Set large icon if available (video thumbnail)
        // This would typically come from the media metadata
        // builder.setLargeIcon(thumbnailBitmap)
        
        return builder.build()
    }
    
    /**
     * Update notification when playback state changes
     */
    private fun updateNotification() {
        val notification = createNotification()
        notificationManager?.notify(NOTIFICATION_ID, notification)
    }
}
