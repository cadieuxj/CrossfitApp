package com.apexai.crossfit.feature.vision.presentation.coaching

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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AlertTriangle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.apexai.crossfit.core.domain.model.CoachingReport
import com.apexai.crossfit.core.domain.model.FaultSeverity
import com.apexai.crossfit.core.domain.model.MovementFault
import com.apexai.crossfit.core.ui.components.ApexCard
import com.apexai.crossfit.core.ui.components.PrimaryButton
import com.apexai.crossfit.core.ui.components.SecondaryButton
import com.apexai.crossfit.core.ui.components.ShimmerBox
import com.apexai.crossfit.core.ui.theme.ApexTypography
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.BlazeOrange
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.ColorError
import com.apexai.crossfit.core.ui.theme.ColorWarning
import com.apexai.crossfit.core.ui.theme.CornerMedium
import com.apexai.crossfit.core.ui.theme.CornerSmall
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.SurfaceDark
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachingReportScreen(
    viewModel: CoachingViewModel,
    onNavigateHome: () -> Unit,
    onNavigateToPlayback: (String, Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is CoachingEffect.NavigateToPlayback ->
                    onNavigateToPlayback(effect.videoId, effect.timestampMs)
            }
        }
    }

    Scaffold(
        containerColor = BackgroundDeepBlack,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("AI Coaching Report", style = ApexTypography.headlineMedium, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateHome) {
                        Icon(Icons.Outlined.Close, "Close", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.Share, "Share", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundDeepBlack)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                PrimaryButton(
                    text = "Done — Back to Home",
                    onClick = onNavigateHome,
                    leadingIcon = {
                        Icon(Icons.Outlined.Home, null, modifier = Modifier.padding(end = 4.dp))
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                )
            }
        }
    ) { innerPadding ->
        when (uiState.analysisStatus) {
            AnalysisStatus.ANALYZING, AnalysisStatus.IDLE -> {
                Box(
                    modifier = Modifier.padding(innerPadding).fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        CircularProgressIndicator(color = ElectricBlue, modifier = Modifier.size(56.dp))
                        Text("Analyzing your movement...", style = ApexTypography.bodyMedium, color = TextSecondary)
                    }
                }
            }
            AnalysisStatus.ERROR -> {
                Column(
                    modifier = Modifier.padding(innerPadding).padding(32.dp).fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Outlined.AlertTriangle, null, tint = ColorError, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Analysis Failed", style = ApexTypography.headlineMedium, color = TextPrimary)
                    Text(uiState.error ?: "Something went wrong.", style = ApexTypography.bodyMedium, color = TextSecondary)
                    Spacer(Modifier.height(24.dp))
                    PrimaryButton("Retry Analysis", onClick = { viewModel.onEvent(CoachingEvent.RetryAnalysis) },
                        modifier = Modifier.fillMaxWidth())
                    SecondaryButton("Go Back", onClick = onNavigateHome, modifier = Modifier.fillMaxWidth())
                }
            }
            AnalysisStatus.COMPLETE -> {
                uiState.report?.let { report ->
                    ReportContent(
                        report = report,
                        modifier = Modifier.padding(innerPadding),
                        onFaultClick = { fault -> viewModel.onFaultSelected(fault) }
                    )
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun ReportContent(
    report: CoachingReport,
    modifier: Modifier,
    onFaultClick: (MovementFault) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary card
        item {
            ApexCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("OVERALL ASSESSMENT", style = ApexTypography.labelSmall, color = TextSecondary)
                    Text(report.overallAssessment, style = ApexTypography.bodyLarge, color = TextPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            "${report.repCount} reps",
                            style = ApexTypography.bodyMedium,
                            color = ElectricBlue
                        )
                        report.estimatedWeight?.let {
                            Text("~${it.toInt()} kg", style = ApexTypography.bodyMedium, color = ElectricBlue)
                        }
                    }
                    Text(report.movementType, style = ApexTypography.bodySmall, color = TextSecondary)
                }
            }
        }

        // Global cues
        if (report.globalCues.isNotEmpty()) {
            item {
                Text("COACHING CUES", style = ApexTypography.labelSmall, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    report.globalCues.forEach { cue ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(ElectricBlue, com.apexai.crossfit.core.ui.theme.CornerFull)
                                    .align(Alignment.Top)
                                    .padding(top = 8.dp)
                            )
                            Text(cue, style = ApexTypography.bodyMedium, color = TextPrimary)
                        }
                    }
                }
            }
        }

        // Faults header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("MOVEMENT FAULTS", style = ApexTypography.labelSmall, color = TextSecondary)
                val faultCountColor = when {
                    report.faults.any { it.severity == FaultSeverity.CRITICAL } -> ColorError
                    report.faults.any { it.severity == FaultSeverity.MODERATE } -> BlazeOrange
                    else -> ColorWarning
                }
                Text(
                    "${report.faults.size} found",
                    style = ApexTypography.bodySmall,
                    color = faultCountColor
                )
            }
        }

        items(report.faults) { fault ->
            FaultCard(fault = fault, onClick = { onFaultClick(fault) })
        }

        // AI disclaimer — always shown at bottom of report
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark.copy(alpha = 0.6f), CornerSmall)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.AlertTriangle,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    "AI-generated analysis · Not coach-validated · CRITICAL flags should be reviewed with a qualified coach",
                    style = ApexTypography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun FaultCard(fault: MovementFault, onClick: () -> Unit) {
    val severityColor = when (fault.severity) {
        FaultSeverity.MINOR    -> ColorWarning
        FaultSeverity.MODERATE -> BlazeOrange
        FaultSeverity.CRITICAL -> ColorError
    }

    ApexCard(
        onClick = onClick,
        borderColor = severityColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.Top) {
            // Left severity bar
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = 80.dp)
                    .background(severityColor, com.apexai.crossfit.core.ui.theme.CornerFull)
            )
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .background(severityColor.copy(alpha = 0.15f), CornerSmall)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(fault.severity.name, style = ApexTypography.labelLarge, color = severityColor)
                    }
                    Text(
                        formatTimestamp(fault.timestampMs),
                        style = ApexTypography.bodySmall,
                        color = TextSecondary
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(fault.description, style = ApexTypography.bodyLarge, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Outlined.Lightbulb, null, tint = ElectricBlue, modifier = Modifier.size(16.dp))
                    Text(
                        fault.cue,
                        style = ApexTypography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                        color = ElectricBlue
                    )
                }
                fault.correctedImageUrl?.let { url ->
                    Spacer(Modifier.height(12.dp))
                    AsyncImage(
                        model = url,
                        contentDescription = "AI-generated corrected form for ${fault.description}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(CornerMedium),
                        contentScale = ContentScale.Crop
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    com.apexai.crossfit.core.ui.components.ApexTextButton(
                        text = "View in Video →",
                        onClick = onClick
                    )
                }
            }
        }
    }
}

private fun formatTimestamp(ms: Long): String {
    val s = ms / 1000
    return "${(s / 60).toString().padStart(2, '0')}:${(s % 60).toString().padStart(2, '0')}.${((ms % 1000) / 100)}"
}
