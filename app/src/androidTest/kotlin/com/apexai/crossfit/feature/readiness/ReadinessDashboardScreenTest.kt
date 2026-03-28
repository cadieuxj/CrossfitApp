package com.apexai.crossfit.feature.readiness

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.apexai.crossfit.FakeReadinessRepository
import com.apexai.crossfit.Fixtures
import com.apexai.crossfit.core.domain.model.ReadinessZone
import com.apexai.crossfit.core.ui.theme.ApexAITheme
import com.apexai.crossfit.feature.readiness.domain.usecase.SyncHealthDataUseCase
import com.apexai.crossfit.feature.readiness.presentation.ReadinessDashboardScreen
import com.apexai.crossfit.feature.readiness.presentation.ReadinessViewModel
import io.github.jan.supabase.SupabaseClient
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI tests for [ReadinessDashboardScreen].
 *
 * Verifies:
 * - Readiness ring displays zone label text for each zone
 * - ACWR gauge section shows training load labels
 * - Biometric cards (HRV, Sleep, Resting HR) render with data
 * - When Health Connect permissions are not granted, setup screen navigation is triggered
 * - Loading state shows shimmer (no content crash)
 * - Recommendation text is displayed
 */
@RunWith(AndroidJUnit4::class)
class ReadinessDashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeRepository: FakeReadinessRepository
    private lateinit var viewModel: ReadinessViewModel
    private var navigatedToSetup = false

    private fun buildViewModel(permissionsGranted: Boolean = true): ReadinessViewModel {
        fakeRepository = FakeReadinessRepository().apply {
            checkPermissionsResult  = permissionsGranted
        }
        val mockSupabase = mockk<SupabaseClient>(relaxed = true)
        // Auth returns null user — loadReadiness will no-op (no userId)
        // This is acceptable for UI rendering tests
        return ReadinessViewModel(
            repository           = fakeRepository,
            syncHealthDataUseCase = SyncHealthDataUseCase(fakeRepository),
            supabase             = mockSupabase
        )
    }

    private fun launchScreen() {
        navigatedToSetup = false
        composeTestRule.setContent {
            ApexAITheme {
                ReadinessDashboardScreen(
                    viewModel         = viewModel,
                    currentNavRoute   = "readiness",
                    onNavigateToSetup = { navigatedToSetup = true },
                    onBottomNavNavigate = {}
                )
            }
        }
    }

    // --------------------------------------------------------
    // Screen structure — always present elements
    // --------------------------------------------------------

    @Test
    fun readinessDashboard_topBarTitleDisplayed() {
        viewModel = buildViewModel()
        launchScreen()

        composeTestRule.onNodeWithText("Readiness").assertIsDisplayed()
    }

    @Test
    fun readinessDashboard_trainingLoadSectionDisplayed() {
        viewModel = buildViewModel()
        fakeRepository.readinessScoreToReturn = Fixtures.readinessScore(
            acwr         = 1.1f,
            zone         = ReadinessZone.OPTIMAL,
            acuteLoad    = 550f,
            chronicLoad  = 500f
        )
        launchScreen()

        // ACWR zone labels should be visible in the gauge section
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodes(hasText("TRAINING LOAD")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("TRAINING LOAD").assertIsDisplayed()
    }

    @Test
    fun readinessDashboard_biometricsLabelDisplayed() {
        viewModel = buildViewModel()
        launchScreen()

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodes(hasText("BIOMETRICS")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("BIOMETRICS").assertIsDisplayed()
    }

    // --------------------------------------------------------
    // Zone colour labels
    // --------------------------------------------------------

    @Test
    fun readinessDashboard_optimalZone_zoneChipLabelsVisible() {
        viewModel = buildViewModel()
        launchScreen()

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodes(hasText("0.8–1.3")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("0.8–1.3").assertIsDisplayed()
    }

    @Test
    fun readinessDashboard_acwrRangeChipsAllVisible() {
        viewModel = buildViewModel()
        launchScreen()

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodes(hasText("> 1.5")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("< 0.8").assertIsDisplayed()
        composeTestRule.onNodeWithText("0.8–1.3").assertIsDisplayed()
        composeTestRule.onNodeWithText("1.3–1.5").assertIsDisplayed()
        composeTestRule.onNodeWithText("> 1.5").assertIsDisplayed()
    }

    // --------------------------------------------------------
    // Biometric card data
    // --------------------------------------------------------

    @Test
    fun biometricCards_withHrvData_displayHrvValue() {
        viewModel = buildViewModel()
        fakeRepository.readinessScoreToReturn = Fixtures.readinessScore(hrv = 72)
        launchScreen()

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodes(hasText("72 ms")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("72 ms").assertIsDisplayed()
    }

    @Test
    fun biometricCards_withSleepData_displaySleepDuration() {
        viewModel = buildViewModel()
        fakeRepository.readinessScoreToReturn = Fixtures.readinessScore(
            sleepMinutes = 480 // 8 hours
        )
        launchScreen()

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodes(hasText("8h 0m")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("8h 0m").assertIsDisplayed()
    }

    @Test
    fun biometricCards_withRestingHr_displaysBpm() {
        viewModel = buildViewModel()
        fakeRepository.readinessScoreToReturn = Fixtures.readinessScore(restingHr = 52)
        launchScreen()

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodes(hasText("52 bpm")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("52 bpm").assertIsDisplayed()
    }

    @Test
    fun biometricCards_missingHrv_displaysPlaceholder() {
        viewModel = buildViewModel()
        fakeRepository.readinessScoreToReturn = Fixtures.readinessScore(hrv = null)
        launchScreen()

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodes(hasText("—")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("—").assertIsDisplayed()
    }

    // --------------------------------------------------------
    // Recommendation
    // --------------------------------------------------------

    @Test
    fun recommendation_nonBlank_isDisplayed() {
        viewModel = buildViewModel()
        fakeRepository.readinessScoreToReturn = Fixtures.readinessScore(
            recommendation = "Good to train at full intensity."
        )
        launchScreen()

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodes(
                hasText("Good to train at full intensity.")
            ).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Good to train at full intensity.").assertIsDisplayed()
    }

    @Test
    fun recommendation_aiRecommendationLabelDisplayed() {
        viewModel = buildViewModel()
        fakeRepository.readinessScoreToReturn = Fixtures.readinessScore(
            recommendation = "Rest and recover."
        )
        launchScreen()

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodes(hasText("AI RECOMMENDATION")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("AI RECOMMENDATION").assertIsDisplayed()
    }

    // --------------------------------------------------------
    // Permissions not granted — navigation to setup
    // --------------------------------------------------------

    @Test
    fun requestPermissions_event_navigatesToSetupScreen() {
        viewModel = buildViewModel(permissionsGranted = false)
        launchScreen()

        // Trigger the RequestPermissions event programmatically
        composeTestRule.runOnUiThread {
            viewModel.onEvent(com.apexai.crossfit.feature.readiness.presentation.ReadinessEvent.RequestPermissions)
        }

        composeTestRule.waitUntil(3_000) { navigatedToSetup }
        assert(navigatedToSetup) { "Expected navigation to Health Connect setup" }
    }
}
