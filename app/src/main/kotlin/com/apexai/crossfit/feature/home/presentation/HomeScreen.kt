package com.apexai.crossfit.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.apexai.crossfit.R
import com.apexai.crossfit.core.domain.model.DailyMacroSummary
import com.apexai.crossfit.core.domain.model.MacroTargets
import com.apexai.crossfit.core.domain.model.PersonalRecord
import com.apexai.crossfit.core.domain.model.ReadinessScore
import com.apexai.crossfit.core.domain.model.ReadinessZone
import com.apexai.crossfit.core.domain.model.WorkoutSummary
import com.apexai.crossfit.core.ui.components.ApexCard
import com.apexai.crossfit.core.ui.components.ShimmerBox
import com.apexai.crossfit.core.ui.theme.ApexTypography
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.BlazeOrange
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.CornerMedium
import com.apexai.crossfit.core.ui.theme.CornerSmall
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.NeonGreen
import com.apexai.crossfit.core.ui.theme.ReadinessOptimal
import com.apexai.crossfit.core.ui.theme.ReadinessReduce
import com.apexai.crossfit.core.ui.theme.ReadinessRest
import com.apexai.crossfit.core.ui.theme.SurfaceDark
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onWodClick: (String) -> Unit,
    onReadinessClick: () -> Unit,
    onPrClick: () -> Unit,
    onCameraClick: () -> Unit,
    onNutritionClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.home_title),
                        style = ApexTypography.headlineMedium,
                        color = TextPrimary
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.loadDashboard() }) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = stringResource(R.string.action_refresh),
                            tint = ElectricBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundDeepBlack
                )
            )
        },
        containerColor = BackgroundDeepBlack
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ReadinessSummaryCard(
                    readiness = uiState.readiness,
                    isLoading = uiState.isLoading,
                    onClick   = onReadinessClick
                )
            }
            item {
                TodayWodCard(
                    wod       = uiState.todayWod,
                    isLoading = uiState.isLoading,
                    onClick   = { uiState.todayWod?.let { onWodClick(it.id) } }
                )
            }
            if (uiState.recentPrs.isNotEmpty() || uiState.isLoading) {
                item {
                    Text(
                        text  = stringResource(R.string.home_recent_prs),
                        style = ApexTypography.labelSmall,
                        color = TextSecondary
                    )
                }
                item {
                    RecentPrsRow(
                        prs       = uiState.recentPrs,
                        isLoading = uiState.isLoading,
                        onPrClick = { onPrClick() }
                    )
                }
            }
            // Nutrition summary card — only shown when there's data or targets set
            if (uiState.macroSummary != null || uiState.macroTargets != null) {
                item {
                    NutritionSummaryCard(
                        summary  = uiState.macroSummary,
                        targets  = uiState.macroTargets,
                        isLoading = uiState.isLoading,
                        onClick  = onNutritionClick
                    )
                }
            }
            item {
                QuickActionsRow(
                    onCameraClick = onCameraClick,
                    onWodClick    = { onWodClick("") },
                    onPrClick     = onPrClick
                )
            }
        }
    }
}

@Composable
private fun NutritionSummaryCard(
    summary: DailyMacroSummary?,
    targets: MacroTargets?,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    if (isLoading) {
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(88.dp))
        return
    }
    val calTarget = targets?.caloriesKcal ?: 2500
    val calActual = summary?.totalCalories ?: 0
    val proteinActual = summary?.totalProteinG?.roundToInt() ?: 0
    val carbsActual   = summary?.totalCarbsG?.roundToInt() ?: 0
    val fatActual     = summary?.totalFatG?.roundToInt() ?: 0
    val calProgress   = (calActual.toFloat() / calTarget.toFloat()).coerceIn(0f, 1f)

    ApexCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("TODAY'S NUTRITION", style = ApexTypography.labelSmall, color = TextSecondary)
                Text("$calActual / $calTarget kcal", style = ApexTypography.bodySmall, color = BlazeOrange)
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Calorie progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(SurfaceDark, CornerSmall)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(calProgress)
                        .height(4.dp)
                        .background(BlazeOrange, CornerSmall)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${proteinActual}g", style = ApexTypography.titleMedium, color = ElectricBlue)
                    Text("Protein", style = ApexTypography.labelSmall, color = TextSecondary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${carbsActual}g", style = ApexTypography.titleMedium, color = NeonGreen)
                    Text("Carbs", style = ApexTypography.labelSmall, color = TextSecondary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${fatActual}g", style = ApexTypography.titleMedium, color = BlazeOrange)
                    Text("Fat", style = ApexTypography.labelSmall, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun ReadinessSummaryCard(
    readiness: ReadinessScore?,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    if (isLoading) {
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(100.dp))
        return
    }
    ApexCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Map ACWR zone to a representative 0-100 display score
            val score = when (readiness?.zone) {
                ReadinessZone.OPTIMAL      -> 85
                ReadinessZone.UNDERTRAINED -> 55
                ReadinessZone.CAUTION      -> 35
                ReadinessZone.HIGH_RISK    -> 15
                ReadinessZone.ONBOARDING, null -> 0
            }
            val zoneColor = when {
                score >= 80 -> ReadinessOptimal
                score >= 60 -> NeonGreen
                score >= 40 -> ReadinessReduce
                else        -> ReadinessRest
            }
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(SurfaceDark, CornerSmall),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text      = "$score",
                    style     = ApexTypography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color     = zoneColor
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text  = stringResource(R.string.home_readiness_title),
                    style = ApexTypography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text     = readiness?.recommendation ?: stringResource(R.string.home_readiness_no_data),
                    style    = ApexTypography.bodySmall,
                    color    = TextSecondary,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun TodayWodCard(
    wod: WorkoutSummary?,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    if (isLoading) {
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(120.dp))
        return
    }
    ApexCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text  = stringResource(R.string.home_today_wod),
                    style = ApexTypography.labelSmall,
                    color = TextSecondary
                )
                Icon(
                    imageVector = Icons.Outlined.WbSunny,
                    contentDescription = null,
                    tint     = ElectricBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (wod != null) {
                Text(
                    text       = wod.name,
                    style      = ApexTypography.headlineSmall,
                    color      = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text  = wod.timeDomain.name.replace("_", " "),
                    style = ApexTypography.labelSmall,
                    color = ElectricBlue
                )
            } else {
                Text(
                    text  = stringResource(R.string.home_no_wod),
                    style = ApexTypography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun RecentPrsRow(
    prs: List<PersonalRecord>,
    isLoading: Boolean,
    onPrClick: () -> Unit
) {
    if (isLoading) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(3) {
                ShimmerBox(modifier = Modifier.size(width = 120.dp, height = 80.dp))
            }
        }
        return
    }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(prs) { pr ->
            ApexCard(
                modifier = Modifier
                    .size(width = 130.dp, height = 88.dp)
                    .clickable(onClick = onPrClick)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Outlined.EmojiEvents,
                        contentDescription = null,
                        tint     = NeonGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text     = pr.movementName,
                        style    = ApexTypography.labelSmall,
                        color    = TextSecondary,
                        maxLines = 1
                    )
                    Text(
                        text       = "${pr.value} ${pr.unit.name.lowercase()}",
                        style      = ApexTypography.titleMedium,
                        color      = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    onCameraClick: () -> Unit,
    onWodClick: () -> Unit,
    onPrClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionCard(
            label    = stringResource(R.string.home_quick_camera),
            icon     = Icons.Outlined.Videocam,
            onClick  = onCameraClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionCard(
            label    = stringResource(R.string.home_quick_wod),
            icon     = Icons.Outlined.WbSunny,
            onClick  = onWodClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionCard(
            label    = stringResource(R.string.home_quick_prs),
            icon     = Icons.Outlined.EmojiEvents,
            onClick  = onPrClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionCard(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ApexCard(
        modifier = modifier
            .height(80.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint     = ElectricBlue,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text  = label,
                style = ApexTypography.labelSmall,
                color = TextSecondary
            )
        }
    }
}
