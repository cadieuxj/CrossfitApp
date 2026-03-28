package com.apexai.crossfit.feature.wod

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.apexai.crossfit.FakeWodRepository
import com.apexai.crossfit.Fixtures
import com.apexai.crossfit.TestCoroutineRule
import com.apexai.crossfit.core.domain.model.ScoringMetric
import com.apexai.crossfit.core.domain.model.TimeDomain
import com.apexai.crossfit.feature.wod.domain.usecase.SubmitResultUseCase
import com.apexai.crossfit.feature.wod.presentation.log.WodLogEffect
import com.apexai.crossfit.feature.wod.presentation.log.WodLogEvent
import com.apexai.crossfit.feature.wod.presentation.log.WodLogViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [WodLogViewModel].
 *
 * Key constraints verified:
 * - PR detection comes from [WorkoutResult.newPrs] (server response), never computed locally.
 * - [WodLogEffect.PrAchieved] is emitted when the server returns PRs.
 * - Submit is blocked when score is blank.
 * - UI state transitions are correct through the submit lifecycle.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WodLogViewModelTest {

    @get:Rule
    val coroutineRule = TestCoroutineRule()

    private lateinit var fakeRepository: FakeWodRepository
    private lateinit var submitResultUseCase: SubmitResultUseCase
    private lateinit var viewModel: WodLogViewModel

    private val testWodId = "wod-abc-123"

    @Before
    fun setUp() {
        fakeRepository = FakeWodRepository().apply {
            workoutToReturn = Fixtures.workout(
                id            = testWodId,
                name          = "Fran",
                timeDomain    = TimeDomain.RFT,
                scoringMetric = ScoringMetric.TIME
            )
        }
        submitResultUseCase = SubmitResultUseCase(fakeRepository)
        viewModel = WodLogViewModel(
            submitResultUseCase = submitResultUseCase,
            repository          = fakeRepository,
            savedStateHandle    = SavedStateHandle(mapOf("wodId" to testWodId))
        )
    }

    // --------------------------------------------------------
    // Init / loading
    // --------------------------------------------------------

    @Test
    fun `init_workoutLoads_uiStateContainsWorkout`() = coroutineRule.runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("Should not be loading after init", state.isLoading)
        assertNotNull("Workout should be populated", state.workout)
        assertEquals("Fran", state.workout?.name)
    }

    @Test
    fun `init_repositoryThrows_uiStateContainsError`() = coroutineRule.runTest {
        fakeRepository.getWorkoutError = RuntimeException("Network unavailable")
        val vm = WodLogViewModel(
            submitResultUseCase = submitResultUseCase,
            repository          = fakeRepository,
            savedStateHandle    = SavedStateHandle(mapOf("wodId" to testWodId))
        )

        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse("Loading should be false after error", state.isLoading)
        assertEquals("Network unavailable", state.error)
        assertNull("Workout should be null on error", state.workout)
    }

    // --------------------------------------------------------
    // Event handling — field updates
    // --------------------------------------------------------

    @Test
    fun `onEvent_scoreChanged_stateReflectsNewScore`() = coroutineRule.runTest {
        advanceUntilIdle()

        viewModel.onEvent(WodLogEvent.ScoreChanged("3:45"))

        assertEquals("3:45", viewModel.uiState.value.score)
    }

    @Test
    fun `onEvent_scoreChanged_clearsExistingError`() = coroutineRule.runTest {
        advanceUntilIdle()
        // Trigger an error first
        viewModel.onEvent(WodLogEvent.SubmitClicked)
        advanceUntilIdle()
        assertNotNull("Error should be set after blank submit", viewModel.uiState.value.error)

        viewModel.onEvent(WodLogEvent.ScoreChanged("5"))

        assertNull("Error should be cleared after typing", viewModel.uiState.value.error)
    }

    @Test
    fun `onEvent_rxdToggled_stateReflectsNewValue`() = coroutineRule.runTest {
        advanceUntilIdle()

        viewModel.onEvent(WodLogEvent.RxdToggled(false))

        assertFalse(viewModel.uiState.value.rxd)
    }

    @Test
    fun `onEvent_notesChanged_stateReflectsNewNotes`() = coroutineRule.runTest {
        advanceUntilIdle()

        viewModel.onEvent(WodLogEvent.NotesChanged("Felt strong today"))

        assertEquals("Felt strong today", viewModel.uiState.value.notes)
    }

    @Test
    fun `onEvent_rpeSelected_stateReflectsNewRpe`() = coroutineRule.runTest {
        advanceUntilIdle()

        viewModel.onEvent(WodLogEvent.RpeSelected(8))

        assertEquals(8, viewModel.uiState.value.rpe)
    }

    // --------------------------------------------------------
    // Submit validation — blank score
    // --------------------------------------------------------

    @Test
    fun `submit_scoreIsBlank_setsErrorAndDoesNotCallRepository`() = coroutineRule.runTest {
        advanceUntilIdle()
        // Default score is empty string

        viewModel.onEvent(WodLogEvent.SubmitClicked)
        advanceUntilIdle()

        assertEquals("Please enter your score", viewModel.uiState.value.error)
        assertEquals(0, fakeRepository.logResultCallCount)
    }

    @Test
    fun `submit_scoreIsWhitespaceOnly_setsError`() = coroutineRule.runTest {
        advanceUntilIdle()
        viewModel.onEvent(WodLogEvent.ScoreChanged("   "))

        viewModel.onEvent(WodLogEvent.SubmitClicked)
        advanceUntilIdle()

        assertEquals("Please enter your score", viewModel.uiState.value.error)
    }

    // --------------------------------------------------------
    // Submit success — no PRs
    // --------------------------------------------------------

    @Test
    fun `submit_validScoreNoPrs_emitsNavigateBack`() = coroutineRule.runTest {
        advanceUntilIdle()
        fakeRepository.logResultResponse = Result.success(
            Fixtures.workoutResult(score = "3:45", newPrs = emptyList())
        )
        viewModel.onEvent(WodLogEvent.ScoreChanged("3:45"))

        viewModel.effects.test {
            viewModel.onEvent(WodLogEvent.SubmitClicked)
            advanceUntilIdle()

            assertEquals(WodLogEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `submit_validScore_isSubmittingIsTrueDuringCall`() = coroutineRule.runTest {
        advanceUntilIdle()
        viewModel.onEvent(WodLogEvent.ScoreChanged("3:45"))
        viewModel.onEvent(WodLogEvent.SubmitClicked)

        // Before idle — should be submitting
        assertTrue(viewModel.uiState.value.isSubmitting)

        advanceUntilIdle()

        assertFalse("isSubmitting should be false after completion", viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun `submit_validScore_inputPassedToRepositoryCorrectly`() = coroutineRule.runTest {
        advanceUntilIdle()
        viewModel.onEvent(WodLogEvent.ScoreChanged("225"))
        viewModel.onEvent(WodLogEvent.RxdToggled(false))
        viewModel.onEvent(WodLogEvent.RpeSelected(9))
        viewModel.onEvent(WodLogEvent.NotesChanged("Heavy day"))

        viewModel.onEvent(WodLogEvent.SubmitClicked)
        advanceUntilIdle()

        val input = fakeRepository.lastLogResultInput
        assertNotNull(input)
        assertEquals(testWodId, input?.workoutId)
        assertEquals("225", input?.score)
        assertEquals(225.0, input?.scoreNumeric)
        assertFalse(input?.rxd ?: true)
        assertEquals(9, input?.rpe)
        assertEquals("Heavy day", input?.notes)
    }

    @Test
    fun `submit_notesIsBlank_notesInInputIsNull`() = coroutineRule.runTest {
        advanceUntilIdle()
        viewModel.onEvent(WodLogEvent.ScoreChanged("100"))
        // notes left as empty string

        viewModel.onEvent(WodLogEvent.SubmitClicked)
        advanceUntilIdle()

        assertNull("Blank notes should be submitted as null", fakeRepository.lastLogResultInput?.notes)
    }

    // --------------------------------------------------------
    // Submit success — PR celebration (server-side detection)
    // --------------------------------------------------------

    @Test
    fun `submit_serverReturnsPrs_emitsPrAchievedEffect`() = coroutineRule.runTest {
        advanceUntilIdle()
        val pr = Fixtures.personalRecord(movementName = "Thruster", value = 60.0)
        fakeRepository.logResultResponse = Result.success(
            Fixtures.workoutResult(newPrs = listOf(pr))
        )
        viewModel.onEvent(WodLogEvent.ScoreChanged("3:45"))

        viewModel.effects.test {
            viewModel.onEvent(WodLogEvent.SubmitClicked)
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is WodLogEffect.PrAchieved)
            val prEffect = effect as WodLogEffect.PrAchieved
            assertEquals(1, prEffect.prs.size)
            assertEquals("Thruster", prEffect.prs[0].movementName)
            assertEquals(60.0, prEffect.prs[0].value, 0.001)
        }
    }

    @Test
    fun `submit_serverReturnsPrs_navigateBackNotEmittedUntilDismiss`() = coroutineRule.runTest {
        advanceUntilIdle()
        val pr = Fixtures.personalRecord()
        fakeRepository.logResultResponse = Result.success(
            Fixtures.workoutResult(newPrs = listOf(pr))
        )
        viewModel.onEvent(WodLogEvent.ScoreChanged("3:45"))

        viewModel.effects.test {
            viewModel.onEvent(WodLogEvent.SubmitClicked)
            advanceUntilIdle()

            val first = awaitItem()
            assertTrue("First effect should be PrAchieved", first is WodLogEffect.PrAchieved)

            viewModel.onEvent(WodLogEvent.DismissPrSheet)
            advanceUntilIdle()

            assertEquals(WodLogEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `submit_prDetectionIsServerSide_clientDoesNotComputePrs`() = coroutineRule.runTest {
        // Critical constraint: PRs come only from WorkoutResult.newPrs returned by the server.
        // The ViewModel must never inspect the score value to determine if it is a PR.
        advanceUntilIdle()

        // Server says no PRs even though score is high
        fakeRepository.logResultResponse = Result.success(
            Fixtures.workoutResult(score = "9999", newPrs = emptyList())
        )
        viewModel.onEvent(WodLogEvent.ScoreChanged("9999"))

        viewModel.effects.test {
            viewModel.onEvent(WodLogEvent.SubmitClicked)
            advanceUntilIdle()

            // Should navigate back — no PrAchieved emitted
            assertEquals(WodLogEffect.NavigateBack, awaitItem())
        }
    }

    // --------------------------------------------------------
    // Submit failure
    // --------------------------------------------------------

    @Test
    fun `submit_repositoryFails_setsErrorInState`() = coroutineRule.runTest {
        advanceUntilIdle()
        fakeRepository.logResultResponse = Result.failure(RuntimeException("Supabase unavailable"))
        viewModel.onEvent(WodLogEvent.ScoreChanged("3:45"))

        viewModel.onEvent(WodLogEvent.SubmitClicked)
        advanceUntilIdle()

        assertEquals("Supabase unavailable", viewModel.uiState.value.error)
        assertFalse("isSubmitting should be false after failure", viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun `submit_repositoryFails_emitsShowErrorEffect`() = coroutineRule.runTest {
        advanceUntilIdle()
        fakeRepository.logResultResponse = Result.failure(RuntimeException("Timeout"))
        viewModel.onEvent(WodLogEvent.ScoreChanged("3:45"))

        viewModel.effects.test {
            viewModel.onEvent(WodLogEvent.SubmitClicked)
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is WodLogEffect.ShowError)
            assertEquals("Timeout", (effect as WodLogEffect.ShowError).message)
        }
    }

    @Test
    fun `submit_repositoryFailsWithNullMessage_emitsFallbackError`() = coroutineRule.runTest {
        advanceUntilIdle()
        fakeRepository.logResultResponse = Result.failure(RuntimeException(null as String?))
        viewModel.onEvent(WodLogEvent.ScoreChanged("3:45"))

        viewModel.effects.test {
            viewModel.onEvent(WodLogEvent.SubmitClicked)
            advanceUntilIdle()

            val effect = awaitItem() as WodLogEffect.ShowError
            assertEquals("Failed to submit", effect.message)
        }
    }
}
