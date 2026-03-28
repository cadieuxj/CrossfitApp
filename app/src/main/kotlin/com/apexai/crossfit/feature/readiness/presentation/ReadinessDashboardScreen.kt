package com.apexai.crossfit.feature.readiness.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import com.apexai.crossfit.core.domain.model.ReadinessZone
import com.apexai.crossfit.core.ui.components.ApexBottomNavBar
import com.apexai.crossfit.core.ui.components.ApexCard
import com.apexai.crossfit.core.ui.components.ApexLinearProgressBar
import com.apexai.crossfit.core.ui.components.CircularReadinessRing
import com.apexai.crossfit.core.ui.components.ShimmerBox
import com.apexai.crossfit.core.ui.theme.ApexTypography
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.ColorError
import com.apexai.crossfit.core.ui.theme.ColorWarning
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.NeonGreen
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary
import com.apexai.crossfit.core.ui.theme.ZoneCaution
import com.apexai.crossfit.core.ui.theme.ZoneHighRisk
import com.apexai.crossfit.core.ui.theme.ZoneOptimal
import com.apexai.crossfit.core.ui.theme.ZoneUndertrained

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadinessDashboardScreen(
    viewModel: ReadinessViewModel,
    currentNavRoute: String,
    onNavigateToSetup: () -> Unit,
    onNavigateToWellnessCheckIn: () -> Unit = {},
    onBottomNavNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ReadinessEffect.NavigateToHealthConnectSetup -> onNavigateToSetup()
            }
        }
    }

    Scaffold(
        containerColor = BackgroundDeepBlack,
        topBar = {
            LargeTopAppBar(
                title = { Text("Readiness", color = TextPrimary) },
                scrollBehavior = scrollBehavior,
                actions = {
                    uiState.lastSyncedAt?.let { syncedAt ->
                        val label = formatSyncTime(syncedAt)
                        Text(
                            label,
                            style = ApexTypography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    IconButton(onClick = { viewModel.onEvent(ReadinessEvent.SyncHealthData) }) {
                        Icon(Icons.Outlined.Refresh, "Sync health data", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = BackgroundDeepBlack,
                    scrolledContainerColor = BackgroundDeepBlack
                )
            )
        },
        bottomBar = {
            ApexBottomNavBar(
                currentRoute    = currentNavRoute,
                onNavigate      = onBottomNavNavigate,
                onCameraFabClick = { onBottomNavNavigate("vision/live") }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, top = 0.dp, bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                if (uiState.isLoading) {
                    ShimmerBox(modifier = Modifier.fillMaxWidth().height(280.dp))
                } else {
                    ReadinessHeroSection(uiState = uiState)
                }
            }

            item {
                if (!uiState.isLoading) {
                    AcwrGaugeSection(uiState = uiState)
                }
            }

            item {
                if (!uiState.isLoading) {
                    BiometricCardsRow(uiState = uiState)
                }
            }

            item {
                if (uiState.recommendation.isNotBlank()) {
                    ApexCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("AI RECOMMENDATION", style = ApexTypography.labelSmall, color = TextSecondary)
                            Text(uiState.recommendation, style = ApexTypography.bodyMedium, color = TextPrimary)
                        }
                    }
                }
            }

            // Morning wellness check-in entry card
            item {
                ApexCard(
                    onClick = onNavigateToWellnessCheckIn,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("MORNING CHECK-IN", style = ApexTypography.labelSmall, color = TextSecondary)
                            Text("Log soreness, readiness & mood", style = ApexTypography.bodyMedium, color = TextPrimary)
                        }
                        Icon(Icons.Outlined.FitnessCenter, null, tint = ElectricBlue, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ReadinessHeroSection(uiState: ReadinessUiState) {
    val score = uiState.readinessScore ?: 0f
    val zone  = uiState.readinessZone ?: ReadinessZone.UNDERTRAINED
    val zoneColor = zoneColor(zone)
    val zoneLabel = zoneLabel(zone)

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularReadinessRing(
            score     = score,
            zoneColor = zoneColor,
            label     = String.format("%.1f", score),
            size      = 220.dp,
            strokeWidth = 18.dp
        )
        Spacer(Modifier.height(16.dp))
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .background(zoneColor.copy(alpha = 0.15f), com.apexai.crossfit.core.ui.theme.CornerSmall)
                .then(
                    Modifier.border(1.dp, zoneColor, com.apexai.crossfit.core.ui.theme.CornerSmall)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(zoneLabel, style = ApexTypography.labelLarge, color = zoneColor)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            uiState.recommendation.take(120),
            style = ApexTypography.bodyMedium,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(8.dp))
        // Scientific disclaimer
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Info, null, tint = TextSecondary, modifier = Modifier.size(12.dp))
            Text(
                "Score based on population research · Not individually calibrated",
                style = ApexTypography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun AcwrGaugeSection(uiState: ReadinessUiState) {
    val acute   = uiState.acuteLoad ?: 0f
    val chronic = uiState.chronicLoad ?: 0.01f

    ApexCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Training Load Ratio", style = ApexTypography.labelSmall, color = TextSecondary)
            Text("Acute:Chronic Workload (7/28 day)", style = ApexTypography.bodySmall, color = TextSecondary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Acute Load (7d)", style = ApexTypography.bodySmall, color = TextSecondary)
                    Text(String.format("%.1f", acute), style = ApexTypography.headlineSmall, color = TextPrimary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Chronic Load (28d)", style = ApexTypography.bodySmall, color = TextSecondary)
                    Text(String.format("%.1f", chronic), style = ApexTypography.headlineSmall, color = TextPrimary)
                }
            }
            ApexLinearProgressBar(
                progress = if (chronic > 0) acute / (chronic * 1.5f) else 0f,
                color    = ElectricBlue,
                modifier = Modifier.fillMaxWidth().height(8.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    "< 0.8" to ZoneUndertrained,
                    "0.8–1.3" to ZoneOptimal,
                    "1.3–1.5" to ZoneCaution,
                    "> 1.5" to ZoneHighRisk
                ).forEach { (label, color) ->
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .background(color.copy(alpha = 0.12f), com.apexai.crossfit.core.ui.theme.CornerSmall)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(label, style = ApexTypography.labelSmall, color = color)
                    }
                }
            }
        }
    }
}

@Composable
private fun BiometricCardsRow(uiState: ReadinessUiState) {
    Column {
        Text("BIOMETRICS", style = ApexTypography.labelSmall, color = TextSecondary,
            modifier = Modifier.padding(bottom = 12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // HRV card
            ApexCard(modifier = Modifier.weight(1f).height(110.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Outlined.Refresh, null, tint = ElectricBlue, modifier = Modifier.size(20.dp))
                    Text("HRV", style = ApexTypography.labelSmall, color = TextSecondary)
                    val hrv = uiState.latestHrv
                    val hrvColor = when {
                        hrv == null    -> TextSecondary
                        hrv > 60       -> NeonGreen
                        hrv in 40..60  -> ColorWarning
                        else           -> ColorError
                    }
                    Text(
                        if (hrv != null) "$hrv ms" else "—",
                        style = ApexTypography.headlineSmall,
                        color = hrvColor
                    )
                    Text("RMSSD", style = ApexTypography.labelSmall, color = TextSecondary)
                }
            }
            // Sleep card
            ApexCard(modifier = Modifier.weight(1f).height(110.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Outlined.Refresh, null, tint = ElectricBlue, modifier = Modifier.size(20.dp))
                    Text("SLEEP", style = ApexTypography.labelSmall, color = TextSecondary)
                    val mins = uiState.sleepDurationMinutes
                    Text(
                        if (mins != null) "${mins / 60}h ${mins % 60}m" else "—",
                        style = ApexTypography.headlineSmall,
                        color = TextPrimary
                    )
                    Text("Last night", style = ApexTypography.labelSmall, color = TextSecondary)
                }
            }
            // Resting HR card
            ApexCard(modifier = Modifier.weight(1f).height(110.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Outlined.Refresh, null, tint = ElectricBlue, modifier = Modifier.size(20.dp))
                    Text("RESTING HR", style = ApexTypography.labelSmall, color = TextSecondary)
                    Text(
                        if (uiState.restingHr != null) "${uiState.restingHr} bpm" else "—",
                        style = ApexTypography.headlineSmall,
                        color = TextPrimary
                    )
                    Text("BPM", style = ApexTypography.labelSmall, color = TextSecondary)
                }
            }
        }
    }
}

fun zoneColor(zone: ReadinessZone) = when (zone) {
    ReadinessZone.OPTIMAL      -> ZoneOptimal
    ReadinessZone.CAUTION      -> ZoneCaution
    ReadinessZone.HIGH_RISK    -> ZoneHighRisk
    ReadinessZone.UNDERTRAINED -> ZoneUndertrained
    ReadinessZone.ONBOARDING   -> ElectricBlue
}

fun zoneLabel(zone: ReadinessZone) = when (zone) {
    ReadinessZone.OPTIMAL      -> "OPTIMAL TRAINING ZONE"
    ReadinessZone.CAUTION      -> "CAUTION — MONITOR LOAD"
    ReadinessZone.HIGH_RISK    -> "HIGH INJURY RISK"
    ReadinessZone.UNDERTRAINED -> "UNDERTRAINED"
    ReadinessZone.ONBOARDING   -> "LOG 7 SESSIONS TO UNLOCK"
}

private fun formatSyncTime(syncedAt: Instant): String {
    val now = Instant.now()
    val minutesAgo = ChronoUnit.MINUTES.between(syncedAt, now)
    return when {
        minutesAgo < 60  -> "Synced ${minutesAgo}m ago"
        minutesAgo < 1440 -> "Synced " + syncedAt.atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("h:mm a"))
        else -> "Synced " + syncedAt.atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("MMM d"))
    }
}

private fun Modifier.border(w: androidx.compose.ui.unit.Dp, c: androidx.compose.ui.graphics.Color, s: androidx.compose.ui.graphics.Shape): Modifier =
    this.then(Modifier.border(androidx.compose.foundation.BorderStroke(w, c), s))
