package io.github.easeatten.ui.nav

enum class NavDestination {
    ONBOARDING,
    ONBOARDING_LANDING,
    ONBOARDING_LICENSE,
    LOGIN,
    LOGIN_DETAILS,
    NOTIFICATION_PERMISSION,
    HOME,
    ATTENDANCE;

    fun route(): String =
        when (this) {
            ONBOARDING -> "/onboarding"
            ONBOARDING_LANDING -> "/onboarding/landing"
            ONBOARDING_LICENSE -> "/onboarding/license"
            LOGIN -> "/login"
            LOGIN_DETAILS -> "/login/details"
            NOTIFICATION_PERMISSION -> "/notification_permission"
            HOME -> "/home"
            ATTENDANCE -> "/attendance"
        }
}
