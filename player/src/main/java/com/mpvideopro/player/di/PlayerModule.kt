package com.mpvideopro.player.di

import android.content.Context
import com.mpvideopro.player.VideoPlayer
import com.mpvideopro.player.gesture.GestureController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for player dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object PlayerModule {
    
    @Provides
    @Singleton
    fun provideVideoPlayer(@ApplicationContext context: Context): VideoPlayer {
        return VideoPlayer(context)
    }
}
