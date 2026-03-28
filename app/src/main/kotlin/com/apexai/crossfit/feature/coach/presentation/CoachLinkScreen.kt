package com.apexai.crossfit.feature.coach.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apexai.crossfit.core.domain.model.CoachConnection
import com.apexai.crossfit.core.ui.components.ApexCard
import com.apexai.crossfit.core.ui.components.ApexTextField
import com.apexai.crossfit.core.ui.components.PrimaryButton
import com.apexai.crossfit.core.ui.components.ShimmerBox
import com.apexai.crossfit.core.ui.theme.ApexTypography
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.ColorError
import com.apexai.crossfit.core.ui.theme.CornerFull
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.SurfaceDark
import com.apexai.crossfit.core.ui.theme.SurfaceElevated
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary
import java.time.format.DateTimeFormatter
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachLinkScreen(
    viewModel: CoachLinkViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var unlinkTarget by remember { mutableStateOf<CoachConnection?>(null) }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { /* success feedback could go here */ }
    }

    // Unlink confirmation dialog
    unlinkTarget?.let { connection ->
        AlertDialog(
            onDismissRequest = { unlinkTarget = null },
            title = { Text("Unlink Coach", color = TextPrimary) },
            text = {
                Text(
                    "Remove ${connection.coachDisplayName} as your coach? They will no longer be able to view your results.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(CoachLinkEvent.UnlinkCoach(connection.id))
                        unlinkTarget = null
                    }
                ) {
                    Text("Remove", color = ColorError)
                }
            },
            dismissButton = {
                TextButton(onClick = { unlinkTarget = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceElevated
        )
    }

    Scaffold(
        containerColor = BackgroundDeepBlack,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Coach Link", style = ApexTypography.headlineMedium, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundDeepBlack)
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.padding(innerPadding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShimmerBox(Modifier.fillMaxWidth().height(140.dp))
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 16.dp, vertical = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Link section
                item {
                    ApexCard {
                        Column(Modifier.fillMaxWidth()) {
                            Text("LINK A COACH", style = ApexTypography.labelSmall, color = TextSecondary)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Ask your coach for their 6-character invite code, then enter it below. Your coach will be able to view your workout results and personal records.",
                                style = ApexTypography.bodySmall,
                                color = TextSecondary
                            )
                            Spacer(Modifier.height(16.dp))

                            ApexTextField(
                                value = uiState.inviteCode,
                                onValueChange = { viewModel.onEvent(CoachLinkEvent.InviteCodeChanged(it)) },
                                label = "6-character code",
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (uiState.error != null) {
                                Spacer(Modifier.height(6.dp))
                                Text(uiState.error!!, style = ApexTypography.bodySmall, color = ColorError)
                            }

                            Spacer(Modifier.height(12.dp))
                            PrimaryButton(
                                text = "Link Coach",
                                onClick = { viewModel.onEvent(CoachLinkEvent.LinkCoach) },
                                enabled = uiState.inviteCode.length == 6 && !uiState.isLinking,
                                isLoading = uiState.isLinking,
                                modifier = Modifier.fillMaxWidth().height(52.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }

                // Linked coaches section
                if (uiState.connections.isNotEmpty()) {
                    item {
                        Text("MY COACHES", style = ApexTypography.labelSmall, color = TextSecondary,
                            modifier = Modifier.padding(bottom = 12.dp))
                    }
                    items(uiState.connections) { connection ->
                        CoachConnectionRow(
                            connection = connection,
                            onUnlink = { unlinkTarget = connection }
                        )
                        HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Outlined.Person, null,
                                    tint = TextSecondary, modifier = Modifier.size(40.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("No coaches linked yet", style = ApexTypography.bodyMedium, color = TextSecondary)
                                Text("Enter your coach's code above to connect", style = ApexTypography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CoachConnectionRow(connection: CoachConnection, onUnlink: () -> Unit) {
    val dateFmt = DateTimeFormatter.ofPattern("MMM d, yyyy")
    val linkedDate = connection.connectedAt.atOffset(ZoneOffset.UTC).toLocalDate().format(dateFmt)

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CornerFull)
                .background(ElectricBlue.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                connection.coachDisplayName.first().uppercaseChar().toString(),
                style = ApexTypography.titleMedium,
                color = ElectricBlue
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(connection.coachDisplayName, style = ApexTypography.titleMedium, color = TextPrimary)
            connection.gymName?.let {
                Text(it, style = ApexTypography.bodySmall, color = TextSecondary)
            }
            Text("Linked $linkedDate", style = ApexTypography.bodySmall, color = TextSecondary)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                connection.permissions.forEach { permission ->
                    PermissionTag(permission)
                }
            }
        }

        IconButton(onClick = onUnlink, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Outlined.LinkOff, "Unlink", tint = ColorError, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun PermissionTag(permission: String) {
    val label = when (permission) {
        "view_results"      -> "Results"
        "view_prs"          -> "PRs"
        "add_coach_notes"   -> "Notes"
        else                -> permission
    }
    Box(
        modifier = Modifier
            .clip(CornerFull)
            .background(SurfaceDark)
            .border(1.dp, BorderSubtle, CornerFull)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(label, style = ApexTypography.labelSmall, color = TextSecondary)
    }
}
