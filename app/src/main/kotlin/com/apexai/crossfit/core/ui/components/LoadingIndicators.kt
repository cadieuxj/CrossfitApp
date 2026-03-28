package com.apexai.crossfit.core.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.BorderVisible
import com.apexai.crossfit.core.ui.theme.CornerFull
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.SurfaceElevated
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary

/**
 * Shimmer skeleton placeholder for loading states.
 * Animates a gradient sweep from left to right infinitely.
 */
fun Modifier.shimmerSkeleton(
    baseColor: Color = SurfaceElevated,
    shimmerColor: Color = BorderVisible.copy(alpha = 0.6f)
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerTranslateX by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    background(
        brush = Brush.horizontalGradient(
            colors = listOf(
                baseColor,
                shimmerColor,
                baseColor
            ),
            startX = shimmerTranslateX * 500f,
            endX = (shimmerTranslateX + 1f) * 500f
        )
    )
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp
) {
    Box(
        modifier = modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius))
            .shimmerSkeleton()
    )
}

/**
 * Full circular readiness ring drawn with Canvas.
 * Animates from 0 to the target sweep angle on first composition.
 *
 * @param score      ACWR score (0.0 to 2.0+)
 * @param zoneColor  Color of the filled arc, derived from ReadinessZone
 * @param label      Center label (e.g., "1.12")
 * @param size       Diameter of the ring
 */
@Composable
fun CircularReadinessRing(
    score: Float,
    zoneColor: Color,
    label: String,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    strokeWidth: Dp = 14.dp
) {
    val textMeasurer = rememberTextMeasurer()
    val targetSweep = (score.coerceIn(0f, 2f) / 2f) * 270f
    val animatedSweep by animateFloatAsState(
        targetValue = targetSweep,
        animationSpec = tween(800, easing = androidx.compose.animation.core.EaseOut),
        label = "ring_sweep"
    )
    val strokeWidthPx = with(androidx.compose.ui.platform.LocalDensity.current) { strokeWidth.toPx() }

    Canvas(modifier = modifier.size(size)) {
        val padding = strokeWidthPx / 2f
        val arcSize = Size(
            this.size.width - strokeWidthPx,
            this.size.height - strokeWidthPx
        )

        // Track arc (background)
        drawArc(
            color = BorderSubtle,
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(padding, padding),
            size = arcSize,
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
        )

        // Fill arc (zone color)
        if (animatedSweep > 0f) {
            drawArc(
                color = zoneColor,
                startAngle = 135f,
                sweepAngle = animatedSweep,
                useCenter = false,
                topLeft = Offset(padding, padding),
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }

        // Center label
        val textResult = textMeasurer.measure(
            text = label,
            style = TextStyle(
                color = zoneColor,
                fontSize = 24.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        )
        drawText(
            textLayoutResult = textResult,
            topLeft = Offset(
                (this.size.width - textResult.size.width) / 2f,
                (this.size.height - textResult.size.height) / 2f
            )
        )

        // "ACWR" sub-label
        val subResult = textMeasurer.measure(
            text = "ACWR",
            style = TextStyle(
                color = TextSecondary,
                fontSize = 10.sp
            )
        )
        drawText(
            textLayoutResult = subResult,
            topLeft = Offset(
                (this.size.width - subResult.size.width) / 2f,
                (this.size.height - textResult.size.height) / 2f + textResult.size.height + 2f
            )
        )
    }
}

/**
 * A thin animated linear progress bar.
 */
@Composable
fun ApexLinearProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = ElectricBlue,
    trackColor: Color = BorderSubtle
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(600, easing = androidx.compose.animation.core.EaseOut),
        label = "linear_progress"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(CornerFull)
    ) {
        // Track
        drawRect(
            color = trackColor,
            size = this.size
        )
        // Fill
        drawRect(
            color = color,
            size = Size(this.size.width * animatedProgress, this.size.height)
        )
    }
}
