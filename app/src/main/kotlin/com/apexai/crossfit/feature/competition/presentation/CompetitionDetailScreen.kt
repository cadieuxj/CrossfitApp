package com.apexai.crossfit.feature.competition.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apexai.crossfit.core.domain.model.CompetitionStanding
import com.apexai.crossfit.core.domain.model.CompetitionStatus
import com.apexai.crossfit.core.ui.components.ApexTextField
import com.apexai.crossfit.core.ui.components.PrimaryButton
import com.apexai.crossfit.core.ui.components.ShimmerBox
import com.apexai.crossfit.core.ui.theme.ApexTypography
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.BlazeOrange
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.ColorError
import com.apexai.crossfit.core.ui.theme.CornerMedium
import com.apexai.crossfit.core.ui.theme.CornerXLarge
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.NeonGreen
import com.apexai.crossfit.core.ui.theme.SurfaceElevated
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary
import java.time.format.DateTimeFormatter

private val DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitionDetailScreen(
    viewModel: CompetitionDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { /* error handling could be added here */ }
    }

    Scaffold(
        containerColor = BackgroundDeepBlack,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        uiState.event?.name ?: "",
                        style = ApexTypography.headlineMedium,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundDeepBlack
                )
            )
        },
        floatingActionButton = {
            val event = uiState.event
            if (event != null && event.status == CompetitionStatus.ACTIVE) {
                FloatingActionButton(
                    onClick = { viewModel.onEvent(CompetitionDetailEvent.EnterStanding) },
                    containerColor = ElectricBlue,
                    contentColor = BackgroundDeepBlack
                ) {
                    Icon(Icons.Outlined.Add, "Log standing")
                }
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.padding(innerPadding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShimmerBox(Modifier.fillMaxWidth().height(100.dp))
                ShimmerBox(Modifier.fillMaxWidth().height(200.dp))
            }
        } else {
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 80.dp
                ),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Event details card
                uiState.event?.let { event ->
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CornerMedium)
                                .background(SurfaceElevated)
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(event.type.name, style = ApexTypography.labelSmall, color = ElectricBlue)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "${event.startDate.format(DATE_FMT)} – ${event.endDate.format(DATE_FMT)}",
                                        style = ApexTypography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                                StatusBadge(event.status.name)
                            }
                            event.description?.let { desc ->
                                Spacer(Modifier.height(12.dp))
                                Text(desc, style = ApexTypography.bodySmall, color = TextSecondary)
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                }

                // My standings section
                item {
                    Text("MY SUBMISSIONS", style = ApexTypography.labelSmall, color = TextSecondary,
                        modifier = Modifier.padding(bottom = 12.dp))
                }

                if (uiState.standings.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Outlined.EmojiEvents, null,
                                    tint = TextSecondary, modifier = Modifier.size(40.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("No submissions yet", style = ApexTypography.bodyMedium, color = TextSecondary)
                                if (uiState.event?.status == CompetitionStatus.ACTIVE) {
                                    Text(
                                        "Tap + to log your official score",
                                        style = ApexTypography.bodySmall, color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(uiState.standings) { standing ->
                        StandingCard(
                            standing = standing,
                            onDelete = { viewModel.onEvent(CompetitionDetailEvent.DeleteStanding(standing.id)) }
                        )
                        HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                    }
                }
            }
        }
    }

    // Standing entry sheet
    if (uiState.showEntrySheet) {
        StandingEntrySheet(
            state = uiState,
            onEvent = viewModel::onEvent
        )
    }
}

@Composable
private fun StatusBadge(label: String) {
    val color = when (label) {
        "ACTIVE"    -> NeonGreen
        "UPCOMING"  -> ElectricBlue
        else        -> TextSecondary
    }
    Box(
        modifier = Modifier
            .clip(CornerMedium)
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.4f), CornerMedium)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(label, style = ApexTypography.labelSmall, color = color)
    }
}

@Composable
private fun StandingCard(standing: CompetitionStanding, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(standing.workoutName, style = ApexTypography.titleMedium, color = TextPrimary)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(standing.score, style = ApexTypography.headlineSmall, color = ElectricBlue)
                Text(standing.division, style = ApexTypography.bodySmall, color = TextSecondary)
                standing.rankOverall?.let {
                    Text("Rank #$it", style = ApexTypography.bodySmall, color = NeonGreen)
                }
                standing.percentile?.let {
                    Text("Top ${it.toInt()}%", style = ApexTypography.bodySmall, color = BlazeOrange)
                }
            }
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Outlined.Delete, "Delete", tint = ColorError, modifier = Modifier.size(18.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StandingEntrySheet(
    state: CompetitionDetailUiState,
    onEvent: (CompetitionDetailEvent) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { onEvent(CompetitionDetailEvent.DismissEntrySheet) },
        sheetState = sheetState,
        containerColor = SurfaceElevated,
        shape = CornerXLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .windowInsetsPadding(WindowInsets.navigationBars),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Log Official Score", style = ApexTypography.headlineMedium, color = TextPrimary)

            ApexTextField(
                value = state.entryWorkoutName,
                onValueChange = { onEvent(CompetitionDetailEvent.WorkoutNameChanged(it)) },
                label = "Workout name (e.g. 25.1)",
                modifier = Modifier.fillMaxWidth()
            )

            ApexTextField(
                value = state.entryScore,
                onValueChange = { onEvent(CompetitionDetailEvent.ScoreChanged(it)) },
                label = "Score (reps, time, load...)",
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ApexTextField(
                    value = state.entryRankOverall,
                    onValueChange = { onEvent(CompetitionDetailEvent.RankChanged(it)) },
                    label = "Overall rank",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                ApexTextField(
                    value = state.entryPercentile,
                    onValueChange = { onEvent(CompetitionDetailEvent.PercentileChanged(it)) },
                    label = "Percentile (%)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }

            // Division selector
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("RX", "SCALED", "FOUNDATION").forEach { div ->
                    val selected = state.entryDivision == div
                    Box(
                        modifier = Modifier
                            .clip(CornerMedium)
                            .background(if (selected) ElectricBlue.copy(0.2f) else BackgroundDeepBlack)
                            .border(1.dp, if (selected) ElectricBlue else BorderSubtle, CornerMedium)
                            .clickable { onEvent(CompetitionDetailEvent.DivisionChanged(div)) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(div, style = ApexTypography.labelLarge,
                            color = if (selected) ElectricBlue else TextSecondary)
                    }
                }
            }

            PrimaryButton(
                text = "Save",
                onClick = { onEvent(CompetitionDetailEvent.SubmitStanding) },
                enabled = state.entryWorkoutName.isNotBlank() && state.entryScore.isNotBlank() && !state.isSubmitting,
                isLoading = state.isSubmitting,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}
