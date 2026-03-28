package com.apexai.crossfit.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.apexai.crossfit.core.ui.navigation.NavRoutes
import com.apexai.crossfit.core.ui.theme.ApexTypography
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.CornerFull
import com.apexai.crossfit.core.ui.theme.CornerXLarge
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.ElectricBlueDark
import com.apexai.crossfit.core.ui.theme.SurfaceDark
import com.apexai.crossfit.core.ui.theme.TextOnBlue
import com.apexai.crossfit.core.ui.theme.TextSecondary

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    data object Home      : BottomNavItem(NavRoutes.HOME, Icons.Outlined.Home, "Home")
    data object Wod       : BottomNavItem(NavRoutes.WOD_BROWSE, Icons.Outlined.FitnessCenter, "WOD")
    data object Pr        : BottomNavItem(NavRoutes.PR_DASHBOARD, Icons.Outlined.StarBorder, "PRs")
    data object Readiness : BottomNavItem(NavRoutes.READINESS, Icons.Outlined.MonitorHeart, "Readiness")
    data object Profile   : BottomNavItem(NavRoutes.PROFILE, Icons.Outlined.Person, "Profile")
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Wod,
    BottomNavItem.Pr,
    BottomNavItem.Readiness,
    BottomNavItem.Profile
)

@Composable
fun ApexBottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onCameraFabClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        // Nav bar background
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .border(
                    width = 1.dp,
                    color = BorderSubtle,
                    shape = androidx.compose.foundation.shape.RectangleShape
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left 2 items
                bottomNavItems.take(2).forEach { item ->
                    NavBarItem(
                        item = item,
                        isSelected = currentRoute == item.route,
                        onClick = { onNavigate(item.route) }
                    )
                }

                // Centre FAB placeholder
                Box(modifier = Modifier.size(72.dp))

                // Right 2 items (skip the center)
                bottomNavItems.takeLast(2).forEach { item ->
                    NavBarItem(
                        item = item,
                        isSelected = currentRoute == item.route,
                        onClick = { onNavigate(item.route) }
                    )
                }
            }
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }

        // Centre Camera FAB — elevated above nav bar top edge
        CameraFab(
            onClick = onCameraFabClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-20).dp)
        )
    }
}

@Composable
private fun NavBarItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(CornerXLarge)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Active indicator pill
        Box(
            modifier = Modifier
                .size(width = 16.dp, height = if (isSelected) 2.dp else 0.dp)
                .background(ElectricBlue, CornerFull)
        )

        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = if (isSelected) ElectricBlue else TextSecondary.copy(alpha = 0.4f),
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = item.label,
            style = ApexTypography.labelSmall,
            color = if (isSelected) ElectricBlue else TextSecondary.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun CameraFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(150),
        label = "fab_scale"
    )

    Box(
        modifier = modifier
            .size(56.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(
                elevation = 8.dp,
                shape = CornerFull,
                ambientColor = ElectricBlue.copy(alpha = 0.4f),
                spotColor = ElectricBlue.copy(alpha = 0.4f)
            )
            .background(
                color = if (isPressed) ElectricBlueDark else ElectricBlue,
                shape = CornerFull
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.CameraAlt,
            contentDescription = "Open AI video coaching camera",
            tint = TextOnBlue,
            modifier = Modifier.size(28.dp)
        )
    }
}
