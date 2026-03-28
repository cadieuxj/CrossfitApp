package com.apexai.crossfit.core.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.apexai.crossfit.feature.auth.presentation.login.LoginScreen
import com.apexai.crossfit.feature.auth.presentation.login.LoginViewModel
import com.apexai.crossfit.feature.coaching.presentation.CoachingReportScreen
import com.apexai.crossfit.feature.coaching.presentation.CoachingViewModel
import com.apexai.crossfit.feature.coaching.presentation.VideoPlaybackScreen
import com.apexai.crossfit.feature.coaching.presentation.VideoPlaybackViewModel
import com.apexai.crossfit.feature.pr.presentation.dashboard.PrDashboardScreen
import com.apexai.crossfit.feature.pr.presentation.dashboard.PrDashboardViewModel
import com.apexai.crossfit.feature.pr.presentation.detail.PrDetailScreen
import com.apexai.crossfit.feature.pr.presentation.detail.PrDetailViewModel
import com.apexai.crossfit.feature.readiness.presentation.ReadinessDashboardScreen
import com.apexai.crossfit.feature.readiness.presentation.ReadinessViewModel
import com.apexai.crossfit.feature.readiness.presentation.WellnessCheckInScreen
import com.apexai.crossfit.feature.auth.presentation.splash.SplashScreen
import com.apexai.crossfit.feature.auth.presentation.splash.SplashViewModel
import com.apexai.crossfit.feature.coach.presentation.CoachDashboardScreen
import com.apexai.crossfit.feature.coach.presentation.CoachDashboardViewModel
import com.apexai.crossfit.feature.coach.presentation.CoachLinkScreen
import com.apexai.crossfit.feature.coach.presentation.CoachLinkViewModel
import com.apexai.crossfit.feature.competition.presentation.CompetitionDetailScreen
import com.apexai.crossfit.feature.competition.presentation.CompetitionDetailViewModel
import com.apexai.crossfit.feature.competition.presentation.CompetitionHubScreen
import com.apexai.crossfit.feature.competition.presentation.CompetitionHubViewModel
import com.apexai.crossfit.feature.home.presentation.HomeScreen
import com.apexai.crossfit.feature.home.presentation.HomeViewModel
import com.apexai.crossfit.feature.nutrition.presentation.MacroLogScreen
import com.apexai.crossfit.feature.nutrition.presentation.MacroLogViewModel
import com.apexai.crossfit.feature.profile.presentation.ProfileScreen
import com.apexai.crossfit.feature.profile.presentation.ProfileViewModel
import com.apexai.crossfit.feature.vision.presentation.camera.LiveCameraScreen
import com.apexai.crossfit.feature.vision.presentation.camera.VisionViewModel
import com.apexai.crossfit.feature.vision.presentation.review.RecordingReviewScreen
import com.apexai.crossfit.feature.wod.presentation.detail.WodDetailScreen
import com.apexai.crossfit.feature.wod.presentation.detail.WodDetailViewModel
import com.apexai.crossfit.feature.wod.presentation.log.WodLogScreen
import com.apexai.crossfit.feature.wod.presentation.log.WodLogViewModel
import com.apexai.crossfit.feature.wod.presentation.timer.WodTimerScreen
import com.apexai.crossfit.feature.wod.presentation.timer.WodTimerViewModel

private val SLIDE_DURATION = 300

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    pendingRoute: String? = null,
    onPendingRouteConsumed: () -> Unit = {}
) {
    val navController = rememberNavController()

    // Navigate to a route delivered from a notification tap or external intent.
    // Fires once per non-null pendingRoute, then clears it to prevent re-navigation.
    LaunchedEffect(pendingRoute) {
        if (pendingRoute != null) {
            navController.navigate(pendingRoute) {
                launchSingleTop = true
            }
            onPendingRouteConsumed()
        }
    }

    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(SLIDE_DURATION)
            ) + fadeIn(tween(SLIDE_DURATION))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it / 3 },
                animationSpec = tween(SLIDE_DURATION)
            ) + fadeOut(tween(SLIDE_DURATION))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 3 },
                animationSpec = tween(SLIDE_DURATION)
            ) + fadeIn(tween(SLIDE_DURATION))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(SLIDE_DURATION)
            ) + fadeOut(tween(SLIDE_DURATION))
        }
    ) {
        // ---------------------------------------------------------------
        // Splash
        // ---------------------------------------------------------------
        composable(NavRoutes.SPLASH) {
            val vm: SplashViewModel = hiltViewModel()
            SplashScreen(
                onNavigateToHome  = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(NavRoutes.AUTH_LOGIN) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                },
                viewModel = vm
            )
        }

        // ---------------------------------------------------------------
        // Auth
        // ---------------------------------------------------------------
        composable(NavRoutes.AUTH_LOGIN) {
            val vm: LoginViewModel = hiltViewModel()
            LoginScreen(
                viewModel = vm,
                onNavigateToHome     = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.AUTH_LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(NavRoutes.AUTH_REGISTER)
                }
            )
        }

        composable(NavRoutes.AUTH_REGISTER) {
            val vm: LoginViewModel = hiltViewModel()
            // RegisterScreen uses same VM wiring with different UiEvent
            com.apexai.crossfit.feature.auth.presentation.register.RegisterScreen(
                viewModel = vm,
                onNavigateToHome = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.AUTH_LOGIN) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ---------------------------------------------------------------
        // Home (bottom nav root)
        // ---------------------------------------------------------------
        composable(NavRoutes.HOME) {
            val vm: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel        = vm,
                onWodClick       = { wodId ->
                    if (wodId.isNotBlank()) navController.navigate(NavRoutes.wodDetail(wodId))
                    else navController.navigate(NavRoutes.WOD_BROWSE)
                },
                onReadinessClick = { navController.navigate(NavRoutes.READINESS) },
                onPrClick        = { navController.navigate(NavRoutes.PR_DASHBOARD) },
                onCameraClick    = { navController.navigate(NavRoutes.VISION_LIVE) },
                onNutritionClick = { navController.navigate(NavRoutes.NUTRITION_LOG) }
            )
        }

        // ---------------------------------------------------------------
        // WOD browse
        // ---------------------------------------------------------------
        composable(NavRoutes.WOD_BROWSE) {
            val vm: com.apexai.crossfit.feature.wod.presentation.browse.WodBrowseViewModel = hiltViewModel()
            com.apexai.crossfit.feature.wod.presentation.browse.WodBrowseScreen(
                viewModel = vm,
                currentNavRoute = NavRoutes.WOD_BROWSE,
                onNavigateToDetail = { wodId -> navController.navigate(NavRoutes.wodDetail(wodId)) },
                onBottomNavNavigate = { route -> navController.navigate(route) {
                    popUpTo(NavRoutes.HOME) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }}
            )
        }

        // ---------------------------------------------------------------
        // WOD Detail
        // ---------------------------------------------------------------
        composable(
            route = NavRoutes.WOD_DETAIL_PATTERN,
            arguments = listOf(navArgument("wodId") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = NavRoutes.DEEP_LINK_WOD_DETAIL })
        ) { backStackEntry ->
            val vm: WodDetailViewModel = hiltViewModel()
            WodDetailScreen(
                viewModel       = vm,
                onNavigateBack  = { navController.popBackStack() },
                onNavigateToTimer = { wodId -> navController.navigate(NavRoutes.wodTimer(wodId)) },
                onNavigateToLog   = { wodId -> navController.navigate(NavRoutes.wodLog(wodId)) },
                onNavigateToCamera = { navController.navigate(NavRoutes.VISION_LIVE) }
            )
        }

        // ---------------------------------------------------------------
        // WOD Timer
        // ---------------------------------------------------------------
        composable(
            route = NavRoutes.WOD_TIMER_PATTERN,
            arguments = listOf(navArgument("wodId") { type = NavType.StringType })
        ) { backStackEntry ->
            val vm: WodTimerViewModel = hiltViewModel()
            WodTimerScreen(
                viewModel         = vm,
                onNavigateToLog   = { wodId ->
                    navController.navigate(NavRoutes.wodLog(wodId)) {
                        popUpTo(NavRoutes.WOD_TIMER_PATTERN) { inclusive = true }
                    }
                },
                onNavigateBack    = { navController.popBackStack() }
            )
        }

        // ---------------------------------------------------------------
        // WOD Log
        // ---------------------------------------------------------------
        composable(
            route = NavRoutes.WOD_LOG_PATTERN,
            arguments = listOf(navArgument("wodId") { type = NavType.StringType })
        ) {
            val vm: WodLogViewModel = hiltViewModel()
            WodLogScreen(
                viewModel       = vm,
                onNavigateBack  = { navController.popBackStack() },
                onNavigateHome  = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.HOME) { inclusive = false }
                    }
                }
            )
        }

        // ---------------------------------------------------------------
        // PR Dashboard
        // ---------------------------------------------------------------
        composable(NavRoutes.PR_DASHBOARD) {
            val vm: PrDashboardViewModel = hiltViewModel()
            PrDashboardScreen(
                viewModel = vm,
                currentNavRoute = NavRoutes.PR_DASHBOARD,
                onNavigateToPrDetail = { movementId -> navController.navigate(NavRoutes.prDetail(movementId)) },
                onNavigateToWod = { navController.navigate(NavRoutes.WOD_BROWSE) },
                onBottomNavNavigate = { route -> navController.navigate(route) {
                    popUpTo(NavRoutes.HOME) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }}
            )
        }

        // ---------------------------------------------------------------
        // PR Detail
        // ---------------------------------------------------------------
        composable(
            route = NavRoutes.PR_DETAIL_PATTERN,
            arguments = listOf(navArgument("movementId") { type = NavType.StringType })
        ) {
            val vm: PrDetailViewModel = hiltViewModel()
            PrDetailScreen(
                viewModel      = vm,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ---------------------------------------------------------------
        // Readiness
        // ---------------------------------------------------------------
        composable(NavRoutes.READINESS) {
            val vm: ReadinessViewModel = hiltViewModel()
            ReadinessDashboardScreen(
                viewModel = vm,
                currentNavRoute = NavRoutes.READINESS,
                onNavigateToSetup = { navController.navigate(NavRoutes.READINESS_SETUP) },
                onNavigateToWellnessCheckIn = { navController.navigate(NavRoutes.WELLNESS_CHECK_IN) },
                onBottomNavNavigate = { route -> navController.navigate(route) {
                    popUpTo(NavRoutes.HOME) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }}
            )
        }

        composable(NavRoutes.WELLNESS_CHECK_IN) {
            val vm: com.apexai.crossfit.feature.readiness.presentation.WellnessCheckInViewModel = hiltViewModel()
            WellnessCheckInScreen(
                viewModel      = vm,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ---------------------------------------------------------------
        // Vision (full-screen, no bottom nav)
        // ---------------------------------------------------------------
        composable(
            route = NavRoutes.VISION_LIVE,
            enterTransition = { fadeIn(tween(200)) },
            exitTransition  = { fadeOut(tween(200)) }
        ) {
            val vm: VisionViewModel = hiltViewModel()
            LiveCameraScreen(
                viewModel = vm,
                onNavigateBack   = { navController.popBackStack() },
                onNavigateToReview = { videoUri ->
                    navController.navigate(NavRoutes.visionReview(videoUri))
                }
            )
        }

        composable(
            route = NavRoutes.VISION_REVIEW_PATTERN,
            arguments = listOf(navArgument("videoUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val rawUri = backStackEntry.arguments?.getString("videoUri")?.let {
                java.net.URLDecoder.decode(it, "UTF-8")
            } ?: ""
            // Validate URI is a local media content URI — reject any external or file:// URIs
            // to prevent deep-link injection attacks (C-05)
            val videoUri = if (rawUri.startsWith("content://media/") ||
                rawUri.startsWith("content://com.android.providers.media/")) {
                rawUri
            } else {
                ""
            }
            RecordingReviewScreen(
                videoUri         = videoUri,
                onNavigateBack   = { navController.popBackStack() },
                onNavigateToReport = { analysisId ->
                    navController.navigate(NavRoutes.coachingReport(analysisId)) {
                        popUpTo(NavRoutes.VISION_LIVE) { inclusive = false }
                    }
                }
            )
        }

        // ---------------------------------------------------------------
        // Coaching (full-screen)
        // ---------------------------------------------------------------
        composable(
            route = NavRoutes.COACHING_REPORT_PATTERN,
            arguments = listOf(navArgument("analysisId") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = NavRoutes.DEEP_LINK_COACHING_REPORT })
        ) {
            val vm: CoachingViewModel = hiltViewModel()
            CoachingReportScreen(
                viewModel = vm,
                onNavigateHome = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.HOME) { inclusive = false }
                    }
                },
                onNavigateToPlayback = { videoId, timestampMs ->
                    navController.navigate(NavRoutes.coachingPlayback(videoId, timestampMs))
                }
            )
        }

        composable(
            route = NavRoutes.COACHING_PLAYBACK_PATTERN,
            arguments = listOf(
                navArgument("videoId")   { type = NavType.StringType },
                navArgument("timestamp") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) {
            val vm: VideoPlaybackViewModel = hiltViewModel()
            VideoPlaybackScreen(
                viewModel      = vm,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ---------------------------------------------------------------
        // Profile
        // ---------------------------------------------------------------
        composable(NavRoutes.PROFILE) {
            val vm: ProfileViewModel = hiltViewModel()
            ProfileScreen(
                viewModel = vm,
                onLoggedOut = {
                    navController.navigate(NavRoutes.AUTH_LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToCoachLink      = { navController.navigate(NavRoutes.COACH_LINK) },
                onNavigateToCoachDashboard = { navController.navigate(NavRoutes.COACH_DASHBOARD) },
                onNavigateToNutrition      = { navController.navigate(NavRoutes.NUTRITION_LOG) }
            )
        }

        // ---------------------------------------------------------------
        // Competition Hub
        // ---------------------------------------------------------------
        composable(NavRoutes.COMPETITION) {
            val vm: CompetitionHubViewModel = hiltViewModel()
            CompetitionHubScreen(
                viewModel = vm,
                currentNavRoute = NavRoutes.COMPETITION,
                onNavigateToDetail = { eventId -> navController.navigate(NavRoutes.competitionDetail(eventId)) },
                onBottomNavNavigate = { route -> navController.navigate(route) {
                    popUpTo(NavRoutes.HOME) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }},
                onCameraFabClick = { navController.navigate(NavRoutes.VISION_LIVE) }
            )
        }

        composable(
            route = NavRoutes.COMPETITION_DETAIL_PATTERN,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) {
            val vm: CompetitionDetailViewModel = hiltViewModel()
            CompetitionDetailScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ---------------------------------------------------------------
        // Nutrition
        // ---------------------------------------------------------------
        composable(NavRoutes.NUTRITION_LOG) {
            val vm: MacroLogViewModel = hiltViewModel()
            MacroLogScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ---------------------------------------------------------------
        // Coach
        // ---------------------------------------------------------------
        composable(NavRoutes.COACH_LINK) {
            val vm: CoachLinkViewModel = hiltViewModel()
            CoachLinkScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.COACH_DASHBOARD) {
            val vm: CoachDashboardViewModel = hiltViewModel()
            CoachDashboardScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
