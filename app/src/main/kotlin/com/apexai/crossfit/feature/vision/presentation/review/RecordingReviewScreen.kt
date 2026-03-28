package com.apexai.crossfit.feature.vision.presentation.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.ui.PlayerView
import com.apexai.crossfit.core.ui.components.PrimaryButton
import com.apexai.crossfit.core.ui.components.ApexTextButton
import com.apexai.crossfit.core.ui.theme.ApexTypography
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary

@Composable
fun RecordingReviewScreen(
    videoUri: String,
    onNavigateBack: () -> Unit,
    onNavigateToReport: (String) -> Unit,
    viewModel: RecordingReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(videoUri) {
        viewModel.loadVideo(videoUri)
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is RecordingReviewEffect.NavigateToReport -> onNavigateToReport(effect.analysisId)
            }
        }
    }

    // Gemini cross-border transfer consent dialog — required by Quebec Law 25, Art. 17
    if (uiState.showGeminiConsentDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissGeminiConsent() },
            title = { Text("AI Analysis Consent") },
            text = {
                Text(
                    "Your video will be sent to Google's Gemini AI service for movement analysis. " +
                    "Google's servers are located outside Canada.\n\n" +
                    "The video is deleted from Google's servers immediately after analysis. " +
                    "Your coaching report is stored securely on Supabase.\n\n" +
                    "Do you consent to sending your video to Google for analysis?"
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmGeminiConsent() }) {
                    Text("I Consent")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissGeminiConsent() }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeepBlack)
    ) {
        // Video playback
        val player = viewModel.poolManager.acquire()
        DisposableEffect(videoUri) {
            player.setMediaItem(androidx.media3.common.MediaItem.fromUri(videoUri))
            player.prepare()
            onDispose { viewModel.poolManager.release(player) }
        }

        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = true
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(8.dp)
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.7f))
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            uiState.error?.let { error ->
                Text(error, style = ApexTypography.bodySmall, color = com.apexai.crossfit.core.ui.theme.ColorError)
            }

            if (uiState.isUploading || uiState.isAnalyzing) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = ElectricBlue,
                        strokeWidth = 2.dp,
                        modifier = Modifier.then(Modifier.height(24.dp))
                    )
                    Text(
                        if (uiState.isUploading) "Uploading… ${(uiState.uploadProgress * 100).toInt()}%"
                        else "Analyzing movement…",
                        style = ApexTypography.bodyMedium,
                        color = TextSecondary
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ApexTextButton(
                        text = "Re-record",
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryButton(
                        text = "Analyze with AI",
                        onClick = { viewModel.requestGeminiConsent(videoUri) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
