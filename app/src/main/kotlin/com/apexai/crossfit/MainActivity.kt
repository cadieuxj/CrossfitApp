package com.apexai.crossfit

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.apexai.crossfit.core.ui.navigation.AppNavigation
import com.apexai.crossfit.core.ui.theme.ApexTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Holds a pending deep-link route delivered via notification tap or external intent.
    // Null means no pending navigation — AppNavigation starts at the default SplashScreen.
    private val pendingRoute = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Consume any route requested by the launching intent (e.g. notification tap).
        pendingRoute.value = intent.getStringExtra(EXTRA_NAVIGATE_TO)

        setContent {
            ApexTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.ui.graphics.Color(0xFF0A0A0F)
                ) {
                    AppNavigation(
                        pendingRoute = pendingRoute.value,
                        onPendingRouteConsumed = { pendingRoute.value = null }
                    )
                }
            }
        }
    }

    // Called when the activity is already running (FLAG_ACTIVITY_SINGLE_TOP) and a new
    // notification tap arrives — e.g. user taps a second notification while the app is open.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.getStringExtra(EXTRA_NAVIGATE_TO)?.let { route ->
            pendingRoute.value = route
        }
    }

    companion object {
        const val EXTRA_NAVIGATE_TO = "navigate_to"
    }
}
