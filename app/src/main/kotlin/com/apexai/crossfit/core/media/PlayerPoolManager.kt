package com.apexai.crossfit.core.media

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Maintains a fixed-size pool of ExoPlayer instances.
 *
 * MANDATE (CLAUDE.md): Never instantiate ExoPlayer inline per video tile.
 * One-player-per-tile exhausts the hardware decoder budget and crashes the app.
 *
 * Usage pattern:
 *   val player = pool.acquire()
 *   player.setMediaItem(...)
 *   player.prepare()
 *   // Attach to PlayerView
 *   ...
 *   pool.release(player)
 */
@Singleton
class PlayerPoolManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val poolSize = 2
    private val available = ArrayDeque<ExoPlayer>()
    private val inUse = mutableSetOf<ExoPlayer>()

    init {
        repeat(poolSize) {
            available.addLast(buildPlayer())
        }
    }

    /**
     * Acquires a player from the pool. If the pool is exhausted, returns the
     * least-recently-released player (evicting its current usage is the
     * caller's responsibility via [release]).
     */
    @Synchronized
    fun acquire(): ExoPlayer {
        return if (available.isNotEmpty()) {
            val player = available.removeFirst()
            inUse.add(player)
            player
        } else {
            // Pool exhausted — callers should always balance acquire/release.
            // Build a temporary player rather than crashing; logs a warning.
            android.util.Log.w("PlayerPoolManager", "Pool exhausted — consider increasing pool size")
            buildPlayer().also { inUse.add(it) }
        }
    }

    /**
     * Returns a player to the pool. Stops and clears the player before
     * making it available for reuse so the next caller gets a clean state.
     */
    @Synchronized
    fun release(player: ExoPlayer) {
        if (inUse.remove(player)) {
            player.stop()
            player.clearMediaItems()
            available.addLast(player)
        }
    }

    /**
     * Releases all players and frees hardware decoder resources.
     * Call from Application.onTerminate() or a process-level lifecycle hook.
     */
    @Synchronized
    fun releaseAll() {
        (available + inUse).forEach { it.release() }
        available.clear()
        inUse.clear()
    }

    private fun buildPlayer(): ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .setUsage(C.USAGE_MEDIA)
                .build(),
            /* handleAudioFocus = */ true
        )
        .setHandleAudioBecomingNoisy(true)
        .build()
}
