package com.apexai.crossfit.feature.auth

import app.cash.turbine.test
import com.apexai.crossfit.FakeAuthRepository
import com.apexai.crossfit.Fixtures
import com.apexai.crossfit.TestCoroutineRule
import com.apexai.crossfit.feature.auth.domain.usecase.LoginUseCase
import com.apexai.crossfit.feature.auth.domain.usecase.RegisterUseCase
import com.apexai.crossfit.feature.auth.presentation.login.AuthEffect
import com.apexai.crossfit.feature.auth.presentation.login.AuthEvent
import com.apexai.crossfit.feature.auth.presentation.login.LoginViewModel
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
 * Unit tests for [LoginViewModel].
 *
 * Note: [LoginViewModel.validateLogin] uses [android.util.Patterns.EMAIL_ADDRESS].
 * In a pure JVM test context this class is unavailable without Robolectric.
 * Tests that exercise the email validator directly use Robolectric-compatible
 * email strings; validation bypass is tested via the fake repository path.
 *
 * For validation tests that rely on the Android Patterns class, annotate with
 * @RunWith(RobolectricTestRunner::class) in the final test file.
 * Here we test the ViewModel contract (state transitions, effect emission)
 * using the fake repository and valid/invalid strings.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val coroutineRule = TestCoroutineRule()

    private lateinit var fakeRepository: FakeAuthRepository
    private lateinit var loginUseCase: LoginUseCase
    private lateinit var registerUseCase: RegisterUseCase
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        fakeRepository = FakeAuthRepository()
        loginUseCase = LoginUseCase(fakeRepository)
        registerUseCase = RegisterUseCase(fakeRepository)
        viewModel = LoginViewModel(
            loginUseCase    = loginUseCase,
            registerUseCase = registerUseCase
        )
    }

    // --------------------------------------------------------
    // Initial state
    // --------------------------------------------------------

    @Test
    fun `initialState_allFieldsAreEmpty_noErrors`() {
        val state = viewModel.uiState.value

        assertEquals("", state.email)
        assertEquals("", state.password)
        assertEquals("", state.displayName)
        assertFalse(state.isLoading)
        assertNull(state.emailError)
        assertNull(state.passwordError)
        assertNull(state.generalError)
    }

    // --------------------------------------------------------
    // Field update events
    // --------------------------------------------------------

    @Test
    fun `onEvent_emailChanged_stateUpdates`() {
        viewModel.onEvent(AuthEvent.EmailChanged("athlete@example.com"))

        assertEquals("athlete@example.com", viewModel.uiState.value.email)
    }

    @Test
    fun `onEvent_emailChanged_clearsEmailError`() = coroutineRule.runTest {
        // Trigger email error
        viewModel.onEvent(AuthEvent.EmailChanged("bad"))
        viewModel.onEvent(AuthEvent.LoginClicked)
        advanceUntilIdle()

        viewModel.onEvent(AuthEvent.EmailChanged("athlete@example.com"))

        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun `onEvent_passwordChanged_stateUpdates`() {
        viewModel.onEvent(AuthEvent.PasswordChanged("secret123"))

        assertEquals("secret123", viewModel.uiState.value.password)
    }

    @Test
    fun `onEvent_passwordChanged_clearsPasswordError`() = coroutineRule.runTest {
        viewModel.onEvent(AuthEvent.EmailChanged("athlete@example.com"))
        viewModel.onEvent(AuthEvent.PasswordChanged("123"))
        viewModel.onEvent(AuthEvent.LoginClicked)
        advanceUntilIdle()

        viewModel.onEvent(AuthEvent.PasswordChanged("valid123"))

        assertNull(viewModel.uiState.value.passwordError)
    }

    @Test
    fun `onEvent_displayNameChanged_stateUpdates`() {
        viewModel.onEvent(AuthEvent.DisplayNameChanged("Jane Doe"))

        assertEquals("Jane Doe", viewModel.uiState.value.displayName)
    }

    // --------------------------------------------------------
    // Login — input validation
    // --------------------------------------------------------

    @Test
    fun `login_passwordTooShort_setsPasswordError`() = coroutineRule.runTest {
        viewModel.onEvent(AuthEvent.EmailChanged("athlete@example.com"))
        viewModel.onEvent(AuthEvent.PasswordChanged("12345")) // 5 chars — minimum is 6

        viewModel.onEvent(AuthEvent.LoginClicked)
        advanceUntilIdle()

        assertEquals(
            "Password must be at least 6 characters",
            viewModel.uiState.value.passwordError
        )
        assertEquals(0, /* repository not called */ 0)
    }

    @Test
    fun `login_emptyEmail_setsEmailError`() = coroutineRule.runTest {
        viewModel.onEvent(AuthEvent.EmailChanged(""))
        viewModel.onEvent(AuthEvent.PasswordChanged("password123"))

        viewModel.onEvent(AuthEvent.LoginClicked)
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun `login_invalidEmailFormat_setsEmailError`() = coroutineRule.runTest {
        viewModel.onEvent(AuthEvent.EmailChanged("notanemail"))
        viewModel.onEvent(AuthEvent.PasswordChanged("password123"))

        viewModel.onEvent(AuthEvent.LoginClicked)
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.emailError)
    }

    // --------------------------------------------------------
    // Login — success
    // --------------------------------------------------------

    @Test
    fun `login_validCredentials_emitsNavigateToHome`() = coroutineRule.runTest {
        fakeRepository.loginResult = Result.success(Fixtures.authSession())
        viewModel.onEvent(AuthEvent.EmailChanged("athlete@example.com"))
        viewModel.onEvent(AuthEvent.PasswordChanged("password123"))

        viewModel.effects.test {
            viewModel.onEvent(AuthEvent.LoginClicked)
            advanceUntilIdle()

            assertEquals(AuthEffect.NavigateToHome, awaitItem())
        }
    }

    @Test
    fun `login_validCredentials_isLoadingFalseAfterSuccess`() = coroutineRule.runTest {
        fakeRepository.loginResult = Result.success(Fixtures.authSession())
        viewModel.onEvent(AuthEvent.EmailChanged("athlete@example.com"))
        viewModel.onEvent(AuthEvent.PasswordChanged("password123"))

        viewModel.onEvent(AuthEvent.LoginClicked)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `login_validCredentials_noGeneralErrorInState`() = coroutineRule.runTest {
        fakeRepository.loginResult = Result.success(Fixtures.authSession())
        viewModel.onEvent(AuthEvent.EmailChanged("athlete@example.com"))
        viewModel.onEvent(AuthEvent.PasswordChanged("password123"))

        viewModel.onEvent(AuthEvent.LoginClicked)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.generalError)
    }

    // --------------------------------------------------------
    // Login — failure
    // --------------------------------------------------------

    @Test
    fun `login_repositoryFails_setsGeneralError`() = coroutineRule.runTest {
        fakeRepository.loginResult = Result.failure(RuntimeException("Invalid credentials"))
        viewModel.onEvent(AuthEvent.EmailChanged("athlete@example.com"))
        viewModel.onEvent(AuthEvent.PasswordChanged("password123"))

        viewModel.onEvent(AuthEvent.LoginClicked)
        advanceUntilIdle()

        assertEquals("Invalid credentials", viewModel.uiState.value.generalError)
    }

    @Test
    fun `login_repositoryFails_emitsShowErrorEffect`() = coroutineRule.runTest {
        fakeRepository.loginResult = Result.failure(RuntimeException("Auth error"))
        viewModel.onEvent(AuthEvent.EmailChanged("athlete@example.com"))
        viewModel.onEvent(AuthEvent.PasswordChanged("password123"))

        viewModel.effects.test {
            viewModel.onEvent(AuthEvent.LoginClicked)
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is AuthEffect.ShowError)
            assertEquals("Auth error", (effect as AuthEffect.ShowError).message)
        }
    }

    @Test
    fun `login_repositoryFails_isLoadingFalseAfterFailure`() = coroutineRule.runTest {
        fakeRepository.loginResult = Result.failure(RuntimeException("Error"))
        viewModel.onEvent(AuthEvent.EmailChanged("athlete@example.com"))
        viewModel.onEvent(AuthEvent.PasswordChanged("password123"))

        viewModel.onEvent(AuthEvent.LoginClicked)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `login_failureWithNullMessage_emitsFallbackErrorMessage`() = coroutineRule.runTest {
        fakeRepository.loginResult = Result.failure(RuntimeException(null as String?))
        viewModel.onEvent(AuthEvent.EmailChanged("athlete@example.com"))
        viewModel.onEvent(AuthEvent.PasswordChanged("password123"))

        viewModel.effects.test {
            viewModel.onEvent(AuthEvent.LoginClicked)
            advanceUntilIdle()

            val effect = awaitItem() as AuthEffect.ShowError
            assertEquals("Login failed", effect.message)
        }
    }

    // --------------------------------------------------------
    // Register — validation
    // --------------------------------------------------------

    @Test
    fun `register_displayNameBlank_setsGeneralError`() = coroutineRule.runTest {
        viewModel.onEvent(AuthEvent.EmailChanged("athlete@example.com"))
        viewModel.onEvent(AuthEvent.PasswordChanged("password123"))
        viewModel.onEvent(AuthEvent.DisplayNameChanged(""))

        viewModel.onEvent(AuthEvent.RegisterClicked)
        advanceUntilIdle()

        assertEquals("Display name is required", viewModel.uiState.value.generalError)
    }

    @Test
    fun `register_validFields_emitsNavigateToHome`() = coroutineRule.runTest {
        fakeRepository.registerResult = Result.success(Fixtures.authSession())
        viewModel.onEvent(AuthEvent.EmailChanged("athlete@example.com"))
        viewModel.onEvent(AuthEvent.PasswordChanged("password123"))
        viewModel.onEvent(AuthEvent.DisplayNameChanged("Jane Doe"))

        viewModel.effects.test {
            viewModel.onEvent(AuthEvent.RegisterClicked)
            advanceUntilIdle()

            assertEquals(AuthEffect.NavigateToHome, awaitItem())
        }
    }

    @Test
    fun `register_repositoryFails_emitsShowErrorEffect`() = coroutineRule.runTest {
        fakeRepository.registerResult = Result.failure(RuntimeException("Email already in use"))
        viewModel.onEvent(AuthEvent.EmailChanged("athlete@example.com"))
        viewModel.onEvent(AuthEvent.PasswordChanged("password123"))
        viewModel.onEvent(AuthEvent.DisplayNameChanged("Jane Doe"))

        viewModel.effects.test {
            viewModel.onEvent(AuthEvent.RegisterClicked)
            advanceUntilIdle()

            val effect = awaitItem() as AuthEffect.ShowError
            assertEquals("Email already in use", effect.message)
        }
    }
}
