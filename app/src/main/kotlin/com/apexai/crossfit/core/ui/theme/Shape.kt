package com.apexai.crossfit.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Corner radius tokens
val CornerSmall  = RoundedCornerShape(8.dp)
val CornerMedium = RoundedCornerShape(12.dp)
val CornerLarge  = RoundedCornerShape(16.dp)
val CornerXLarge = RoundedCornerShape(24.dp)
val CornerFull   = RoundedCornerShape(9999.dp)

val ApexShapes = Shapes(
    extraSmall = CornerSmall,
    small      = CornerSmall,
    medium     = CornerMedium,
    large      = CornerLarge,
    extraLarge = CornerXLarge
)
