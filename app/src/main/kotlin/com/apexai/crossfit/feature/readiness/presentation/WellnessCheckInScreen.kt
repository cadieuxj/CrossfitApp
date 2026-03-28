package com.apexai.crossfit.feature.readiness.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apexai.crossfit.core.ui.components.ApexCard
import com.apexai.crossfit.core.ui.components.PrimaryButton
import com.apexai.crossfit.core.ui.theme.ApexTypography
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.ColorError
import com.apexai.crossfit.core.ui.theme.ColorWarning
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.NeonGreen
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary

/**
 * Morning wellness check-in screen.
 *
 * Collects three subjective readiness metrics — soreness, perceived readiness,
 * and mood/stress — on a 1–5 scale. These are combined with biometric Health
 * Connect data in the ACWR readiness model to improve accuracy on days where
 * wearable data is unavailable or outlying.
 *
 * Design principle: 30-second completion target. Three sliders, one button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WellnessCheckInScreen(
    viewModel: WellnessCheckInViewModel,
    onNavigateBack: () -> Unit
) {
    var soreness          by remember { mutableFloatStateOf(3f) }
    var perceivedReadiness by remember { mutableFloatStateOf(3f) }
    var mood              by remember { mutableFloatStateOf(3f) }
    var errorMessage      by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is WellnessCheckInEffect.NavigateBack    -> onNavigateBack()
                is WellnessCheckInEffect.ShowError       -> errorMessage = effect.msg
            }
        }
    }

    errorMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            confirmButton = { TextButton(onClick = { errorMessage = null }) { Text("OK") } },
            title = { Text("Save failed") },
            text  = { Text(msg) }
        )
    }

    Scaffold(
        containerColor = BackgroundDeepBlack,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Morning Check-In", style = ApexTypography.headlineMedium, color = TextPrimary) },
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                "How are you feeling today?",
                style = ApexTypography.bodyLarge,
                color = TextSecondary
            )

            // Soreness
            WellnessSlider(
                label       = "Overall Soreness",
                description = "Rate your whole-body muscle soreness right now",
                value       = soreness,
                onValueChange = { soreness = it },
                lowLabel    = "None",
                highLabel   = "Severe",
                activeColor = when (soreness.toInt()) {
                    in 1..2 -> NeonGreen
                    3       -> ColorWarning
                    else    -> ColorError
                }
            )

            // Perceived readiness
            WellnessSlider(
                label       = "Perceived Readiness",
                description = "How ready do you feel to train hard today?",
                value       = perceivedReadiness,
                onValueChange = { perceivedReadiness = it },
                lowLabel    = "Not ready",
                highLabel   = "Peak",
                activeColor = when (perceivedReadiness.toInt()) {
                    in 4..5 -> NeonGreen
                    3       -> ColorWarning
                    else    -> ColorError
                }
            )

            // Mood / stress
            WellnessSlider(
                label       = "Mood / Stress",
                description = "How is your mental state? 1 = highly stressed, 5 = calm & motivated",
                value       = mood,
                onValueChange = { mood = it },
                lowLabel    = "Stressed",
                highLabel   = "Great",
                activeColor = when (mood.toInt()) {
                    in 4..5 -> NeonGreen
                    3       -> ColorWarning
                    else    -> ColorError
                }
            )

            Spacer(Modifier.height(8.dp))

            PrimaryButton(
                text     = "Log Check-In",
                onClick  = {
                    viewModel.submit(
                        soreness.toInt(),
                        perceivedReadiness.toInt(),
                        mood.toInt()
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            )

            Text(
                "Subjective data combined with biometrics for a more accurate readiness score",
                style = ApexTypography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 48.dp)
            )
        }
    }
}

@Composable
private fun WellnessSlider(
    label: String,
    description: String = "",
    value: Float,
    onValueChange: (Float) -> Unit,
    lowLabel: String,
    highLabel: String,
    activeColor: androidx.compose.ui.graphics.Color
) {
    ApexCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(label, style = ApexTypography.titleMedium, color = TextPrimary)
                    if (description.isNotBlank()) {
                        Text(description, style = ApexTypography.bodySmall, color = TextSecondary)
                    }
                }
                Text(
                    value.toInt().toString(),
                    style = ApexTypography.headlineSmall,
                    color = activeColor
                )
            }
            Slider(
                value         = value,
                onValueChange = onValueChange,
                valueRange    = 1f..5f,
                steps         = 3,
                colors        = SliderDefaults.colors(
                    thumbColor          = activeColor,
                    activeTrackColor    = activeColor,
                    inactiveTrackColor  = activeColor.copy(alpha = 0.2f)
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(lowLabel, style = ApexTypography.bodySmall, color = TextSecondary)
                Text(highLabel, style = ApexTypography.bodySmall, color = TextSecondary)
            }
        }
    }
}
