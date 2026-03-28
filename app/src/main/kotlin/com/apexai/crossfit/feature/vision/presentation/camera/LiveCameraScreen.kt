package com.apexai.crossfit.feature.vision.presentation.camera

import android.Manifest
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Camera
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FlipCameraAndroid
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.graphicsLayer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apexai.crossfit.core.ui.components.PrimaryButton
import com.apexai.crossfit.core.ui.components.ApexTextButton
import com.apexai.crossfit.core.ui.theme.ApexTypography
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.BorderVisible
import com.apexai.crossfit.core.ui.theme.ColorError
import com.apexai.crossfit.core.ui.theme.CornerSmall
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.NeonGreen
import com.apexai.crossfit.core.ui.theme.SurfaceDark
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary
import com.apexai.crossfit.feature.vision.presentation.camera.PoseOverlayCanvas
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LiveCameraScreen(
    viewModel: VisionViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToReview: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    var errorMessage by remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is VisionEffect.NavigateToReview -> onNavigateToReview(effect.videoUri)
                is VisionEffect.ShowError        -> errorMessage = effect.message
            }
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard recording?") },
            text  = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    viewModel.onEvent(VisionEvent.DiscardRecording)
                }) { Text("Discard", color = ColorError) }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("Cancel") }
            }
        )
    }

    errorMessage?.let { msg ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { errorMessage = null },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { errorMessage = null }) {
                    Text("OK")
                }
            },
            title = { Text("Error") },
            text  = { Text(msg) }
        )
    }

    if (!cameraPermission.status.isGranted) {
        CameraPermissionRequest(
            onGrantPermission = { cameraPermission.launchPermissionRequest() },
            onNavigateBack    = onNavigateBack
        )
        return
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Layer 1: CameraX PreviewView — PERFORMANCE mode mandatory (CLAUDE.md)
        val previewView = remember {
            PreviewView(context).apply {
                implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        }

        LaunchedEffect(lifecycleOwner, uiState.isFrontCamera) {
            viewModel.startCamera(lifecycleOwner, previewView)
        }

        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Layer 2: Pose overlay Canvas
        uiState.currentPoseResult?.let { poseData ->
            PoseOverlayCanvas(
                poseOverlayData = poseData,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Layer 3: UI controls
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            // Top bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(16.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(48.dp)
                            .background(SurfaceDark.copy(alpha = 0.6f), com.apexai.crossfit.core.ui.theme.CornerFull)
                    ) {
                        Icon(Icons.Outlined.ArrowBack, "Close camera", tint = TextPrimary)
                    }

                    // Camera state indicator
                    when (uiState.cameraState) {
                        CameraState.RECORDING -> {
                            Row(
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RecordingPulse()
                                Text(
                                    formatMMSS(uiState.recordingDurationMs),
                                    style = ApexTypography.titleLarge,
                                    color = TextPrimary
                                )
                            }
                        }
                        CameraState.READY -> Text("READY", style = ApexTypography.labelSmall, color = NeonGreen)
                        CameraState.INITIALIZING -> CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = ElectricBlue,
                            strokeWidth = 2.dp
                        )
                        CameraState.ERROR -> Text("CAMERA ERROR", style = ApexTypography.labelSmall, color = ColorError)
                    }

                    Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { viewModel.onEvent(VisionEvent.FlipCamera) },
                            modifier = Modifier
                                .size(48.dp)
                                .background(SurfaceDark.copy(alpha = 0.6f), com.apexai.crossfit.core.ui.theme.CornerFull)
                        ) {
                            Icon(Icons.Outlined.FlipCameraAndroid, "Flip camera", tint = TextPrimary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Depth mode badge + angle readouts
            AngleReadoutsRow(
                jointAngles = uiState.currentPoseResult?.jointAngles ?: emptyMap(),
                fps         = uiState.fps,
                depthMode   = uiState.depthMode
            )

            // Recording controls
            RecordingControls(
                isRecording      = uiState.isRecording,
                onStartRecording = { viewModel.onEvent(VisionEvent.StartRecording) },
                onStopRecording  = { viewModel.onEvent(VisionEvent.StopRecording) },
                onPauseRecording = { viewModel.onEvent(VisionEvent.PauseRecording) },
                onDiscardRequest = { showDiscardDialog = true }
            )
        }
    }
}

@Composable
private fun RecordingPulse() {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(400),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    Box(
        modifier = Modifier
            .size(12.dp)
            .then(Modifier.graphicsLayer { scaleX = scale; scaleY = scale })
            .background(ColorError, com.apexai.crossfit.core.ui.theme.CornerFull)
    )
}

@Composable
private fun AngleReadoutsRow(
    jointAngles: Map<com.apexai.crossfit.core.domain.model.JointAngle, Float>,
    fps: Int,
    depthMode: DepthMode = DepthMode.POSE_2D
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(Color.Transparent, SurfaceDark.copy(alpha = 0.8f)))
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        LazyRow(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            items(jointAngles.entries.toList()) { (joint, angle) ->
                Box(
                    modifier = Modifier
                        .background(SurfaceDark.copy(alpha = 0.8f), CornerSmall)
                        .then(Modifier.clip(CornerSmall))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)) {
                        Text(
                            JOINT_LABEL_MAP[joint.name] ?: joint.name.replace("_", " ").take(8),
                            style = ApexTypography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            "${angle.toInt()}°",
                            style = ApexTypography.labelLarge,
                            color = ElectricBlue
                        )
                    }
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .background(SurfaceDark.copy(alpha = 0.8f), CornerSmall)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("$fps fps", style = ApexTypography.labelSmall, color = TextSecondary)
                }
            }
            item {
                val (badgeLabel, badgeColor) = when (depthMode) {
                    DepthMode.DEPTH_3D    -> "3D DEPTH" to NeonGreen
                    DepthMode.POSE_2D     -> "2D POSE"  to ElectricBlue
                    DepthMode.INITIALIZING -> "…"       to TextSecondary
                }
                Box(
                    modifier = Modifier
                        .background(SurfaceDark.copy(alpha = 0.8f), CornerSmall)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(badgeLabel, style = ApexTypography.labelSmall, color = badgeColor)
                }
            }
        }
    }
}

@Composable
private fun RecordingControls(
    isRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onPauseRecording: () -> Unit,
    onDiscardRequest: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(Color.Transparent, SurfaceDark.copy(alpha = 0.8f)))
            )
            .padding(horizontal = 24.dp, bottom = 48.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isRecording) {
                Box(Modifier.size(60.dp))
                // Main record button
                RecordButton(onClick = onStartRecording)
                Box(Modifier.size(60.dp))
            } else {
                // Discard button (Delete icon + confirmation dialog)
                IconButton(
                    onClick = onDiscardRequest,
                    modifier = Modifier
                        .size(60.dp)
                        .background(SurfaceDark.copy(alpha = 0.8f), com.apexai.crossfit.core.ui.theme.CornerFull)
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        "Discard recording",
                        tint = ColorError,
                        modifier = Modifier.size(24.dp)
                    )
                }
                StopButton(onClick = onStopRecording)
                IconButton(
                    onClick = onPauseRecording,
                    modifier = Modifier
                        .size(60.dp)
                        .background(SurfaceDark.copy(alpha = 0.8f), com.apexai.crossfit.core.ui.theme.CornerFull)
                ) {
                    Icon(Icons.Outlined.Pause, "Pause recording", tint = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun RecordButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(76.dp)
            .background(Color.Transparent, com.apexai.crossfit.core.ui.theme.CornerFull)
            .then(
                Modifier
                    .clip(com.apexai.crossfit.core.ui.theme.CornerFull)
                    .background(Color.Transparent)
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(com.apexai.crossfit.core.ui.theme.CornerFull)
                .background(Color.Transparent)
                .then(
                    Modifier.border(
                        3.dp,
                        TextPrimary,
                        com.apexai.crossfit.core.ui.theme.CornerFull
                    )
                )
                .clickable { onClick() }
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.Center)
                    .clip(com.apexai.crossfit.core.ui.theme.CornerFull)
                    .background(ColorError)
            )
        }
    }
}

@Composable
private fun StopButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(76.dp)
            .border(3.dp, ColorError, com.apexai.crossfit.core.ui.theme.CornerFull)
            .clip(com.apexai.crossfit.core.ui.theme.CornerFull)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CornerSmall)
                .background(ColorError)
        )
    }
}

private fun Modifier.border(
    width: androidx.compose.ui.unit.Dp,
    color: Color,
    shape: androidx.compose.ui.graphics.Shape
): Modifier = this.then(
    Modifier.border(
        border = androidx.compose.foundation.BorderStroke(width, color),
        shape = shape
    )
)

private fun Modifier.clickable(onClick: () -> Unit): Modifier =
    this.then(
        Modifier.clickable(
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    )


@Composable
private fun CameraPermissionRequest(
    onGrantPermission: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeepBlack)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.Camera,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(64.dp)
        )
        androidx.compose.foundation.layout.Spacer(Modifier.size(24.dp))
        Text(
            "Camera Access Required",
            style = ApexTypography.headlineMedium,
            color = TextPrimary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
        Text(
            "ApexAI needs camera access to analyze your movement in real time.",
            style = ApexTypography.bodyMedium,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        androidx.compose.foundation.layout.Spacer(Modifier.size(32.dp))
        PrimaryButton(
            text = "Grant Permission",
            onClick = onGrantPermission,
            modifier = Modifier.fillMaxWidth()
        )
        ApexTextButton(text = "Not Now", onClick = onNavigateBack)
    }
}

private val JOINT_LABEL_MAP = mapOf(
    "LEFT_KNEE"        to "L.Knee",
    "RIGHT_KNEE"       to "R.Knee",
    "LEFT_HIP"         to "L.Hip",
    "RIGHT_HIP"        to "R.Hip",
    "LEFT_SHOULDER"    to "L.Shld",
    "RIGHT_SHOULDER"   to "R.Shld",
    "LEFT_ELBOW"       to "L.Elbow",
    "RIGHT_ELBOW"      to "R.Elbow",
    "LEFT_ANKLE"       to "L.Ankle",
    "RIGHT_ANKLE"      to "R.Ankle",
    "TRUNK_INCLINATION" to "Trunk"
)

private fun formatMMSS(millis: Long): String {
    val s = millis / 1000
    return "${(s / 60).toString().padStart(2, '0')}:${(s % 60).toString().padStart(2, '0')}"
}
