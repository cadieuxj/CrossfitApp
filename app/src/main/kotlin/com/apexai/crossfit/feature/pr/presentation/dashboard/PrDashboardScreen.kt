package com.apexai.crossfit.feature.pr.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.SlidersHorizontal
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apexai.crossfit.core.domain.model.PersonalRecord
import com.apexai.crossfit.core.ui.components.ApexBottomNavBar
import com.apexai.crossfit.core.ui.components.PrimaryButton
import com.apexai.crossfit.core.ui.theme.ApexTypography
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.NeonGreen
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrDashboardScreen(
    viewModel: PrDashboardViewModel,
    currentNavRoute: String,
    onNavigateToPrDetail: (String) -> Unit,
    onNavigateToWod: () -> Unit,
    onBottomNavNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        containerColor = BackgroundDeepBlack,
        topBar = {
            LargeTopAppBar(
                title = { Text("Personal Records", color = TextPrimary) },
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.SlidersHorizontal, "Filter", tint = TextPrimary)
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
        if (uiState.prsByCategory.isEmpty() && !uiState.isLoading) {
            // Empty state
            Column(
                modifier = Modifier.padding(innerPadding).padding(32.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Outlined.EmojiEvents, null, tint = TextSecondary, modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(16.dp))
                Text("No Personal Records Yet", style = ApexTypography.headlineSmall, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Complete workouts to automatically track your PRs",
                    style = ApexTypography.bodyMedium, color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                PrimaryButton("Browse Workouts", onClick = onNavigateToWod,
                    modifier = Modifier.fillMaxWidth())
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                uiState.prsByCategory.forEach { (category, prs) ->
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(category, style = ApexTypography.titleLarge, color = TextPrimary)
                            Text("${prs.size} movements", style = ApexTypography.bodySmall, color = TextSecondary)
                        }
                    }
                    items(prs) { pr ->
                        PrListItem(pr = pr, onClick = { onNavigateToPrDetail(pr.movementId) })
                        HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
private fun PrListItem(pr: PersonalRecord, onClick: () -> Unit) {
    val isNew = pr.achievedAt.isAfter(Instant.now().minusSeconds(7 * 24 * 3600))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                Modifier.clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
            )
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(pr.movementName, style = ApexTypography.titleMedium, color = TextPrimary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isNew) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .background(NeonGreen.copy(alpha = 0.15f), com.apexai.crossfit.core.ui.theme.CornerFull)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("NEW", style = ApexTypography.labelSmall, color = NeonGreen)
                    }
                }
                Text(
                    "Set ${formatRelativeDate(pr.achievedAt)}",
                    style = ApexTypography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "${pr.value.toLong()} ${pr.unit.name.lowercase()}",
                style = ApexTypography.titleLarge,
                color = ElectricBlue
            )
        }
    }
}

private fun formatRelativeDate(instant: Instant): String {
    val days = java.time.temporal.ChronoUnit.DAYS.between(instant, Instant.now())
    return when {
        days == 0L -> "today"
        days == 1L -> "yesterday"
        days < 7   -> "${days}d ago"
        days < 30  -> "${days / 7}w ago"
        else       -> DateTimeFormatter.ofPattern("MMM d")
            .withZone(ZoneId.systemDefault()).format(instant)
    }
}

private fun Modifier.clickable(
    interactionSource: androidx.compose.foundation.interaction.MutableInteractionSource,
    indication: androidx.compose.foundation.Indication?,
    onClick: () -> Unit
): Modifier = this.then(
    Modifier.clickable(
        interactionSource = interactionSource,
        indication = indication,
        onClick = onClick
    )
)

private val remember = @Composable { androidx.compose.runtime.remember { } }
