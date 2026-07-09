package io.github.easeatten.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import io.github.easeatten.ui.nav.home.HomeScaffold
import io.github.easeatten.ui.nav.login.DetailsPage
import io.github.easeatten.ui.nav.onboarding.LandingPage
import io.github.easeatten.ui.nav.onboarding.LicensePage

enum class NavDestination {
  ONBOARDING,
  ONBOARDING_LANDING,
  ONBOARDING_LICENSE,
  LOGIN,
  LOGIN_DETAILS,
  HOME,
  ;

  fun route(): String =
      when (this) {
        ONBOARDING -> "/onboarding"
        ONBOARDING_LANDING -> "/onboarding/landing"
        ONBOARDING_LICENSE -> "/onboarding/license"
        LOGIN -> "/login"
        LOGIN_DETAILS -> "/login/details"
        HOME -> "/home"
      }

  @Composable
  fun Composable(navController: NavController) {
    when (this) {
      ONBOARDING_LICENSE -> LicensePage(navController)
      ONBOARDING_LANDING -> LandingPage(navController)
      LOGIN_DETAILS -> DetailsPage(navController)
      HOME -> HomeScaffold(navController)
      else -> assert(true) { "No composable for route ${this.route()}" }
    }
  }
}
