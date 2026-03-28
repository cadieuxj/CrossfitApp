package com.apexai.crossfit.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apexai.crossfit.core.ui.theme.ApexTypography
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.ColorError
import com.apexai.crossfit.core.ui.theme.CornerMedium
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.SurfaceElevated
import com.apexai.crossfit.core.ui.theme.TextDisabled
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary

@Composable
fun ApexTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    errorMessage: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = label?.let { { Text(it, style = ApexTypography.bodySmall) } },
            placeholder = placeholder?.let { { Text(it) } },
            trailingIcon = trailingIcon,
            leadingIcon = leadingIcon,
            isError = errorMessage != null,
            enabled = enabled,
            readOnly = readOnly,
            minLines = minLines,
            maxLines = maxLines,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            shape = CornerMedium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor    = SurfaceElevated,
                unfocusedContainerColor  = SurfaceElevated,
                disabledContainerColor   = com.apexai.crossfit.core.ui.theme.SurfaceDark,
                focusedBorderColor       = ElectricBlue,
                unfocusedBorderColor     = BorderSubtle,
                errorBorderColor         = ColorError,
                focusedTextColor         = TextPrimary,
                unfocusedTextColor       = TextPrimary,
                disabledTextColor        = TextDisabled,
                focusedLabelColor        = ElectricBlue,
                unfocusedLabelColor      = TextSecondary,
                errorLabelColor          = ColorError,
                placeholderColor         = TextSecondary,
                focusedLeadingIconColor  = TextSecondary,
                focusedTrailingIconColor = TextSecondary,
            ),
            textStyle = ApexTypography.bodyLarge.copy(color = TextPrimary)
        )
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                style = ApexTypography.bodySmall,
                color = ColorError
            )
        }
    }
}
