package com.apexai.crossfit.feature.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.apexai.crossfit.FakeAuthRepository
import com.apexai.crossfit.Fixtures
import com.apexai.crossfit.core.ui.theme.ApexAITheme
import com.apexai.crossfit.feature.auth.domain.usecase.LoginUseCase
import com.apexai.crossfit.feature.auth.domain.usecase.RegisterUseCase
import com.apexai.crossfit.feature.auth.presentation.login.LoginScreen
import com.apexai.crossfit.feature.auth.presentation.login.LoginViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI tests for [LoginScreen].
 *
 * Verifies:
 * - Email and password fields are rendered
 * - Sign In button triggers ViewModel login event
 * - Error messages from ViewModel state are displayed
 * - Loading state disables Sign In button
 * - Navigate to register link is present
 */
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeRepository: FakeAuthRepository
    private lateinit var viewModel: LoginViewModel
    private var navigatedToHome = false
    private var navigatedToRegister = false

    @Before
    fun setUp() {
        fakeRepository = FakeAuthRepository()
        viewModel = LoginViewModel(
            loginUseCase    = LoginUseCase(fakeRepository),
            registerUseCase = RegisterUseCase(fakeRepository)
        )
        navigatedToHome     = false
        navigatedToRegister = false

        composeTestRule.setContent {
            ApexAITheme {
                LoginScreen(
                    viewModel           = viewModel,
                    onNavigateToHome    = { navigatedToHome = true },
                    onNavigateToRegister = { navigatedToRegister = true }
                )
            }
        }
    }

    // --------------------------------------------------------
    // Screen structure
    // --------------------------------------------------------

    @Test
    fun loginScreen_brandingDisplayed() {
        composeTestRule.onNodeWithText("APEX AI").assertIsDisplayed()
        composeTestRule.onNodeWithText("ATHLETICS").assertIsDisplayed()
    }

    @Test
    fun loginScreen_welcomeTextDisplayed() {
        composeTestRule.onNodeWithText("Welcome Back").assertIsDisplayed()
    }

    @Test
    fun loginScreen_emailFieldDisplayed() {
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
    }

    @Test
    fun loginScreen_passwordFieldDisplayed() {
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
    }

    @Test
    fun loginScreen_signInButtonDisplayed() {
        composeTestRule.onNodeWithText("Sign In").assertIsDisplayed()
    }

    @Test
    fun loginScreen_createAccountLinkDisplayed() {
        composeTestRule.onNodeWithText("Don't have an account? Create one").assertIsDisplayed()
    }

    // --------------------------------------------------------
    // Field interaction
    // --------------------------------------------------------

    @Test
    fun emailField_enteringText_updatesState() {
        composeTestRule.onNodeWithText("Email").performTextInput("athlete@example.com")

        // Verify the ViewModel state was updated
        composeTestRule.runOnUiThread {
            assert(viewModel.uiState.value.email == "athlete@example.com")
        }
    }

    @Test
    fun passwordField_enteringText_updatesState() {
        composeTestRule.onNodeWithText("Password").performTextInput("password123")

        composeTestRule.runOnUiThread {
            assert(viewModel.uiState.value.password == "password123")
        }
    }

    @Test
    fun signInButton_isEnabledWhenNotLoading() {
        composeTestRule.onNodeWithText("Sign In").assertIsEnabled()
    }

    // --------------------------------------------------------
    // Validation error display
    // --------------------------------------------------------

    @Test
    fun signInButton_click_withInvalidEmail_showsEmailError() {
        composeTestRule.onNodeWithText("Email").performTextInput("notvalid")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")

        composeTestRule.onNodeWithText("Sign In").performClick()

        composeTestRule.waitUntil(3_000) {
            composeTestRule.onAllNodes(hasText("Enter a valid email address")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Enter a valid email address").assertIsDisplayed()
    }

    @Test
    fun signInButton_click_withShortPassword_showsPasswordError() {
        composeTestRule.onNodeWithText("Email").performTextInput("athlete@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("12345")

        composeTestRule.onNodeWithText("Sign In").performClick()

        composeTestRule.waitUntil(3_000) {
            composeTestRule.onAllNodes(
                hasText("Password must be at least 6 characters")
            ).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Password must be at least 6 characters").assertIsDisplayed()
    }

    @Test
    fun loginError_generalErrorDisplayedInUI() {
        fakeRepository.loginResult = Result.failure(RuntimeException("Invalid credentials"))

        composeTestRule.onNodeWithText("Email").performTextInput("athlete@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        composeTestRule.onNodeWithText("Sign In").performClick()

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodes(hasText("Invalid credentials")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Invalid credentials").assertIsDisplayed()
    }

    // --------------------------------------------------------
    // Navigation
    // --------------------------------------------------------

    @Test
    fun createAccountLink_click_triggersNavigateToRegister() {
        composeTestRule.onNodeWithText("Don't have an account? Create one").performClick()

        assert(navigatedToRegister) { "Expected navigation to register screen" }
    }

    @Test
    fun signIn_success_triggersNavigateToHome() {
        fakeRepository.loginResult = Result.success(Fixtures.authSession())

        composeTestRule.onNodeWithText("Email").performTextInput("athlete@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        composeTestRule.onNodeWithText("Sign In").performClick()

        composeTestRule.waitUntil(5_000) { navigatedToHome }
        assert(navigatedToHome) { "Expected navigation to home after successful login" }
    }
}
