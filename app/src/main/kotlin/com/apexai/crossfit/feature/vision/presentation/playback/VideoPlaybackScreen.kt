package com.apexai.crossfit.feature.vision.presentation.playback

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.FastForward
import androidx.compose.material.icons.outlined.FastRewind
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import com.apexai.crossfit.core.domain.model.FaultMarker
import com.apexai.crossfit.core.domain.model.FaultSeverity
import com.apexai.crossfit.core.domain.model.TimedPoseOverlay
import com.apexai.crossfit.core.ui.theme.ApexTypography
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.BlazeOrange
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.ColorError
import com.apexai.crossfit.core.ui.theme.ColorWarning
import com.apexai.crossfit.core.ui.theme.CornerFull
import com.apexai.crossfit.core.ui.theme.CornerMedium
import com.apexai.crossfit.core.ui.theme.CornerSmall
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.NeonGreen
import com.apexai.crossfit.core.ui.theme.SurfaceCard
import com.apexai.crossfit.core.ui.theme.SurfaceDark
import com.apexai.crossfit.core.ui.theme.SurfaceElevated
import com.apexai.crossfit.core.ui.theme.TextOnBlue
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary
import com.apexai.crossfit.feature.vision.presentation.camera.PoseOverlayCanvas
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlaybackScreen(
    viewModel: VideoPlaybackViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Acquire player from pool — CRITICAL: never create inline (CLAUDE.md mandate)
    val player = remember { viewModel.playerPoolManager.acquire() }

    DisposableEffect(uiState.videoUrl) {
        uiState.videoUrl?.let { url ->
            player.setMediaItem(MediaItem.fromUri(url))
            player.prepare()
            player.seekTo(viewModel.initialSeekPositionMs)
            player.playWhenReady = true
        }
        onDispose { viewModel.playerPoolManager.release(player) }
    }

    // Poll player position
    LaunchedEffect(player) {
        while (isActive) {
            viewModel.updatePosition(
                positionMs = player.currentPosition,
                durationMs = player.duration.coerceAtLeast(1L),
                isPlaying  = player.isPlaying
            )
            delay(50)
        }
    }

    val scaffoldState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        containerColor = BackgroundDeepBlack,
        sheetContainerColor = SurfaceCard,
        sheetPeekHeight = 80.dp,
        sheetContent = {
            RepBreakdownPanel(
                repCount     = uiState.repCount,
                faultMarkers = uiState.faultMarkers,
                onRepClick   = { ts -> player.seekTo(ts) }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

            // Layer 1: PlayerView from pool
            val videoAspect = 9f / 16f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f / videoAspect)
                    .align(Alignment.TopCenter)
            ) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            this.player = player
                            useController = false
                            contentDescription = "Video analysis playback"
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Layer 2: Kinematic Canvas overlay
                val overlayFrame = viewModel.overlayForPosition(uiState.currentPositionMs)
                val activeFault  = viewModel.faultAtPosition(uiState.currentPositionMs)

                overlayFrame?.let { frame ->
                    KinematicOverlayCanvas(
                        overlay     = frame,
                        activeFault = activeFault,
                        modifier    = Modifier.fillMaxSize()
                    )
                }
            }

            // Layer 3: Controls
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(Color.Black.copy(0.7f), Color.Transparent)
                            )
                        )
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, "Back", tint = TextPrimary)
                    }
                    Text("Rep Analysis", style = ApexTypography.titleMedium, color = TextPrimary)
                    Row {
                        IconButton(onClick = {}) {
                            Icon(Icons.Outlined.Bookmark, "Bookmark", tint = TextPrimary)
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Outlined.Share, "Share", tint = TextPrimary)
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                // Fault markers timeline
                FaultMarkersTimeline(
                    faultMarkers      = uiState.faultMarkers,
                    currentPositionMs = uiState.currentPositionMs,
                    durationMs        = uiState.durationMs,
                    onFaultTap        = { ts -> player.seekTo(ts) },
                    modifier          = Modifier.fillMaxWidth()
                )

                // Playback controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            viewModel.previousFaultTimestamp(uiState.currentPositionMs)
                                ?.let { player.seekTo(it) }
                        },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Outlined.SkipPrevious, "Previous fault", tint = TextPrimary,
                            modifier = Modifier.size(24.dp))
                    }
                    IconButton(
                        onClick = { player.seekTo((player.currentPosition - 5000).coerceAtLeast(0)) },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Outlined.FastRewind, "Rewind 5s", tint = TextPrimary,
                            modifier = Modifier.size(24.dp))
                    }
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(SurfaceElevated, CornerFull)
                            .clip(CornerFull)
                            .then(
                                Modifier.background(
                                    SurfaceElevated,
                                    CornerFull
                                ).clickable { if (player.isPlaying) player.pause() else player.play() }
                            )
                            .border(2.dp, ElectricBlue, CornerFull),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (uiState.isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                            if (uiState.isPlaying) "Pause" else "Play",
                            tint = ElectricBlue,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    IconButton(
                        onClick = { player.seekTo(player.currentPosition + 5000) },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Outlined.FastForward, "Forward 5s", tint = TextPrimary,
                            modifier = Modifier.size(24.dp))
                    }
                    IconButton(
                        onClick = {
                            viewModel.nextFaultTimestamp(uiState.currentPositionMs)
                                ?.let { player.seekTo(it) }
                        },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Outlined.SkipNext, "Next fault", tint = TextPrimary,
                            modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun KinematicOverlayCanvas(
    overlay: TimedPoseOverlay,
    activeFault: FaultMarker?,
    modifier: Modifier = Modifier
) {
    // Reuse PoseOverlayCanvas logic plus fault highlight
    val fakePoseData = com.apexai.crossfit.core.domain.model.PoseOverlayData(
        landmarks        = overlay.landmarks,
        jointAngles      = overlay.jointAngles,
        barbellPosition  = null,
        barbellTrajectory = emptyList(),
        frameTimestamp   = overlay.timestampMs
    )
    PoseOverlayCanvas(poseOverlayData = fakePoseData, modifier = modifier)

    // Fault flash border
    if (activeFault != null) {
        val faultColor = when (activeFault.severity) {
            FaultSeverity.MINOR    -> ColorWarning
            FaultSeverity.MODERATE -> BlazeOrange
            FaultSeverity.CRITICAL -> ColorError
        }
        Canvas(modifier = modifier) {
            drawRect(
                color = faultColor.copy(alpha = 0.6f),
                style = Stroke(width = 4.dp.toPx())
            )
        }
    }
}

@Composable
private fun FaultMarkersTimeline(
    faultMarkers: List<FaultMarker>,
    currentPositionMs: Long,
    durationMs: Long,
    onFaultTap: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(SurfaceDark.copy(alpha = 0.8f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Faults", style = ApexTypography.labelSmall, color = TextSecondary,
            modifier = Modifier.size(width = 44.dp, height = 24.dp))
        Box(modifier = Modifier.weight(1f).height(24.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Track
                drawLine(
                    color = BorderSubtle,
                    start = Offset(0f, size.height / 2),
                    end   = Offset(size.width, size.height / 2),
                    strokeWidth = 2.dp.toPx()
                )
                // Fault dots
                faultMarkers.forEach { fault ->
                    val x = if (durationMs > 0) (fault.timestampMs.toFloat() / durationMs) * size.width else 0f
                    val isActive = kotlin.math.abs(fault.timestampMs - currentPositionMs) <= 500L
                    val faultColor = when (fault.severity) {
                        FaultSeverity.MINOR    -> ColorWarning
                        FaultSeverity.MODERATE -> BlazeOrange
                        FaultSeverity.CRITICAL -> ColorError
                    }
                    drawCircle(
                        color  = faultColor,
                        radius = if (isActive) 8.dp.toPx() else 6.dp.toPx(),
                        center = Offset(x, size.height / 2)
                    )
                }
                // Playhead
                val playheadX = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs) * size.width else 0f
                drawLine(
                    color = ElectricBlue,
                    start = Offset(playheadX, 0f),
                    end   = Offset(playheadX, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
            // Tap targets for faults
            faultMarkers.forEach { fault ->
                val fraction = if (durationMs > 0) fault.timestampMs.toFloat() / durationMs else 0f
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterStart)
                        .then(
                            Modifier.padding(
                                start = (fraction * 300).dp.coerceIn(0.dp, 300.dp)
                            )
                        )
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null,
                            onClick = { onFaultTap(fault.timestampMs) }
                        )
                )
            }
        }
    }
}

@Composable
private fun RepBreakdownPanel(
    repCount: Int,
    faultMarkers: List<FaultMarker>,
    onRepClick: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("$repCount Reps Analyzed", style = ApexTypography.titleMedium, color = TextPrimary)
            Icon(Icons.Outlined.ArrowBack, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
        }
        Text("REP BREAKDOWN", style = ApexTypography.labelSmall, color = TextSecondary,
            modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(8.dp))
        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items((1..repCount).toList()) { repNumber ->
                val repFaults = faultMarkers.filter { true } // simplified
                val worstSeverity = repFaults.maxByOrNull { it.severity.ordinal }?.severity
                val borderCol = when (worstSeverity) {
                    FaultSeverity.CRITICAL -> ColorError
                    FaultSeverity.MODERATE -> BlazeOrange
                    FaultSeverity.MINOR    -> ColorWarning
                    null                   -> BorderSubtle
                }
                Column(
                    modifier = Modifier
                        .size(width = 80.dp, height = 100.dp)
                        .background(SurfaceCard, CornerMedium)
                        .border(1.dp, borderCol, CornerMedium)
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Rep $repNumber", style = ApexTypography.labelSmall, color = TextSecondary)
                    Spacer(Modifier.height(4.dp))
                    val statusColor = worstSeverity?.let {
                        when (it) {
                            FaultSeverity.CRITICAL -> ColorError
                            FaultSeverity.MODERATE -> BlazeOrange
                            FaultSeverity.MINOR    -> ColorWarning
                        }
                    } ?: NeonGreen
                    Icon(
                        if (worstSeverity == null) Icons.Outlined.PlayArrow else Icons.Outlined.ArrowBack,
                        null,
                        tint = statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        if (worstSeverity == null) "Clean" else "${repFaults.size}",
                        style = ApexTypography.labelSmall,
                        color = statusColor
                    )
                }
            }
        }
        Spacer(Modifier.windowInsetsPadding(WindowInsets.navigationBars).height(16.dp))
    }
}

private fun Modifier.border(w: androidx.compose.ui.unit.Dp, c: Color, s: androidx.compose.ui.graphics.Shape): Modifier =
    this.then(Modifier.border(androidx.compose.foundation.BorderStroke(w, c), s))
