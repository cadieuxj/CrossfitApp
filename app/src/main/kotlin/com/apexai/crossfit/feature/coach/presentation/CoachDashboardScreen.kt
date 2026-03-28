package com.apexai.crossfit.feature.coach.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.apexai.crossfit.core.domain.model.WorkoutResult
import com.apexai.crossfit.core.ui.components.ApexCard
import com.apexai.crossfit.core.ui.components.ApexTextField
import com.apexai.crossfit.core.ui.components.PrimaryButton
import com.apexai.crossfit.core.ui.components.ShimmerBox
import com.apexai.crossfit.feature.coach.domain.AthleteInfo
import com.apexai.crossfit.core.ui.theme.ApexTypography
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.BlazeOrange
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.ColorError
import com.apexai.crossfit.core.ui.theme.CornerFull
import com.apexai.crossfit.core.ui.theme.CornerMedium
import com.apexai.crossfit.core.ui.theme.CornerXLarge
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.NeonGreen
import com.apexai.crossfit.core.ui.theme.SurfaceDark
import com.apexai.crossfit.core.ui.theme.SurfaceElevated
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val DATE_FMT = DateTimeFormatter.ofPattern("MMM d")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachDashboardScreen(
    viewModel: CoachDashboardViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { /* snackbar can go here */ }
    }

    Scaffold(
        containerColor = BackgroundDeepBlack,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Athletes", style = ApexTypography.headlineMedium, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundDeepBlack)
            )
        }
    ) { innerPadding ->
        if (uiState.isLoadingAthletes) {
            Column(Modifier.padding(innerPadding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ShimmerBox(Modifier.fillMaxWidth().height(80.dp))
                ShimmerBox(Modifier.fillMaxWidth().height(200.dp))
            }
        } else if (uiState.athletes.isEmpty()) {
            Box(Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.Person, null, tint = TextSecondary, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No athletes linked yet", style = ApexTypography.titleMedium, color = TextSecondary)
                    Text("Athletes link to you via your invite code", style = ApexTypography.bodySmall, color = TextSecondary)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Athlete selector row
                item {
                    Text("ATHLETES", style = ApexTypography.labelSmall, color = TextSecondary,
                        modifier = Modifier.padding(bottom = 12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(uiState.athletes) { athlete ->
                            AthletePill(
                                athlete  = athlete,
                                selected = uiState.selectedAthleteId == athlete.userId,
                                onClick  = { viewModel.onEvent(CoachDashboardEvent.SelectAthlete(athlete.userId)) }
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }

                // Results for selected athlete
                uiState.selectedAthlete?.let { athlete ->
                    item {
                        Text(
                            "${athlete.displayName}'s Results",
                            style = ApexTypography.labelSmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }

                if (uiState.isLoadingResults) {
                    items(3) { ShimmerBox(Modifier.fillMaxWidth().height(80.dp).padding(vertical = 4.dp)) }
                } else if (uiState.athleteResults.isEmpty()) {
                    item {
                        Text("No results yet", style = ApexTypography.bodyMedium, color = TextSecondary,
                            modifier = Modifier.padding(vertical = 24.dp))
                    }
                } else {
                    items(uiState.athleteResults) { result ->
                        AthleteResultCard(
                            result  = result,
                            onAddNote = {
                                viewModel.onEvent(CoachDashboardEvent.StartEditNote(
                                    resultId     = result.id,
                                    existingNote = result.coachNote ?: ""
                                ))
                            }
                        )
                        HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                    }
                }
            }
        }
    }

    // Coach note editing sheet
    if (uiState.editingResultId != null) {
        CoachNoteSheet(
            noteInput    = uiState.noteInput,
            isSaving     = uiState.isSavingNote,
            onNoteChange = { viewModel.onEvent(CoachDashboardEvent.NoteChanged(it)) },
            onSave       = { viewModel.onEvent(CoachDashboardEvent.SaveNote) },
            onDismiss    = { viewModel.onEvent(CoachDashboardEvent.CancelEditNote) }
        )
    }
}

@Composable
private fun AthletePill(athlete: AthleteInfo, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(CornerFull)
            .background(if (selected) ElectricBlue.copy(0.15f) else SurfaceDark)
            .border(1.dp, if (selected) ElectricBlue else BorderSubtle, CornerFull)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CornerFull)
                .background(if (selected) ElectricBlue else TextSecondary.copy(0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                athlete.displayName.first().uppercaseChar().toString(),
                style = ApexTypography.labelSmall,
                color = if (selected) BackgroundDeepBlack else TextSecondary
            )
        }
        Text(
            athlete.displayName,
            style = ApexTypography.labelLarge,
            color = if (selected) ElectricBlue else TextSecondary
        )
    }
}

@Composable
private fun AthleteResultCard(result: WorkoutResult, onAddNote: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(result.score, style = ApexTypography.headlineSmall, color = ElectricBlue)
                    if (result.rxd) {
                        Box(
                            modifier = Modifier
                                .clip(CornerMedium)
                                .background(NeonGreen.copy(0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Rx", style = ApexTypography.labelSmall, color = NeonGreen)
                        }
                    }
                    if (result.isOfficialSubmission) {
                        Box(
                            modifier = Modifier
                                .clip(CornerMedium)
                                .background(BlazeOrange.copy(0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Official", style = ApexTypography.labelSmall, color = BlazeOrange)
                        }
                    }
                }
                val date = result.completedAt.atOffset(ZoneOffset.UTC).toLocalDate().format(DATE_FMT)
                Text(date, style = ApexTypography.bodySmall, color = TextSecondary)
                result.notes?.let {
                    Text(it, style = ApexTypography.bodySmall, color = TextSecondary, maxLines = 2)
                }
            }
            IconButton(onClick = onAddNote, modifier = Modifier.size(40.dp)) {
                Icon(
                    if (result.coachNote.isNullOrBlank()) Icons.Outlined.Edit else Icons.Outlined.Check,
                    "Add coach note",
                    tint = if (result.coachNote.isNullOrBlank()) TextSecondary else NeonGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        result.coachNote?.takeIf { it.isNotBlank() }?.let { note ->
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CornerMedium)
                    .background(NeonGreen.copy(0.08f))
                    .border(1.dp, NeonGreen.copy(0.3f), CornerMedium)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Outlined.Edit, null, tint = NeonGreen, modifier = Modifier.size(14.dp))
                Text(note, style = ApexTypography.bodySmall, color = NeonGreen)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CoachNoteSheet(
    noteInput: String,
    isSaving: Boolean,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
            Text("Coach Note", style = ApexTypography.headlineMedium, color = TextPrimary)
            Text(
                "Your note will be visible to the athlete on their result.",
                style = ApexTypography.bodySmall, color = TextSecondary
            )
            ApexTextField(
                value = noteInput,
                onValueChange = onNoteChange,
                label = "Add coaching feedback...",
                minLines = 3,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth()
            )
            PrimaryButton(
                text = "Save Note",
                onClick = onSave,
                enabled = noteInput.isNotBlank() && !isSaving,
                isLoading = isSaving,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}
