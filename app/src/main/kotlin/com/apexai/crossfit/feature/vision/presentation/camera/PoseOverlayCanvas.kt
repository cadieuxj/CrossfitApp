package com.apexai.crossfit.feature.vision.presentation.camera

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.apexai.crossfit.core.domain.model.JointAngle
import com.apexai.crossfit.core.domain.model.PoseLandmark
import com.apexai.crossfit.core.domain.model.PoseOverlayData
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.NeonGreen
import com.apexai.crossfit.core.ui.theme.SurfaceDark
import com.apexai.crossfit.core.ui.theme.TextPrimary

// BlazePose skeleton connections (33 landmarks, standard topology)
private val POSE_CONNECTIONS = listOf(
    // Face
    0 to 1, 1 to 2, 2 to 3, 3 to 7,
    0 to 4, 4 to 5, 5 to 6, 6 to 8,
    // Upper body
    11 to 12, 11 to 13, 13 to 15, 12 to 14, 14 to 16,
    // Torso
    11 to 23, 12 to 24, 23 to 24,
    // Lower body
    23 to 25, 25 to 27, 27 to 29, 29 to 31,
    24 to 26, 26 to 28, 28 to 30, 30 to 32
)

// Key landmark indices per CLAUDE.md
private val KEY_LANDMARKS = setOf(11, 12, 23, 24, 27, 28)

@Composable
fun PoseOverlayCanvas(
    poseOverlayData: PoseOverlayData,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val canvasWidth  = size.width
        val canvasHeight = size.height
        val landmarks    = poseOverlayData.landmarks

        fun lm(idx: Int) = landmarks.getOrNull(idx)
        fun Offset(lm: PoseLandmark) = Offset(lm.x * canvasWidth, lm.y * canvasHeight)

        // --- Skeleton lines ---
        POSE_CONNECTIONS.forEach { (startIdx, endIdx) ->
            val start = lm(startIdx); val end = lm(endIdx)
            if (start != null && end != null &&
                start.visibility > 0.5f && end.visibility > 0.5f) {
                drawLine(
                    color     = ElectricBlue.copy(alpha = 0.6f),
                    start     = Offset(start),
                    end       = Offset(end),
                    strokeWidth = 3.dp.toPx(),
                    cap       = StrokeCap.Round
                )
            }
        }

        // --- Joint dots ---
        landmarks.forEach { lm ->
            if (lm.visibility > 0.5f) {
                val isKey = lm.index in KEY_LANDMARKS
                drawCircle(
                    color  = if (isKey) ElectricBlue else TextPrimary,
                    radius = if (isKey) 12.dp.toPx() else 8.dp.toPx(),
                    center = Offset(lm)
                )
            }
        }

        // --- Barbell trajectory ---
        if (poseOverlayData.barbellTrajectory.size > 1) {
            val path = androidx.compose.ui.graphics.Path()
            poseOverlayData.barbellTrajectory.forEachIndexed { i, pt ->
                val x = pt.x * canvasWidth
                val y = pt.y * canvasHeight
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path        = path,
                color       = NeonGreen.copy(alpha = 0.7f),
                style       = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 2.5.dp.toPx()
                )
            )
        }
        poseOverlayData.barbellPosition?.let { pos ->
            drawCircle(
                color  = NeonGreen.copy(alpha = 0.8f),
                radius = 16.dp.toPx(),
                center = Offset(pos.x * canvasWidth, pos.y * canvasHeight)
            )
        }

        // --- Joint angle text labels ---
        drawIntoCanvas { canvas ->
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 14.dp.toPx()
                isAntiAlias = true
            }
            val bgPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.argb(178, 18, 18, 26)
            }

            poseOverlayData.jointAngles.forEach { (joint, angle) ->
                val anchorLmIndex = when (joint) {
                    JointAngle.LEFT_KNEE       -> 25
                    JointAngle.RIGHT_KNEE      -> 26
                    JointAngle.LEFT_HIP        -> 23
                    JointAngle.RIGHT_HIP       -> 24
                    JointAngle.LEFT_ELBOW      -> 13
                    JointAngle.RIGHT_ELBOW     -> 14
                    JointAngle.LEFT_SHOULDER   -> 11
                    JointAngle.RIGHT_SHOULDER  -> 12
                    JointAngle.LEFT_ANKLE      -> 27
                    JointAngle.RIGHT_ANKLE     -> 28
                    JointAngle.TRUNK_INCLINATION -> 23
                }
                val anchorLm = lm(anchorLmIndex) ?: return@forEach
                if (anchorLm.visibility < 0.5f) return@forEach

                val text = "${angle.toInt()}°"
                val x = (anchorLm.x * canvasWidth) + 14.dp.toPx()
                val y = (anchorLm.y * canvasHeight) - 4.dp.toPx()

                val textWidth  = paint.measureText(text)
                val textHeight = paint.textSize
                val rect = android.graphics.RectF(
                    x - 4.dp.toPx(), y - textHeight,
                    x + textWidth + 4.dp.toPx(), y + 4.dp.toPx()
                )
                canvas.nativeCanvas.drawRoundRect(rect, 4.dp.toPx(), 4.dp.toPx(), bgPaint)
                canvas.nativeCanvas.drawText(text, x, y, paint)
            }
        }
    }
}
