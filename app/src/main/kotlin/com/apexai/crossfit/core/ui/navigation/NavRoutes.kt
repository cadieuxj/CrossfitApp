package com.apexai.crossfit.core.ui.navigation

object NavRoutes {
    const val SPLASH       = "splash"

    // Auth
    const val AUTH_LOGIN    = "auth/login"
    const val AUTH_REGISTER = "auth/register"

    // Main tabs
    const val HOME           = "home"
    const val WOD_BROWSE     = "wod"
    const val WOD_HISTORY    = "wod/history"
    const val PR_DASHBOARD   = "pr"
    const val READINESS             = "readiness"
    const val READINESS_SETUP       = "readiness/setup"
    const val WELLNESS_CHECK_IN     = "readiness/wellness_check_in"
    const val PROFILE        = "profile"

    // WOD detail flow
    fun wodDetail(wodId: String)  = "wod/$wodId"
    fun wodLog(wodId: String)     = "wod/$wodId/log"
    fun wodTimer(wodId: String)   = "wod/$wodId/timer"
    fun prDetail(movementId: String) = "pr/$movementId"

    // Vision flow
    const val VISION_LIVE = "vision/live"
    fun visionReview(videoUri: String) = "vision/review/${encode(videoUri)}"

    // Coaching flow
    fun coachingReport(analysisId: String)                        = "coaching/report/$analysisId"
    fun coachingPlayback(videoId: String, startMs: Long = 0L)    = "coaching/playback/$videoId?timestamp=$startMs"

    // Deep link patterns for NavDeepLink
    const val DEEP_LINK_WOD_DETAIL     = "apexai://wod/{wodId}"
    const val DEEP_LINK_COACHING_REPORT = "apexai://coaching/report/{analysisId}"

    // Route patterns with arguments (for navArgument declarations)
    const val WOD_DETAIL_PATTERN       = "wod/{wodId}"
    const val WOD_LOG_PATTERN          = "wod/{wodId}/log"
    const val WOD_TIMER_PATTERN        = "wod/{wodId}/timer"
    const val PR_DETAIL_PATTERN        = "pr/{movementId}"
    const val VISION_REVIEW_PATTERN    = "vision/review/{videoUri}"
    const val COACHING_REPORT_PATTERN  = "coaching/report/{analysisId}"
    const val COACHING_PLAYBACK_PATTERN = "coaching/playback/{videoId}?timestamp={timestamp}"

    private fun encode(value: String): String =
        java.net.URLEncoder.encode(value, "UTF-8")
}
