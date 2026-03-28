package com.apexai.crossfit.feature.auth.presentation.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.apexai.crossfit.R
import com.apexai.crossfit.core.ui.components.ApexTextField
import com.apexai.crossfit.core.ui.components.PrimaryButton
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary
import com.apexai.crossfit.feature.auth.presentation.login.AuthEffect
import com.apexai.crossfit.feature.auth.presentation.login.AuthEvent
import com.apexai.crossfit.feature.auth.presentation.login.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onBack: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AuthEffect.NavigateToHome -> onRegistered()
                else -> Unit
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.register_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = TextPrimary
                        )
                    }
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
                .padding(horizontal = 24.dp)
                .imePadding(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.register_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(32.dp))
            ApexTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEvent(AuthEvent.EmailChanged(it)) },
                label = stringResource(R.string.label_email),
                isError = uiState.emailError != null,
                errorMessage = uiState.emailError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            ApexTextField(
                value = uiState.password,
                onValueChange = { viewModel.onEvent(AuthEvent.PasswordChanged(it)) },
                label = stringResource(R.string.label_password),
                isError = uiState.passwordError != null,
                errorMessage = uiState.passwordError,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.onEvent(AuthEvent.RegisterClicked)
                    }
                ),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(
                            text = if (passwordVisible)
                                stringResource(R.string.action_hide)
                            else
                                stringResource(R.string.action_show),
                            color = TextSecondary,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            if (uiState.serverError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.serverError!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Privacy Policy consent — required by Quebec Law 25, Art. 8
            val uriHandler = LocalUriHandler.current
            val privacyText = buildAnnotatedString {
                append("I have read and agree to the ")
                pushStringAnnotation(tag = "URL", annotation = "https://apexaiathletics.com/privacy")
                withStyle(SpanStyle(color = com.apexai.crossfit.core.ui.theme.ElectricBlue)) {
                    append("Privacy Policy")
                }
                pop()
                append(" and ")
                pushStringAnnotation(tag = "URL", annotation = "https://apexaiathletics.com/terms")
                withStyle(SpanStyle(color = com.apexai.crossfit.core.ui.theme.ElectricBlue)) {
                    append("Terms of Service")
                }
                pop()
            }
            androidx.compose.foundation.layout.Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = uiState.privacyPolicyAccepted,
                    onCheckedChange = { viewModel.onEvent(AuthEvent.PrivacyPolicyToggled(it)) }
                )
                ClickableText(
                    text = privacyText,
                    style = MaterialTheme.typography.bodySmall.copy(color = com.apexai.crossfit.core.ui.theme.TextSecondary),
                    onClick = { offset ->
                        privacyText.getStringAnnotations("URL", offset, offset)
                            .firstOrNull()?.let { uriHandler.openUri(it.item) }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            PrimaryButton(
                text = stringResource(R.string.register_cta),
                onClick = { viewModel.onEvent(AuthEvent.RegisterClicked) },
                isLoading = uiState.isLoading,
                enabled = uiState.privacyPolicyAccepted,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
