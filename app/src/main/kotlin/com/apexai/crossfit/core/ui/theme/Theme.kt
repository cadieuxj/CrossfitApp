package com.apexai.crossfit.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ApexDarkColorScheme = darkColorScheme(
    primary              = ElectricBlue,
    onPrimary            = TextOnBlue,
    primaryContainer     = ElectricBlueDark,
    onPrimaryContainer   = TextPrimary,
    secondary            = NeonGreen,
    onSecondary          = TextOnBlue,
    secondaryContainer   = SurfaceElevated,
    onSecondaryContainer = TextPrimary,
    tertiary             = BlazeOrange,
    background           = BackgroundDeepBlack,
    onBackground         = TextPrimary,
    surface              = SurfaceCard,
    onSurface            = TextPrimary,
    surfaceVariant       = SurfaceElevated,
    onSurfaceVariant     = TextSecondary,
    outline              = BorderSubtle,
    outlineVariant       = BorderVisible,
    error                = ColorError,
    onError              = TextPrimary,
    scrim                = SurfaceDark
)

@Composable
fun ApexTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ApexDarkColorScheme,
        typography  = ApexTypography,
        shapes      = ApexShapes,
        content     = content
    )
}
