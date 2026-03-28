package com.apexai.crossfit.feature.pr.presentation.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apexai.crossfit.core.domain.model.PrHistoryEntry
import com.apexai.crossfit.core.ui.components.ApexCard
import com.apexai.crossfit.core.ui.theme.ApexTypography
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.NeonGreen
import com.apexai.crossfit.core.ui.theme.SurfaceCard
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrDetailScreen(
    viewModel: PrDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BackgroundDeepBlack,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        uiState.movement?.name ?: "",
                        style = ApexTypography.headlineMedium,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundDeepBlack)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero
            item {
                uiState.currentPr?.let { pr ->
                    ApexCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("CURRENT PR", style = ApexTypography.labelSmall, color = TextSecondary)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "${pr.value.toLong()}",
                                    style = ApexTypography.displayMedium,
                                    color = ElectricBlue
                                )
                                Text(pr.unit.name.lowercase(), style = ApexTypography.headlineSmall, color = TextSecondary)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Set on ${DateTimeFormatter.ofPattern("MMM d, yyyy").withZone(ZoneId.systemDefault()).format(pr.achievedAt)}",
                                    style = ApexTypography.bodySmall, color = TextSecondary
                                )
                            }
                            Icon(Icons.Outlined.EmojiEvents, null, tint = ElectricBlue, modifier = Modifier.size(48.dp))
                        }
                    }
                }
            }

            // Progress chart
            item {
                Text("PROGRESS", style = ApexTypography.labelSmall, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                TabRow(
                    selectedTabIndex = uiState.selectedTimeRangeIndex,
                    containerColor   = BackgroundDeepBlack,
                    contentColor     = ElectricBlue,
                    indicator        = { tabPositions ->
                        if (uiState.selectedTimeRangeIndex < tabPositions.size) {
                            androidx.compose.material3.TabRowDefaults.SecondaryIndicator(
                                Modifier.androidx.compose.material3.tabIndicatorOffset(
                                    tabPositions[uiState.selectedTimeRangeIndex]
                                ),
                                color = ElectricBlue
                            )
                        }
                    }
                ) {
                    listOf("3M", "6M", "1Y", "All").forEachIndexed { index, label ->
                        Tab(
                            selected = uiState.selectedTimeRangeIndex == index,
                            onClick  = { viewModel.selectTimeRange(index) },
                            text     = { Text(label, color = if (uiState.selectedTimeRangeIndex == index) ElectricBlue else TextSecondary) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                PrLineChart(
                    history  = uiState.filteredHistory,
                    modifier = Modifier.fillMaxWidth().height(180.dp)
                )
            }

            // History list
            item {
                Text("HISTORY", style = ApexTypography.labelSmall, color = TextSecondary,
                    modifier = Modifier.padding(bottom = 12.dp))
            }
            items(uiState.filteredHistory.reversed()) { entry ->
                val idx = uiState.filteredHistory.indexOf(entry)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.size(width = 48.dp, height = 48.dp)) {
                            val dt = entry.achievedAt.atZone(ZoneId.systemDefault())
                            Text(
                                dt.format(DateTimeFormatter.ofPattern("MMM")),
                                style = ApexTypography.labelSmall, color = TextSecondary
                            )
                            Text(
                                dt.format(DateTimeFormatter.ofPattern("d")),
                                style = ApexTypography.titleMedium, color = TextPrimary
                            )
                        }
                        Column {
                            Text(
                                "${entry.value.toLong()} ${entry.unit.name.lowercase()}",
                                style = ApexTypography.titleLarge, color = TextPrimary
                            )
                            if (idx > 0) {
                                val prev = uiState.filteredHistory[idx - 1]
                                val delta = entry.value - prev.value
                                if (delta > 0) {
                                    Text(
                                        "+${delta.toLong()} ${entry.unit.name.lowercase()} improvement",
                                        style = ApexTypography.bodySmall, color = NeonGreen
                                    )
                                }
                            }
                        }
                    }
                    if (idx == uiState.filteredHistory.size - 1) {
                        Icon(Icons.Outlined.EmojiEvents, null, tint = NeonGreen, modifier = Modifier.size(20.dp))
                    }
                }
                HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
            }
        }
    }
}

@Composable
private fun PrLineChart(history: List<PrHistoryEntry>, modifier: Modifier) {
    if (history.size < 2) return
    val maxVal = history.maxOf { it.value }
    val minVal = history.minOf { it.value }
    val range  = (maxVal - minVal).coerceAtLeast(1.0)

    Canvas(modifier = modifier.background(SurfaceCard, com.apexai.crossfit.core.ui.theme.CornerLarge)
        .padding(16.dp)) {
        val pw = size.width
        val ph = size.height
        val stepX = pw / (history.size - 1).coerceAtLeast(1)

        fun xFor(i: Int)    = i * stepX
        fun yFor(v: Double) = ph - ((v - minVal) / range * ph).toFloat()

        // Grid lines
        (0..4).forEach { i ->
            val y = ph * i / 4f
            drawLine(BorderSubtle, Offset(0f, y), Offset(pw, y), 1.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx())))
        }

        // Area fill
        val areaPath = Path().apply {
            moveTo(xFor(0), ph)
            history.forEachIndexed { i, e -> lineTo(xFor(i), yFor(e.value)) }
            lineTo(xFor(history.size - 1), ph)
            close()
        }
        drawPath(areaPath, brush = androidx.compose.ui.graphics.Brush.verticalGradient(
            listOf(ElectricBlue.copy(alpha = 0.3f), Color.Transparent)
        ))

        // Line
        val linePath = Path()
        history.forEachIndexed { i, e ->
            if (i == 0) linePath.moveTo(xFor(0), yFor(e.value))
            else        linePath.lineTo(xFor(i), yFor(e.value))
        }
        drawPath(linePath, ElectricBlue, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))

        // Data points
        history.forEachIndexed { i, e ->
            drawCircle(ElectricBlue, 6.dp.toPx(), Offset(xFor(i), yFor(e.value)))
            drawCircle(BackgroundDeepBlack, 3.dp.toPx(), Offset(xFor(i), yFor(e.value)))
        }
    }
}
