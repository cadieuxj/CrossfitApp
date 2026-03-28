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
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.apexai.crossfit.core.domain.model.PersonalRecord
import com.apexai.crossfit.core.domain.model.ReadinessScore
import com.apexai.crossfit.core.domain.model.WorkoutSummary
import com.apexai.crossfit.core.ui.components.ShimmerBox
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.NeonGreen
import com.apexai.crossfit.core.ui.theme.ReadinessOptimal
import com.apexai.crossfit.core.ui.theme.ReadinessReduce
import com.apexai.crossfit.core.ui.theme.ReadinessRest
import com.apexai.crossfit.core.ui.theme.SurfaceCard
import com.apexai.crossfit.core.ui.theme.SurfaceDark
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onWodClick: (String) -> Unit,
    onReadinessClick: () -> Unit,
    onPrClick: () -> Unit,
    onCameraClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.home_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.loadDashboard() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
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
                    onClick = onReadinessClick
                )
            }
            item {
                TodayWodCard(
                    wod = uiState.todayWod,
                    isLoading = uiState.isLoading,
                    onClick = { uiState.todayWod?.let { onWodClick(it.id) } }
                )
            }
            if (uiState.recentPrs.isNotEmpty() || uiState.isLoading) {
                item {
                    Text(
                        text = stringResource(R.string.home_recent_prs),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }
                item {
                    RecentPrsRow(
                        prs = uiState.recentPrs,
                        isLoading = uiState.isLoading,
                        onPrClick = { onPrClick() }
                    )
                }
            }
            item {
                QuickActionsRow(
                    onCameraClick = onCameraClick,
                    onWodClick = { onWodClick("") },
                    onPrClick = onPrClick
                )
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val score = readiness?.score ?: 0
            val zoneColor = when {
                score >= 80 -> ReadinessOptimal
                score >= 60 -> NeonGreen
                score >= 40 -> ReadinessReduce
                else -> ReadinessRest
            }
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(SurfaceDark, MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$score",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = zoneColor
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = stringResource(R.string.home_readiness_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = readiness?.recommendation ?: stringResource(R.string.home_readiness_no_data),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.home_today_wod),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary
                )
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = ElectricBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (wod != null) {
                Text(
                    text = wod.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = wod.timeDomain.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = ElectricBlue
                )
            } else {
                Text(
                    text = stringResource(R.string.home_no_wod),
                    style = MaterialTheme.typography.bodyMedium,
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
            Card(
                modifier = Modifier
                    .size(width = 120.dp, height = 80.dp)
                    .clickable(onClick = onPrClick),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = MaterialTheme.shapes.small
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = pr.movementName,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        maxLines = 1
                    )
                    Text(
                        text = "${pr.value} ${pr.unit.name.lowercase()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
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
            label = stringResource(R.string.home_quick_camera),
            icon = Icons.Default.FitnessCenter,
            onClick = onCameraClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionCard(
            label = stringResource(R.string.home_quick_wod),
            icon = Icons.Default.FitnessCenter,
            onClick = onWodClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionCard(
            label = stringResource(R.string.home_quick_prs),
            icon = Icons.Default.Star,
            onClick = onPrClick,
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
    Card(
        modifier = modifier
            .height(72.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = MaterialTheme.shapes.small,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}
