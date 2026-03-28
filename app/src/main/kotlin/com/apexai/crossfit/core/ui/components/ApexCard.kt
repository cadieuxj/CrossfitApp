package com.apexai.crossfit.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.CornerLarge
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.SurfaceCard

@Composable
fun ApexCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    isSelected: Boolean = false,
    isError: Boolean = false,
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp,
    padding: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.99f else 1f,
        animationSpec = tween(150),
        label = "card_scale"
    )

    val effectiveBorderColor = when {
        isError    -> com.apexai.crossfit.core.ui.theme.ColorError
        isSelected -> ElectricBlue
        borderColor != null -> borderColor
        isPressed && onClick != null -> ElectricBlue
        else -> BorderSubtle
    }

    val effectiveBorderWidth = when {
        isSelected -> 2.dp
        else -> borderWidth
    }

    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        shape = CornerLarge,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SurfaceCard.copy(alpha = 0.94f) else SurfaceCard
        ),
        border = BorderStroke(effectiveBorderWidth, effectiveBorderColor)
    ) {
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}
