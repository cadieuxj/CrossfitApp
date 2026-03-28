package com.apexai.crossfit.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.apexai.crossfit.core.ui.theme.ApexTypography
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.ColorError
import com.apexai.crossfit.core.ui.theme.CornerMedium
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.ElectricBlueDark
import com.apexai.crossfit.core.ui.theme.TextDisabled
import com.apexai.crossfit.core.ui.theme.TextOnBlue
import com.apexai.crossfit.core.ui.theme.TextPrimary

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(150),
        label = "primary_button_scale"
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .height(52.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale },
        enabled = enabled && !isLoading,
        shape = CornerMedium,
        contentPadding = PaddingValues(horizontal = 24.dp),
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor         = ElectricBlue,
            contentColor           = TextOnBlue,
            disabledContainerColor = BorderSubtle,
            disabledContentColor   = TextDisabled
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = TextOnBlue,
                strokeWidth = 2.dp
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                if (leadingIcon != null) {
                    leadingIcon()
                }
            }
            Text(
                text = text,
                style = ApexTypography.titleMedium.copy(color = TextOnBlue)
            )
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        enabled = enabled,
        shape = CornerMedium,
        contentPadding = PaddingValues(horizontal = 24.dp),
        border = BorderStroke(
            1.dp,
            if (enabled) ElectricBlue else BorderSubtle
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor           = ElectricBlue,
            disabledContentColor   = TextDisabled
        )
    ) {
        leadingIcon?.invoke()
        Text(
            text = text,
            style = ApexTypography.titleMedium
        )
    }
}

@Composable
fun ApexTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    androidx.compose.material3.TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor         = ElectricBlue,
            disabledContentColor = TextDisabled
        )
    ) {
        Text(
            text = text,
            style = ApexTypography.titleMedium
        )
    }
}

@Composable
fun DestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        enabled = enabled,
        shape = CornerMedium,
        colors = ButtonDefaults.buttonColors(
            containerColor       = ColorError,
            contentColor         = TextPrimary,
            disabledContainerColor = BorderSubtle,
            disabledContentColor   = TextDisabled
        )
    ) {
        Text(
            text = text,
            style = ApexTypography.titleMedium.copy(color = TextPrimary)
        )
    }
}
