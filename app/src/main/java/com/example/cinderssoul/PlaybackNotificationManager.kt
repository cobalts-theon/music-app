package com.example.cinderssoul

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.ui.PlayerNotificationManager

@UnstableApi
class PlaybackNotificationManager(
    private val context: Context,
    private val mediaSession: MediaSession,
    private val player: Player,
    private val contentIntent: PendingIntent
) {
    companion object {
        private const val CHANNEL_ID = "cinders_playback"
        private const val CHANNEL_NAME = "Cinder's Soul Playback"
        private const val NOTIFICATION_ID = 1001
    }

    private val notificationManager: PlayerNotificationManager
    private val appLogoBitmap: Bitmap? by lazy {
        BitmapFactory.decodeResource(context.resources, R.drawable.cinders_souls_logo)
    }

    init {
        ensureNotificationChannel()
        val descriptionAdapter = object : PlayerNotificationManager.MediaDescriptionAdapter {
            override fun getCurrentContentTitle(player: Player): CharSequence {
                return player.mediaMetadata.title ?: "Cinder's Soul"
            }

            override fun createCurrentContentIntent(player: Player): PendingIntent? {
                return contentIntent
            }

            override fun getCurrentContentText(player: Player): CharSequence? {
                return player.mediaMetadata.artist ?: player.mediaMetadata.albumTitle
            }

            override fun getCurrentLargeIcon(
                player: Player,
                callback: PlayerNotificationManager.BitmapCallback
            ): Bitmap? {
                return appLogoBitmap
            }
        }

        notificationManager = PlayerNotificationManager.Builder(
            context,
            NOTIFICATION_ID,
            CHANNEL_ID
        )
            .setMediaDescriptionAdapter(descriptionAdapter)
            .setSmallIconResourceId(R.drawable.ic_cinder_notification)
            .build()
            .apply {
                setMediaSessionToken(mediaSession.platformToken)
                setUseFastForwardAction(false)
                setUseRewindAction(false)
                setUseStopAction(false)
                setUseNextAction(true)
                setUsePreviousAction(true)
                setPlayer(player)
            }
    }

    fun release() {
        notificationManager.setPlayer(null)
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        manager.createNotificationChannel(channel)
    }
}
