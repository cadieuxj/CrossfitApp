package com.apexai.crossfit.feature.wod.presentation.timer

import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.RotateCcw
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apexai.crossfit.core.domain.model.TimeDomain
import com.apexai.crossfit.core.ui.components.PrimaryButton
import com.apexai.crossfit.core.ui.theme.ApexTypography
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.ColorError
import com.apexai.crossfit.core.ui.theme.ColorWarning
import com.apexai.crossfit.core.ui.theme.CornerFull
import com.apexai.crossfit.core.ui.theme.CornerLarge
import com.apexai.crossfit.core.ui.theme.CornerSmall
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.SurfaceCard
import com.apexai.crossfit.core.ui.theme.SurfaceElevated
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary
import com.apexai.crossfit.core.ui.components.ApexLinearProgressBar

@Composable
fun WodTimerScreen(
    viewModel: WodTimerViewModel,
    onNavigateToLog: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showExitDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    // Keep screen on during active timer session
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? android.app.Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeepBlack)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showExitDialog = true }) {
                    Icon(Icons.Outlined.Close, "Exit workout", tint = TextSecondary)
                }
                Text(
                    "${uiState.workout?.name ?: ""} · ${uiState.workout?.timeDomain?.name ?: ""}",
                    style = ApexTypography.bodyMedium,
                    color = TextSecondary
                )
                val workout = uiState.workout
                if (workout?.timeDomain == TimeDomain.RFT || workout?.timeDomain == TimeDomain.EMOM) {
                    Text(
                        "Round ${uiState.currentRound}/${workout.rounds ?: "∞"}",
                        style = ApexTypography.bodyMedium,
                        color = ElectricBlue
                    )
                } else {
                    Box(modifier = Modifier.size(48.dp))
                }
            }

            // Main timer display
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                uiState.workout?.let { workout ->
                    when (workout.timeDomain) {
                        TimeDomain.AMRAP -> {
                            val remaining = (workout.timeCap?.toMillis() ?: 0L) - uiState.elapsedMillis
                            Text(
                                formatMMSS(remaining.coerceAtLeast(0L)),
                                style = ApexTypography.displayLarge.copy(
                                    fontSize = 72.sp,
                                    letterSpacing = (-2).sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = TextPrimary
                            )
                            Text("remaining", style = ApexTypography.bodyLarge, color = TextSecondary)
                        }
                        TimeDomain.EMOM -> {
                            Text(
                                "${uiState.currentIntervalSecondsRemaining.toString().padStart(2, '0')}",
                                style = ApexTypography.displayLarge.copy(fontSize = 72.sp, letterSpacing = (-2).sp),
                                color = TextPrimary
                            )
                            Text("seconds this minute", style = ApexTypography.bodyLarge, color = TextSecondary)
                            Spacer(Modifier.height(16.dp))
                            ApexLinearProgressBar(
                                progress = uiState.currentIntervalSecondsRemaining / 60f,
                                modifier = Modifier.size(width = 200.dp, height = 8.dp),
                                color = if (uiState.currentIntervalSecondsRemaining <= 10) ColorWarning else ElectricBlue
                            )
                        }
                        TimeDomain.RFT -> {
                            Text(
                                formatMMSS(uiState.elapsedMillis),
                                style = ApexTypography.displayLarge.copy(fontSize = 72.sp, letterSpacing = (-2).sp),
                                color = TextPrimary
                            )
                            Text("elapsed", style = ApexTypography.bodyLarge, color = TextSecondary)
                        }
                        TimeDomain.TABATA -> {
                            val tabataColor = if (uiState.tabataIsWorkInterval) ElectricBlue else ColorWarning
                            Text(
                                uiState.currentIntervalSecondsRemaining.toString().padStart(2, '0'),
                                style = ApexTypography.displayLarge.copy(fontSize = 72.sp, letterSpacing = (-2).sp),
                                color = tabataColor
                            )
                            Text(
                                if (uiState.tabataIsWorkInterval) "WORK" else "REST",
                                style = ApexTypography.headlineMedium,
                                color = tabataColor
                            )
                            Spacer(Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                (0 until 8).forEach { i ->
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CornerFull)
                                            .background(
                                                when {
                                                    i < uiState.tabataCompletedIntervals -> ElectricBlue
                                                    i == uiState.tabataCompletedIntervals -> ElectricBlue.copy(alpha = 0.6f)
                                                    else -> BorderSubtle
                                                }
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Movement reference list
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard, androidx.compose.foundation.shape.RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                Text(
                    "MOVEMENTS",
                    style = ApexTypography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp)
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.workout?.movements ?: emptyList()) { wm ->
                        Column(
                            modifier = Modifier
                                .background(SurfaceElevated, CornerSmall)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "${wm.prescribedReps ?: "-"}× ${wm.movement.name}",
                                style = ApexTypography.bodyMedium,
                                color = TextPrimary
                            )
                            wm.prescribedWeight?.let {
                                Text("${it}kg", style = ApexTypography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))

                // Timer controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .windowInsetsPadding(WindowInsets.navigationBars),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!uiState.isComplete) {
                        IconButton(
                            onClick = { showResetDialog = true },
                            modifier = Modifier
                                .size(48.dp)
                                .background(SurfaceElevated, CornerFull)
                        ) {
                            Icon(Icons.Outlined.RotateCcw, "Reset", tint = TextSecondary)
                        }

                        // Start/Pause FAB
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .background(
                                    if (uiState.isRunning) ColorError else ElectricBlue,
                                    CornerFull
                                )
                                .clip(CornerFull)
                                .then(
                                    Modifier.clickable { viewModel.onEvent(WodTimerEvent.StartPause) }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (uiState.isRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                                contentDescription = if (uiState.isRunning) "Pause timer" else "Start timer",
                                tint = com.apexai.crossfit.core.ui.theme.TextOnBlue,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Box(Modifier.size(48.dp))
                    } else {
                        PrimaryButton(
                            text = "Log Result",
                            onClick = { uiState.workout?.id?.let(onNavigateToLog) },
                            leadingIcon = {
                                Icon(Icons.Outlined.Check, null,
                                    modifier = Modifier.size(20.dp).padding(end = 4.dp))
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        )
                    }
                }
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Exit Workout?", color = TextPrimary) },
            text  = { Text("Your timer progress will be lost.", color = TextSecondary) },
            confirmButton = {
                com.apexai.crossfit.core.ui.components.ApexTextButton("Exit", onClick = {
                    showExitDialog = false; onNavigateBack()
                })
            },
            dismissButton = {
                com.apexai.crossfit.core.ui.components.ApexTextButton("Continue", onClick = { showExitDialog = false })
            },
            containerColor = SurfaceElevated
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Timer?", color = TextPrimary) },
            text  = { Text("This will reset the timer to zero.", color = TextSecondary) },
            confirmButton = {
                com.apexai.crossfit.core.ui.components.ApexTextButton("Reset", onClick = {
                    showResetDialog = false
                    viewModel.onEvent(WodTimerEvent.Reset)
                })
            },
            dismissButton = {
                com.apexai.crossfit.core.ui.components.ApexTextButton("Cancel", onClick = { showResetDialog = false })
            },
            containerColor = SurfaceElevated
        )
    }
}

private fun Modifier.clickable(onClick: () -> Unit): Modifier =
    this.then(Modifier.clickable(
        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
        indication = null,
        onClick = onClick
    ))

private fun formatMMSS(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}
