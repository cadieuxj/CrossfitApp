package com.apexai.crossfit.core.media

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.lang.reflect.Field

/**
 * Unit tests for [PlayerPoolManager].
 *
 * Critical constraints (CLAUDE.md):
 * - Pool size MUST be exactly 2 — hardware decoder budget limit.
 * - Pool exhaustion must not crash — returns temporary player with warning log.
 * - [release] stops and clears media items before returning player to pool.
 *
 * Uses Robolectric for Android [Context]. Uses reflection to access private
 * [available] and [inUse] fields for white-box size assertions, since the
 * production class intentionally does not expose these counts publicly.
 */
@RunWith(RobolectricTestRunner::class)
class PlayerPoolManagerTest {

    private lateinit var context: Context
    private lateinit var pool: TestablePlayerPoolManager

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        pool = TestablePlayerPoolManager(context)
    }

    // --------------------------------------------------------
    // Pool size validation — CLAUDE.md mandates exactly 2
    // --------------------------------------------------------

    @Test
    fun poolSize_isExactly2_onInit() {
        assertEquals(
            "Pool size must be 2 per CLAUDE.md hardware decoder constraint",
            2,
            pool.availableCount()
        )
    }

    @Test
    fun inUseCount_isZero_onInit() {
        assertEquals(0, pool.inUseCount())
    }

    // --------------------------------------------------------
    // acquire
    // --------------------------------------------------------

    @Test
    fun acquire_firstCall_movesPlayerToInUse() {
        pool.acquire()

        assertEquals(1, pool.availableCount())
        assertEquals(1, pool.inUseCount())
    }

    @Test
    fun acquire_secondCall_exhaustsPool() {
        pool.acquire()
        pool.acquire()

        assertEquals(0, pool.availableCount())
        assertEquals(2, pool.inUseCount())
    }

    @Test
    fun acquire_returnedPlayer_isNotNull() {
        val player = pool.acquire()
        assertNotNull(player)
    }

    @Test
    fun acquire_twoPlayers_areDifferentInstances() {
        val p1 = pool.acquire()
        val p2 = pool.acquire()
        assertNotSame(p1, p2)
    }

    @Test
    fun acquire_poolExhausted_doesNotThrow() {
        pool.acquire()
        pool.acquire()

        // Third acquire — pool is empty, must not crash
        val p3 = pool.acquire()
        assertNotNull("Pool exhaustion must not return null", p3)
    }

    @Test
    fun acquire_poolExhausted_logicFallsBackToTemporaryPlayer() {
        pool.acquire()
        pool.acquire()
        // Pool is now empty (0 available, 2 in use)

        val p3 = pool.acquire()

        assertNotNull(p3)
        // Available stays 0 — the temporary player is tracked in inUse
        assertEquals(0, pool.availableCount())
    }

    // --------------------------------------------------------
    // release
    // --------------------------------------------------------

    @Test
    fun release_acquiredPlayer_restoresToAvailable() {
        val player = pool.acquire()

        pool.release(player)

        assertEquals(2, pool.availableCount())
        assertEquals(0, pool.inUseCount())
    }

    @Test
    fun release_callsStopOnPlayer() {
        val player = pool.acquire() as MockExoPlayer
        pool.release(player)

        assertTrue("stop() must be called on release", player.stopCalled)
    }

    @Test
    fun release_callsClearMediaItemsOnPlayer() {
        val player = pool.acquire() as MockExoPlayer
        pool.release(player)

        assertTrue("clearMediaItems() must be called on release", player.clearMediaItemsCalled)
    }

    @Test
    fun release_playerNotInUse_isIdempotent() {
        val player = pool.acquire()
        pool.release(player)

        // Second release of same player — should not double-add to available
        pool.release(player)

        assertTrue("Available should not exceed 2 after double-release",
            pool.availableCount() <= 2)
    }

    @Test
    fun release_unknownPlayer_doesNotCorruptPool() {
        val outsider = MockExoPlayer()

        pool.release(outsider)

        assertEquals("Pool should be unchanged after releasing unknown player",
            2, pool.availableCount())
        assertEquals(0, pool.inUseCount())
    }

    // --------------------------------------------------------
    // Acquire → Release → Acquire cycle
    // --------------------------------------------------------

    @Test
    fun acquireReleaseCycle_poolRestoresToFullCapacity() {
        val p1 = pool.acquire()
        val p2 = pool.acquire()
        assertEquals(0, pool.availableCount())

        pool.release(p1)
        pool.release(p2)

        assertEquals(2, pool.availableCount())
        assertEquals(0, pool.inUseCount())
    }

    @Test
    fun acquireAfterRelease_returnsPlayerFromPool() {
        val first = pool.acquire()
        pool.release(first)

        val second = pool.acquire()

        assertNotNull(second)
        assertEquals(1, pool.inUseCount())
    }

    // --------------------------------------------------------
    // releaseAll
    // --------------------------------------------------------

    @Test
    fun releaseAll_clearsAvailableAndInUse() {
        pool.releaseAll()

        assertEquals(0, pool.availableCount())
        assertEquals(0, pool.inUseCount())
    }

    @Test
    fun releaseAll_withActiveAcquire_releasesAll() {
        pool.acquire()

        pool.releaseAll()

        assertEquals(0, pool.availableCount())
        assertEquals(0, pool.inUseCount())
    }
}

// --------------------------------------------------------
// Test doubles
// --------------------------------------------------------

/**
 * Minimal ExoPlayer stand-in that records [stop] and [clearMediaItems] calls.
 * Avoids real ExoPlayer instantiation (requires hardware decoder).
 */
class MockExoPlayer : ExoPlayer by mockk(relaxed = true) {
    var stopCalled: Boolean = false
    var clearMediaItemsCalled: Boolean = false

    override fun stop() {
        stopCalled = true
    }

    override fun clearMediaItems() {
        clearMediaItemsCalled = true
    }

    override fun release() {
        // no-op in tests
    }
}

/**
 * Subclass of [PlayerPoolManager] that overrides [buildPlayer] to return
 * [MockExoPlayer] instances instead of real [ExoPlayer] objects.
 *
 * Uses reflection to read the private [available] (ArrayDeque) and
 * [inUse] (Set) fields for assertion without modifying production code.
 */
class TestablePlayerPoolManager(context: Context) : PlayerPoolManager(context) {

    // Intercept player creation to use MockExoPlayer
    public override fun buildPlayer(): ExoPlayer = MockExoPlayer()

    fun availableCount(): Int = getPrivateDeque("available").size

    fun inUseCount(): Int = getPrivateSet("inUse").size

    private fun getPrivateDeque(fieldName: String): Collection<*> {
        val field: Field = PlayerPoolManager::class.java.getDeclaredField(fieldName)
        field.isAccessible = true
        return field.get(this) as Collection<*>
    }

    private fun getPrivateSet(fieldName: String): Collection<*> {
        val field: Field = PlayerPoolManager::class.java.getDeclaredField(fieldName)
        field.isAccessible = true
        return field.get(this) as Collection<*>
    }
}
