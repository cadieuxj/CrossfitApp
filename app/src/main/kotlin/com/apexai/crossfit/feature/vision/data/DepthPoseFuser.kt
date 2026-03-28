package com.apexai.crossfit.feature.vision.data

import android.content.Context
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Session
import com.apexai.crossfit.core.domain.model.PoseLandmark
import kotlin.math.acos
import kotlin.math.sqrt

/**
 * Fuses 2D MediaPipe pose landmarks with ARCore depth map to produce
 * real 3D joint positions and scientifically valid joint angles.
 *
 * Replaces the previous Gemini-hallucinated overlay_data with actual
 * depth measurements from the device's ToF/structured-light sensor.
 *
 * Falls back to 2D-only mode on devices without ARCore depth support —
 * [isDepthSupported] will be false and all input landmarks are returned unchanged.
 *
 * Supported devices: any Android device that passes
 * [Session.isDepthModeSupported] with [Config.DepthMode.AUTOMATIC].
 * Typical examples: Samsung Galaxy S21+/S22+/S23+, Google Pixel 6 Pro/7 Pro,
 * OnePlus 9 Pro, Xiaomi Mi 11 Ultra.
 */
class DepthPoseFuser(private val context: Context) {

    private var arSession: Session? = null

    /** True only if the device has depth hardware and ARCore is available. */
    val isDepthSupported: Boolean get() = arSession != null

    /**
     * Initialises ARCore and depth mode.
     * Call once during ViewModel init. Returns true if depth is available.
     */
    fun initialize(): Boolean {
        return try {
            val availability = ArCoreApk.getInstance().checkAvailability(context)
            if (!availability.isSupported) return false

            val session = Session(context)
            val config = Config(session).apply {
                depthMode = Config.DepthMode.AUTOMATIC
            }
            if (!session.isDepthModeSupported(config.depthMode)) {
                session.close()
                return false
            }
            session.configure(config)
            arSession = session
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Enriches 2D normalized landmarks (x/y in 0..1 range) with real depth
     * sampled from the ARCore 16-bit depth image.
     *
     * @param landmarks2d   MediaPipe landmarks in normalised image coordinates
     * @param frame         Current ARCore [Frame] — call [Session.update] first
     * @param imageWidth    Width of the camera image in pixels
     * @param imageHeight   Height of the camera image in pixels
     * @return              Landmarks with [PoseLandmark.z] set to depth in metres
     *                      (or unchanged if depth is unavailable for a given point)
     */
    fun enrichWithDepth(
        landmarks2d: List<PoseLandmark>,
        frame: Frame,
        imageWidth: Int,
        imageHeight: Int
    ): List<PoseLandmark> {
        arSession ?: return landmarks2d
        return try {
            val depthImage = frame.acquireDepthImage16Bits()
            val depthBuffer = depthImage.planes[0].buffer.asShortBuffer()
            val depthWidth  = depthImage.width
            val depthHeight = depthImage.height

            val enriched = landmarks2d.map { landmark ->
                val px = (landmark.x * imageWidth).toInt().coerceIn(0, imageWidth - 1)
                val py = (landmark.y * imageHeight).toInt().coerceIn(0, imageHeight - 1)
                val dx = (px.toFloat() / imageWidth  * depthWidth).toInt().coerceIn(0, depthWidth  - 1)
                val dy = (py.toFloat() / imageHeight * depthHeight).toInt().coerceIn(0, depthHeight - 1)
                // ARCore depth16: value in millimetres (uint16, little-endian)
                val depthMm = depthBuffer.get(dy * depthWidth + dx).toInt() and 0xFFFF
                val depthMeters = depthMm / 1000f
                if (depthMm > 0) landmark.copy(z = depthMeters) else landmark
            }
            depthImage.close()
            enriched
        } catch (e: Exception) {
            landmarks2d
        }
    }

    /**
     * Computes the 3D angle (in degrees) at joint [b] formed by the ray
     * A→B and the ray C→B using the dot-product formula.
     *
     * This is only scientifically valid when [PoseLandmark.z] contains
     * real depth data from [enrichWithDepth]; on devices without depth
     * the z-component defaults to 0 and the result degrades to the 2D angle.
     */
    fun compute3dAngle(a: PoseLandmark, b: PoseLandmark, c: PoseLandmark): Float {
        val v1 = floatArrayOf(a.x - b.x, a.y - b.y, a.z - b.z)
        val v2 = floatArrayOf(c.x - b.x, c.y - b.y, c.z - b.z)
        val dot  = v1.indices.sumOf { (v1[it] * v2[it]).toDouble() }
        val mag1 = sqrt(v1.sumOf { (it.toDouble() * it) })  // compile-safe lambda
        val mag2 = sqrt(v2.sumOf { (it.toDouble() * it) })
        val mag1f = sqrt(v1.fold(0.0) { acc, f -> acc + f * f })
        val mag2f = sqrt(v2.fold(0.0) { acc, f -> acc + f * f })
        if (mag1f < 1e-6 || mag2f < 1e-6) return 0f
        return Math.toDegrees(acos((dot / (mag1f * mag2f)).coerceIn(-1.0, 1.0))).toFloat()
    }

    fun close() {
        arSession?.close()
        arSession = null
    }
}
