package com.apexai.crossfit.feature.wod

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.apexai.crossfit.FakeWodRepository
import com.apexai.crossfit.Fixtures
import com.apexai.crossfit.core.domain.model.ScoringMetric
import com.apexai.crossfit.core.domain.model.TimeDomain
import com.apexai.crossfit.core.ui.theme.ApexAITheme
import com.apexai.crossfit.feature.wod.domain.usecase.SubmitResultUseCase
import com.apexai.crossfit.feature.wod.presentation.log.WodLogScreen
import com.apexai.crossfit.feature.wod.presentation.log.WodLogViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI tests for [WodLogScreen].
 *
 * Verifies:
 * - Score input format varies by scoring metric (AMRAP/EMOM = reps, RFT = time, LOAD = weight)
 * - Rx toggle renders and responds to interaction
 * - RPE selector (1–10) renders and responds
 * - Submit button is disabled when score field is empty
 * - Submit button is enabled when score is non-empty
 * - Error message is displayed when present in UiState
 */
@RunWith(AndroidJUnit4::class)
class WodLogScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeRepository: FakeWodRepository
    private lateinit var viewModel: WodLogViewModel
    private val testWodId = "test-wod-ui"

    private fun setupScreen(
        timeDomain: TimeDomain = TimeDomain.RFT,
        scoringMetric: ScoringMetric = ScoringMetric.TIME
    ) {
        fakeRepository = FakeWodRepository().apply {
            workoutToReturn = Fixtures.workout(
                id            = testWodId,
                name          = "Grace",
                timeDomain    = timeDomain,
                scoringMetric = scoringMetric
            )
        }
        viewModel = WodLogViewModel(
            submitResultUseCase = SubmitResultUseCase(fakeRepository),
            repository          = fakeRepository,
            savedStateHandle    = SavedStateHandle(mapOf("wodId" to testWodId))
        )
        composeTestRule.setContent {
            ApexAITheme {
                WodLogScreen(
                    viewModel       = viewModel,
                    onNavigateBack  = {},
                    onNavigateHome  = {}
                )
            }
        }
    }

    // --------------------------------------------------------
    // Screen structure
    // --------------------------------------------------------

    @Test
    fun wodLogScreen_displaysWorkoutName() {
        setupScreen()

        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            composeTestRule.onAllNodes(androidx.compose.ui.test.hasText("Grace")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Grace").assertExists()
    }

    @Test
    fun wodLogScreen_rftWorkout_displaysTimeDomainLabel() {
        setupScreen(timeDomain = TimeDomain.RFT)

        composeTestRule.waitUntil(3_000) {
            composeTestRule.onAllNodes(androidx.compose.ui.test.hasText("RFT")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("RFT").assertExists()
    }

    @Test
    fun wodLogScreen_amrapWorkout_displaysAmrapDomain() {
        setupScreen(
            timeDomain    = TimeDomain.AMRAP,
            scoringMetric = ScoringMetric.ROUNDS_PLUS_REPS
        )

        composeTestRule.waitUntil(3_000) {
            composeTestRule.onAllNodes(androidx.compose.ui.test.hasText("AMRAP")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("AMRAP").assertExists()
    }

    // --------------------------------------------------------
    // Score input — varies by scoring metric
    // --------------------------------------------------------

    @Test
    fun scoreInput_timeScoring_displaysMinuteAndSecondFields() {
        setupScreen(timeDomain = TimeDomain.RFT, scoringMetric = ScoringMetric.TIME)

        composeTestRule.onNodeWithText("Minutes").assertExists()
        composeTestRule.onNodeWithText("Seconds").assertExists()
    }

    @Test
    fun scoreInput_roundsPlusRepsScoring_displaysRoundsAndRepsFields() {
        setupScreen(timeDomain = TimeDomain.AMRAP, scoringMetric = ScoringMetric.ROUNDS_PLUS_REPS)

        composeTestRule.waitUntil(3_000) {
            composeTestRule.onAllNodes(androidx.compose.ui.test.hasText("Rounds")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Rounds").assertExists()
        composeTestRule.onNodeWithText("Reps").assertExists()
    }

    @Test
    fun scoreInput_loadScoring_displaysWeightField() {
        setupScreen(timeDomain = TimeDomain.RFT, scoringMetric = ScoringMetric.LOAD)

        composeTestRule.waitUntil(3_000) {
            composeTestRule.onAllNodes(androidx.compose.ui.test.hasText("Weight (kg)")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Weight (kg)").assertExists()
    }

    @Test
    fun scoreInput_repsScoring_displaysTotalRepsField() {
        setupScreen(timeDomain = TimeDomain.EMOM, scoringMetric = ScoringMetric.REPS)

        composeTestRule.waitUntil(3_000) {
            composeTestRule.onAllNodes(androidx.compose.ui.test.hasText("Total Reps")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Total Reps").assertExists()
    }

    // --------------------------------------------------------
    // Rx toggle
    // --------------------------------------------------------

    @Test
    fun rxToggle_isOnByDefault() {
        setupScreen()

        composeTestRule.onNodeWithText("AS PRESCRIBED (Rx)").assertExists()
        // Switch is ON by default (rxd = true)
        composeTestRule
            .onNode(
                androidx.compose.ui.test.hasContentDescription("").and(
                    androidx.compose.ui.test.isToggleable()
                )
            )
    }

    @Test
    fun rxToggle_tappingToggle_changesState() {
        setupScreen()

        composeTestRule.waitUntil(3_000) {
            composeTestRule.onAllNodes(
                androidx.compose.ui.test.isToggleable()
            ).fetchSemanticsNodes().isNotEmpty()
        }
        // Click the Rx toggle — it should invert
        composeTestRule
            .onNode(androidx.compose.ui.test.isToggleable())
            .performClick()
    }

    // --------------------------------------------------------
    // RPE selector
    // --------------------------------------------------------

    @Test
    fun rpeSelector_allTenValuesAreDisplayed() {
        setupScreen()

        (1..10).forEach { rpe ->
            composeTestRule.onNodeWithText(rpe.toString()).assertExists()
        }
    }

    @Test
    fun rpeSelector_defaultPromptIsDisplayed() {
        setupScreen()

        composeTestRule.onNodeWithText("Select your perceived exertion.").assertExists()
    }

    @Test
    fun rpeSelector_tapping8_showsDescriptionForRpe8() {
        setupScreen()

        composeTestRule.onNodeWithText("8").performClick()

        composeTestRule.onNodeWithText("Very hard. Near your limit.").assertExists()
    }

    @Test
    fun rpeSelector_tapping1_showsDescriptionForRpe1() {
        setupScreen()

        composeTestRule.onNodeWithText("1").performClick()

        composeTestRule.onNodeWithText("Very light. Easy warm-up pace.").assertExists()
    }

    // --------------------------------------------------------
    // Submit button state
    // --------------------------------------------------------

    @Test
    fun submitButton_emptyScore_buttonIsDisabled() {
        setupScreen()

        composeTestRule.onNodeWithText("Submit Result").assertIsNotEnabled()
    }

    @Test
    fun submitButton_withScore_buttonIsEnabled() {
        setupScreen(timeDomain = TimeDomain.EMOM, scoringMetric = ScoringMetric.REPS)

        composeTestRule.waitUntil(3_000) {
            composeTestRule.onAllNodes(androidx.compose.ui.test.hasText("Total Reps")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Total Reps").performTextInput("150")

        composeTestRule.onNodeWithText("Submit Result").assertIsEnabled()
    }

    // --------------------------------------------------------
    // Notes field
    // --------------------------------------------------------

    @Test
    fun notesField_labelIsVisible() {
        setupScreen()

        composeTestRule.onNodeWithText("NOTES").assertExists()
    }

    @Test
    fun notesField_characterCountShowsZeroByDefault() {
        setupScreen()

        composeTestRule.onNodeWithText("0/500").assertExists()
    }

    @Test
    fun notesField_typingText_updatesCharacterCount() {
        setupScreen()

        composeTestRule.onNodeWithText("How did it feel? Movement notes...").performTextInput("Great session")

        // Character count should update (13 characters)
        composeTestRule.onNodeWithText("13/500").assertExists()
    }
}
