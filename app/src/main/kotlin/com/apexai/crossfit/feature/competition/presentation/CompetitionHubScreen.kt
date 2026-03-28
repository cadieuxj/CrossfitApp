package com.apexai.crossfit.feature.competition.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apexai.crossfit.core.domain.model.CompetitionEvent
import com.apexai.crossfit.core.domain.model.CompetitionStatus
import com.apexai.crossfit.core.domain.model.CompetitionType
import com.apexai.crossfit.core.ui.components.ApexBottomNavBar
import com.apexai.crossfit.core.ui.components.ShimmerBox
import com.apexai.crossfit.core.ui.navigation.NavRoutes
import com.apexai.crossfit.core.ui.theme.ApexTypography
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.BlazeOrange
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.ColorError
import com.apexai.crossfit.core.ui.theme.CornerMedium
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.NeonGreen
import com.apexai.crossfit.core.ui.theme.SurfaceElevated
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary
import java.time.format.DateTimeFormatter

private val DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitionHubScreen(
    viewModel: CompetitionHubViewModel,
    currentNavRoute: String,
    onNavigateToDetail: (String) -> Unit,
    onBottomNavNavigate: (String) -> Unit,
    onCameraFabClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BackgroundDeepBlack,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Season", style = ApexTypography.headlineMedium, color = TextPrimary)
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundDeepBlack
                )
            )
        },
        bottomBar = {
            ApexBottomNavBar(
                currentRoute = currentNavRoute,
                onNavigate = onBottomNavNavigate,
                onCameraFabClick = onCameraFabClick
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(3) { ShimmerBox(Modifier.fillMaxWidth().height(88.dp)) }
            }
        } else if (uiState.error != null) {
            Box(Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(uiState.error!!, style = ApexTypography.bodyMedium, color = ColorError)
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
                if (uiState.activeEvents.isNotEmpty()) {
                    item {
                        SectionHeader("ACTIVE NOW")
                    }
                    items(uiState.activeEvents) { event ->
                        EventCard(event = event, onClick = { onNavigateToDetail(event.id) })
                        HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }

                if (uiState.upcomingEvents.isNotEmpty()) {
                    item { SectionHeader("UPCOMING") }
                    items(uiState.upcomingEvents) { event ->
                        EventCard(event = event, onClick = { onNavigateToDetail(event.id) })
                        HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }

                if (uiState.completedEvents.isNotEmpty()) {
                    item { SectionHeader("COMPLETED") }
                    items(uiState.completedEvents) { event ->
                        EventCard(event = event, onClick = { onNavigateToDetail(event.id) })
                        HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                    }
                }

                if (uiState.events.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Outlined.CalendarMonth,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "No events scheduled",
                                    style = ApexTypography.titleMedium,
                                    color = TextSecondary
                                )
                                Text(
                                    "Season calendar will appear here",
                                    style = ApexTypography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = ApexTypography.labelSmall,
        color = TextSecondary,
        modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
    )
}

@Composable
private fun EventCard(event: CompetitionEvent, onClick: () -> Unit) {
    val statusColor = when (event.status) {
        CompetitionStatus.ACTIVE    -> NeonGreen
        CompetitionStatus.UPCOMING  -> ElectricBlue
        CompetitionStatus.COMPLETED -> TextSecondary
    }
    val typeColor = when (event.type) {
        CompetitionType.OPEN          -> BlazeOrange
        CompetitionType.QUARTERFINALS -> ElectricBlue
        CompetitionType.SEMIFINALS    -> ElectricBlue
        CompetitionType.GAMES         -> NeonGreen
        CompetitionType.LOCAL         -> BlazeOrange
        CompetitionType.VIRTUAL       -> TextSecondary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CornerMedium)
                .background(typeColor.copy(alpha = 0.12f))
                .border(1.dp, typeColor.copy(alpha = 0.4f), CornerMedium),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.EmojiEvents,
                contentDescription = null,
                tint = typeColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(event.name, style = ApexTypography.titleMedium, color = TextPrimary,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusPill(event.status.name, statusColor)
                Text(
                    "${event.startDate.format(DATE_FMT)} – ${event.endDate.format(DATE_FMT)}",
                    style = ApexTypography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Icon(
            Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun StatusPill(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(CornerMedium)
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(label, style = ApexTypography.labelSmall, color = color)
    }
}
