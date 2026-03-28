package com.apexai.crossfit.feature.wod.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack

import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apexai.crossfit.core.domain.model.TimeDomain
import com.apexai.crossfit.core.domain.model.WorkoutMovement
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
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.NeonGreen
import com.apexai.crossfit.core.ui.theme.SurfaceElevated
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WodDetailScreen(
    viewModel: WodDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToTimer: (String) -> Unit,
    onNavigateToLog: (String) -> Unit,
    onNavigateToCamera: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BackgroundDeepBlack,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        uiState.workout?.name ?: "",
                        style = ApexTypography.headlineMedium,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {},
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundDeepBlack
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SecondaryButton(
                        text = "Log Result",
                        onClick = { uiState.workout?.id?.let(onNavigateToLog) },
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryButton(
                        text = "Start Timer",
                        onClick = { uiState.workout?.id?.let(onNavigateToTimer) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShimmerBox(modifier = Modifier.fillMaxWidth().height(40.dp))
                ShimmerBox(modifier = Modifier.fillMaxWidth().height(120.dp))
                ShimmerBox(modifier = Modifier.fillMaxWidth().height(200.dp))
            }
        } else if (uiState.error != null) {
            Column(
                modifier = Modifier.padding(innerPadding).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(uiState.error!!, style = ApexTypography.bodyMedium, color = ColorError)
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
                // WOD meta chips row
                item {
                    uiState.workout?.let { wod ->
                        WodMetaRow(wod)
                    }
                    HorizontalDivider(color = BorderSubtle, thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 12.dp))
                }

                // Movements section
                item {
                    Text(
                        "MOVEMENTS",
                        style = ApexTypography.labelSmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                items(uiState.movements) { wm ->
                    MovementRow(wm = wm, onRecordClick = onNavigateToCamera)
                    HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun WodMetaRow(wod: com.apexai.crossfit.core.domain.model.Workout) {
    val domainColor = when (wod.timeDomain) {
        TimeDomain.AMRAP      -> ElectricBlue
        TimeDomain.EMOM       -> NeonGreen
        TimeDomain.RFT        -> BlazeOrange
        TimeDomain.TABATA     -> ColorWarning
        TimeDomain.FOR_TIME   -> BlazeOrange  // Sprint/chipper — same energy as RFT
        TimeDomain.MAX_WEIGHT -> NeonGreen    // Strength domain
        TimeDomain.CALORIES   -> ElectricBlue // Cardio/machine domain
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        MetaChip(icon = Icons.Outlined.Timer, label = wod.timeDomain.name, color = domainColor)
        wod.timeCap?.let {
            MetaChip(
                icon  = Icons.Outlined.Timer,
                label = "${it.toMinutes()} min",
                color = TextSecondary
            )
        }
    }
    if (wod.description.isNotBlank()) {
        Spacer(Modifier.height(8.dp))
        Text(wod.description, style = ApexTypography.bodyMedium, color = TextSecondary)
    }
}

@Composable
private fun MetaChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .then(
                Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Text(label, style = ApexTypography.labelLarge, color = color)
    }
}

@Composable
private fun MovementRow(
    wm: WorkoutMovement,
    onRecordClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "${wm.prescribedReps ?: "-"}×",
            style = ApexTypography.headlineSmall,
            color = ElectricBlue,
            modifier = Modifier.width(48.dp),
            textAlign = TextAlign.End
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(wm.movement.name, style = ApexTypography.titleMedium, color = TextPrimary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                wm.prescribedWeight?.let {
                    Text("${it} kg (Rx)", style = ApexTypography.bodySmall, color = TextSecondary)
                }
                wm.movement.equipment?.let {
                    Text(it, style = ApexTypography.bodySmall, color = TextSecondary)
                }
            }
        }
        IconButton(
            onClick = onRecordClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                Icons.Outlined.Videocam,
                contentDescription = "Record ${wm.movement.name}",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
