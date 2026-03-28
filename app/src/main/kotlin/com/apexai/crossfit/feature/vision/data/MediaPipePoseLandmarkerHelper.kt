package com.apexai.crossfit.feature.vision.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import androidx.camera.core.ImageProxy
import com.apexai.crossfit.core.domain.model.JointAngle
import com.apexai.crossfit.core.domain.model.PoseLandmark
import com.apexai.crossfit.core.domain.model.PoseOverlayData
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Wraps MediaPipe PoseLandmarker in LIVE_STREAM mode.
 *
 * MANDATE (CLAUDE.md): Must use LIVE_STREAM mode with async resultListener.
 * Z-depth coordinates are unreliable on mobile — all coaching logic uses
 * only 2D (x, y) angles.
 *
 * Landmark indices for Olympic lifting (from CLAUDE.md):
 *   Shoulders: 11, 12
 *   Hips:      23, 24
 *   Ankles:    27, 28
 */
@Singleton
class MediaPipePoseLandmarkerHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var poseLandmarker: PoseLandmarker? = null

    var resultListener: ((PoseOverlayData) -> Unit)? = null
    var errorListener: ((RuntimeException) -> Unit)? = null

    fun setup() {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("pose_landmarker_lite.task")
            .setDelegate(Delegate.GPU)
            .build()

        val options = PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setMinPoseDetectionConfidence(0.5f)
            .setMinPosePresenceConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setResultListener(::onResult)
            .setErrorListener { e -> errorListener?.invoke(RuntimeException(e)) }
            .build()

        poseLandmarker = PoseLandmarker.createFromOptions(context, options)
    }

    /**
     * Feeds an ImageProxy frame to MediaPipe for asynchronous pose detection.
     * Called from CameraX ImageAnalysis use case — must return quickly.
     */
    fun detectAsync(imageProxy: ImageProxy, isFrontCamera: Boolean) {
        val bitmapBuffer = Bitmap.createBitmap(
            imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888
        )
        imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }

        val matrix = Matrix().apply {
            postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            if (isFrontCamera) postScale(-1f, 1f, imageProxy.width / 2f, imageProxy.height / 2f)
        }
        val rotatedBitmap = Bitmap.createBitmap(
            bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height, matrix, true
        )
        bitmapBuffer.recycle()

        val mpImage = BitmapImageBuilder(rotatedBitmap).build()
        poseLandmarker?.detectAsync(mpImage, SystemClock.uptimeMillis())
        rotatedBitmap.recycle()
    }

    private fun onResult(result: PoseLandmarkerResult, input: MPImage) {
        if (result.landmarks().isEmpty()) return

        val rawLandmarks = result.landmarks()[0]
        val landmarks = rawLandmarks.mapIndexed { index, lm ->
            PoseLandmark(
                index      = index,
                x          = lm.x(),
                y          = lm.y(),
                z          = lm.z(),
                visibility = lm.visibility().orElse(0f)
            )
        }

        val jointAngles = calculateJointAngles(landmarks)

        resultListener?.invoke(
            PoseOverlayData(
                landmarks       = landmarks,
                jointAngles     = jointAngles,
                barbellPosition = null,   // barbell tracking via separate pipeline
                barbellTrajectory = emptyList(),
                frameTimestamp  = result.timestampMs()
            )
        )
    }

    /**
     * Calculates 2D joint angles using only x, y coordinates.
     * Z is deliberately excluded per CLAUDE.md (unreliable on mobile).
     *
     * Angle at joint B in triangle A-B-C = atan2 formula.
     */
    private fun calculateJointAngles(landmarks: List<PoseLandmark>): Map<JointAngle, Float> {
        val angles = mutableMapOf<JointAngle, Float>()

        fun angle(a: PoseLandmark, b: PoseLandmark, c: PoseLandmark): Float {
            val ax = a.x - b.x; val ay = a.y - b.y
            val cx = c.x - b.x; val cy = c.y - b.y
            val dot  = ax * cx + ay * cy
            val magA = sqrt(ax * ax + ay * ay)
            val magC = sqrt(cx * cx + cy * cy)
            if (magA == 0f || magC == 0f) return 0f
            return Math.toDegrees(Math.acos((dot / (magA * magC)).toDouble().coerceIn(-1.0, 1.0))).toFloat()
        }

        fun lm(idx: Int) = landmarks.getOrNull(idx)

        // Key joints for Olympic lifting (CLAUDE.md landmark indices)
        val lShoulder = lm(11); val rShoulder = lm(12)
        val lHip      = lm(23); val rHip      = lm(24)
        val lKnee     = lm(25); val rKnee     = lm(26)
        val lAnkle    = lm(27); val rAnkle    = lm(28)
        val lElbow    = lm(13); val rElbow    = lm(14)
        val lWrist    = lm(15); val rWrist    = lm(16)

        // Knee angles
        if (lHip != null && lKnee != null && lAnkle != null && lKnee.visibility > 0.5f)
            angles[JointAngle.LEFT_KNEE] = angle(lHip, lKnee, lAnkle)
        if (rHip != null && rKnee != null && rAnkle != null && rKnee.visibility > 0.5f)
            angles[JointAngle.RIGHT_KNEE] = angle(rHip, rKnee, rAnkle)

        // Hip angles
        if (lShoulder != null && lHip != null && lKnee != null && lHip.visibility > 0.5f)
            angles[JointAngle.LEFT_HIP] = angle(lShoulder, lHip, lKnee)
        if (rShoulder != null && rHip != null && rKnee != null && rHip.visibility > 0.5f)
            angles[JointAngle.RIGHT_HIP] = angle(rShoulder, rHip, rKnee)

        // Elbow angles
        if (lShoulder != null && lElbow != null && lWrist != null && lElbow.visibility > 0.5f)
            angles[JointAngle.LEFT_ELBOW] = angle(lShoulder, lElbow, lWrist)
        if (rShoulder != null && rElbow != null && rWrist != null && rElbow.visibility > 0.5f)
            angles[JointAngle.RIGHT_ELBOW] = angle(rShoulder, rElbow, rWrist)

        // Trunk inclination (angle of torso from vertical)
        if (lShoulder != null && lHip != null) {
            val deltaY = lHip.y - lShoulder.y
            val deltaX = lHip.x - lShoulder.x
            val inclination = Math.toDegrees(atan2(deltaX.toDouble(), deltaY.toDouble())).toFloat()
            angles[JointAngle.TRUNK_INCLINATION] = Math.abs(inclination)
        }

        return angles
    }

    fun close() {
        poseLandmarker?.close()
        poseLandmarker = null
    }
}
