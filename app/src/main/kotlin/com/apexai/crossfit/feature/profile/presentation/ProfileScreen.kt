package com.apexai.crossfit.feature.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.apexai.crossfit.R
import com.apexai.crossfit.core.ui.components.DestructiveButton
import com.apexai.crossfit.core.ui.theme.ApexTypography
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.SurfaceDark
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLoggedOut: () -> Unit,
    onNavigateToCoachLink: () -> Unit = {},
    onNavigateToCoachDashboard: () -> Unit = {},
    onNavigateToNutrition: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ProfileEffect.NavigateToLogin -> onLoggedOut()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.profile_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundDeepBlack
                )
            )
        },
        containerColor = BackgroundDeepBlack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(SurfaceDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = ElectricBlue,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            if (uiState.profile != null) {
                Text(
                    text = uiState.profile!!.displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = uiState.profile!!.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = BorderSubtle)

            Spacer(Modifier.height(8.dp))
            Text(
                "TOOLS",
                style = ApexTypography.labelSmall,
                color = TextSecondary,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp)
            )

            ProfileMenuRow(
                icon  = Icons.Outlined.LocalFireDepartment,
                label = "Nutrition & Macros",
                onClick = onNavigateToNutrition
            )
            HorizontalDivider(color = BorderSubtle)

            ProfileMenuRow(
                icon  = Icons.Outlined.Link,
                label = "Coach Link",
                onClick = onNavigateToCoachLink
            )
            HorizontalDivider(color = BorderSubtle)

            ProfileMenuRow(
                icon  = Icons.Outlined.Group,
                label = "My Athletes",
                onClick = onNavigateToCoachDashboard
            )
            HorizontalDivider(color = BorderSubtle)

            Spacer(modifier = Modifier.height(32.dp))
            DestructiveButton(
                text = stringResource(R.string.profile_logout),
                onClick = { viewModel.logout() },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(20.dp))
        Text(label, style = ApexTypography.titleMedium, color = TextPrimary, modifier = Modifier.weight(1f))
        Icon(Icons.Outlined.ChevronRight, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
    }
}
