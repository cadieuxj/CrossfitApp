package com.apexai.crossfit.feature.wod.presentation.log

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apexai.crossfit.core.domain.model.PersonalRecord
import com.apexai.crossfit.core.domain.model.ScoringMetric
import com.apexai.crossfit.core.ui.components.ApexCard
import com.apexai.crossfit.core.ui.components.ApexTextField
import com.apexai.crossfit.core.ui.components.PrimaryButton
import com.apexai.crossfit.core.ui.theme.ApexTypography
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.ColorError
import com.apexai.crossfit.core.ui.theme.ColorWarning
import com.apexai.crossfit.core.ui.theme.CornerFull
import com.apexai.crossfit.core.ui.theme.CornerMedium
import com.apexai.crossfit.core.ui.theme.CornerSmall
import com.apexai.crossfit.core.ui.theme.CornerXLarge
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.NeonGreen
import com.apexai.crossfit.core.ui.theme.SurfaceDark
import com.apexai.crossfit.core.ui.theme.SurfaceElevated
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WodLogScreen(
    viewModel: WodLogViewModel,
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showPrSheet by remember { mutableStateOf(false) }
    var prList by remember { mutableStateOf<List<PersonalRecord>>(emptyList()) }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is WodLogEffect.PrAchieved -> {
                    prList = effect.prs
                    showPrSheet = true
                }
                is WodLogEffect.NavigateBack -> onNavigateBack()
                is WodLogEffect.ShowError    -> { /* error shown in state */ }
            }
        }
    }

    Scaffold(
        containerColor = BackgroundDeepBlack,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Log Result", style = ApexTypography.headlineMedium, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundDeepBlack)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // WOD name header
            uiState.workout?.let { wod ->
                Text(wod.name, style = ApexTypography.titleLarge, color = TextPrimary)
                Text(wod.timeDomain.name, style = ApexTypography.bodySmall, color = TextSecondary)
                Spacer(Modifier.height(24.dp))
            }

            // Score section
            Text("SCORE", style = ApexTypography.labelSmall, color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp))
            ScoreInputSection(
                scoringMetric = uiState.workout?.scoringMetric,
                score         = uiState.score,
                onScoreChanged = { viewModel.onEvent(WodLogEvent.ScoreChanged(it)) }
            )

            Spacer(Modifier.height(24.dp))

            // Rxd toggle
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("AS PRESCRIBED (Rx)", style = ApexTypography.titleMedium, color = TextPrimary)
                    Text(
                        "Did you use the prescribed weights?",
                        style = ApexTypography.bodySmall, color = TextSecondary
                    )
                }
                Switch(
                    checked = uiState.rxd,
                    onCheckedChange = { viewModel.onEvent(WodLogEvent.RxdToggled(it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = BackgroundDeepBlack,
                        checkedTrackColor = ElectricBlue,
                        uncheckedTrackColor = BorderSubtle
                    )
                )
            }

            Spacer(Modifier.height(24.dp))

            // Competition mode section — auto-appears when there's an active event
            if (uiState.isActiveCompetitionEvent) {
                CompetitionModeSection(
                    eventName          = uiState.activeCompetitionName ?: "Active Competition",
                    isOfficialSubmission = uiState.isOfficialSubmission,
                    onToggle           = { viewModel.onEvent(WodLogEvent.OfficialSubmissionToggled(it)) }
                )
                Spacer(Modifier.height(24.dp))
            }

            // RPE
            Text("EFFORT LEVEL (RPE)", style = ApexTypography.labelSmall, color = TextSecondary,
                modifier = Modifier.padding(bottom = 12.dp))
            RpeSelector(
                selectedRpe = uiState.rpe,
                onRpeSelected = { viewModel.onEvent(WodLogEvent.RpeSelected(it)) }
            )

            Spacer(Modifier.height(24.dp))

            // Session duration (used for session-RPE training load calculation)
            Text("SESSION DURATION", style = ApexTypography.labelSmall, color = TextSecondary,
                modifier = Modifier.padding(bottom = 12.dp))
            DurationPickerSection(
                durationMinutes = uiState.sessionDurationMinutes,
                onDurationChanged = { viewModel.onEvent(WodLogEvent.DurationChanged(it)) }
            )

            Spacer(Modifier.height(24.dp))

            // Notes
            Text("NOTES", style = ApexTypography.labelSmall, color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp))
            ApexTextField(
                value = uiState.notes,
                onValueChange = { if (it.length <= 500) viewModel.onEvent(WodLogEvent.NotesChanged(it)) },
                label = "How did it feel? Movement notes...",
                minLines = 3,
                maxLines = 6,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "${uiState.notes.length}/500",
                style = ApexTypography.bodySmall, color = TextSecondary,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End
            )

            if (uiState.error != null) {
                Spacer(Modifier.height(8.dp))
                Text(uiState.error!!, style = ApexTypography.bodySmall, color = ColorError)
            }

            Spacer(Modifier.height(32.dp))

            PrimaryButton(
                text      = "Submit Result",
                onClick   = { viewModel.onEvent(WodLogEvent.SubmitClicked) },
                isLoading = uiState.isSubmitting,
                enabled   = uiState.score.isNotBlank() && !uiState.isSubmitting,
                modifier  = Modifier.fillMaxWidth().height(56.dp)
            )
            Spacer(Modifier.height(48.dp))
        }
    }

    // PR Celebration Bottom Sheet
    if (showPrSheet) {
        PrCelebrationSheet(
            prs = prList,
            onDismiss = {
                showPrSheet = false
                viewModel.onEvent(WodLogEvent.DismissPrSheet)
            }
        )
    }
}

@Composable
private fun ScoreInputSection(
    scoringMetric: ScoringMetric?,
    score: String,
    onScoreChanged: (String) -> Unit
) {
    when (scoringMetric) {
        ScoringMetric.TIME -> {
            val parts = score.split(":")
            val minutes = parts.getOrNull(0) ?: ""
            val seconds = parts.getOrNull(1) ?: ""
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                ApexTextField(
                    value = minutes,
                    onValueChange = { onScoreChanged("$it:$seconds") },
                    label = "Minutes",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Text(":", style = ApexTypography.headlineMedium, color = TextSecondary)
                ApexTextField(
                    value = seconds,
                    onValueChange = { onScoreChanged("$minutes:$it") },
                    label = "Seconds",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        ScoringMetric.ROUNDS_PLUS_REPS -> {
            val parts = score.split("+")
            val rounds = parts.getOrNull(0) ?: ""
            val reps   = parts.getOrNull(1) ?: ""
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                ApexTextField(
                    value = rounds,
                    onValueChange = { onScoreChanged("$it+$reps") },
                    label = "Rounds",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Text("+", style = ApexTypography.headlineMedium, color = TextSecondary)
                ApexTextField(
                    value = reps,
                    onValueChange = { onScoreChanged("$rounds+$it") },
                    label = "Reps",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        ScoringMetric.LOAD -> {
            ApexTextField(
                value = score,
                onValueChange = onScoreChanged,
                label = "Weight (kg)",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        }
        else -> {
            ApexTextField(
                value = score,
                onValueChange = onScoreChanged,
                label = "Total Reps",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun RpeSelector(selectedRpe: Int?, onRpeSelected: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        (1..10).forEach { value ->
            val rpeColor = when (value) {
                in 1..4  -> NeonGreen
                in 5..7  -> ColorWarning
                else     -> ColorError
            }
            val isSelected = selectedRpe == value
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(CornerSmall)
                    .background(if (isSelected) rpeColor.copy(alpha = 0.2f) else SurfaceElevated)
                    .border(
                        1.dp,
                        if (isSelected) rpeColor else BorderSubtle,
                        CornerSmall
                    )
                    .clickable { onRpeSelected(value) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    value.toString(),
                    style = ApexTypography.labelLarge,
                    color = if (isSelected) rpeColor else TextSecondary
                )
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    Text(
        text = rpeDescription(selectedRpe),
        style = ApexTypography.bodySmall,
        color = TextSecondary
    )
}

@Composable
private fun DurationPickerSection(durationMinutes: Int?, onDurationChanged: (Int) -> Unit) {
    val current = durationMinutes ?: 0
    val scope   = rememberCoroutineScope()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Decrement — tap −1, hold >400ms to ramp −5 every 150ms; cancels on release
        val canDecrement = durationMinutes != null && current > 1
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CornerFull)
                .background(SurfaceElevated)
                .then(
                    if (canDecrement) Modifier.pointerInput(current) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            var longPressTriggered = false
                            val holdJob = scope.launch {
                                delay(400L)
                                longPressTriggered = true
                                var held = current
                                while (held > 1) {
                                    held = (held - 5).coerceAtLeast(1)
                                    onDurationChanged(held)
                                    delay(150L)
                                }
                            }
                            waitForUpOrCancellation()
                            holdJob.cancel()
                            if (!longPressTriggered) {
                                onDurationChanged((current - 1).coerceAtLeast(1))
                            }
                        }
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("−", style = ApexTypography.headlineMedium,
                color = if (canDecrement) TextPrimary else TextSecondary)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
            Text(
                if (current > 0) "$current min" else "—",
                style = ApexTypography.headlineSmall,
                color = if (current > 0) ElectricBlue else TextSecondary
            )
            Text("1–240 min  •  hold to jump ±5", style = ApexTypography.bodySmall, color = TextSecondary)
        }

        // Increment — tap +1, hold >400ms to ramp +5 every 150ms; cancels on release
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CornerFull)
                .background(SurfaceElevated)
                .pointerInput(current) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        var longPressTriggered = false
                        val holdJob = scope.launch {
                            delay(400L)
                            longPressTriggered = true
                            var held = current
                            while (held < 240) {
                                held = (held + 5).coerceAtMost(240)
                                onDurationChanged(held)
                                delay(150L)
                            }
                        }
                        waitForUpOrCancellation()
                        holdJob.cancel()
                        if (!longPressTriggered) {
                            onDurationChanged((current + 1).coerceAtMost(240))
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text("+", style = ApexTypography.headlineMedium, color = TextPrimary)
        }
    }
    Spacer(Modifier.height(4.dp))
    Text(
        "Used to calculate your Training Load score (session duration × RPE)",
        style = ApexTypography.bodySmall,
        color = TextSecondary
    )
}

@Composable
private fun CompetitionModeSection(
    eventName: String,
    isOfficialSubmission: Boolean,
    onToggle: (Boolean) -> Unit
) {
    ApexCard(
        borderColor = if (isOfficialSubmission) NeonGreen else BorderSubtle,
        borderWidth = if (isOfficialSubmission) 2.dp else 1.dp
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Outlined.WorkspacePremium,
                    contentDescription = null,
                    tint = if (isOfficialSubmission) NeonGreen else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    "COMPETITION MODE",
                    style = ApexTypography.labelSmall,
                    color = if (isOfficialSubmission) NeonGreen else TextSecondary
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Official Submission",
                        style = ApexTypography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        eventName,
                        style = ApexTypography.bodySmall,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = isOfficialSubmission,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = BackgroundDeepBlack,
                        checkedTrackColor = NeonGreen,
                        uncheckedTrackColor = BorderSubtle
                    )
                )
            }
            if (isOfficialSubmission) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "This result will be flagged as your official competition score.",
                    style = ApexTypography.bodySmall,
                    color = NeonGreen
                )
            }
        }
    }
}

private fun rpeDescription(rpe: Int?): String = when (rpe) {
    1    -> "Very light. Easy warm-up pace."
    2    -> "Light. Could do this all day."
    3    -> "Moderate. Comfortable pace."
    4    -> "Somewhat hard. Breathing elevated."
    5    -> "Hard. Conversation getting difficult."
    6    -> "Hard. Short sentences only."
    7    -> "Very hard. Conversation is difficult."
    8    -> "Very hard. Near your limit."
    9    -> "Extremely hard. Almost maximal effort."
    10   -> "Maximal. Absolute limit."
    else -> "Select your perceived exertion."
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrCelebrationSheet(
    prs: List<PersonalRecord>,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var trophyAnimated by remember { mutableStateOf(false) }
    val trophyScale by animateFloatAsState(
        targetValue = if (trophyAnimated) 1f else 0.5f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium),
        label = "trophy_scale"
    )

    LaunchedEffect(Unit) { trophyAnimated = true }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceElevated,
        shape = CornerXLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .windowInsetsPadding(WindowInsets.navigationBars),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Outlined.EmojiEvents,
                contentDescription = null,
                tint = NeonGreen,
                modifier = Modifier
                    .size(48.dp)
                    .graphicsLayer { scaleX = trophyScale; scaleY = trophyScale }
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "NEW PERSONAL RECORD!",
                style = ApexTypography.headlineMedium,
                color = NeonGreen,
                textAlign = TextAlign.Center
            )
            Text(
                "Outstanding performance!",
                style = ApexTypography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))

            prs.forEach { pr ->
                ApexCard(
                    borderColor = NeonGreen,
                    borderWidth = 2.dp,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(pr.movementName, style = ApexTypography.titleMedium, color = TextPrimary)
                        Text(
                            "${pr.value} ${pr.unit.name.lowercase()}",
                            style = ApexTypography.headlineMedium,
                            color = NeonGreen
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            PrimaryButton(
                text = "Awesome!",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}
