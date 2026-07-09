package io.github.easeatten.ui.nav.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import io.github.easeatten.data.sources.AttendanceData
import io.github.easeatten.data.sources.LoginData
import io.github.easeatten.data.sources.SettingsData
import io.github.easeatten.ui.icons.iconHome
import io.github.easeatten.ui.icons.iconSettings
import io.github.easeatten.ui.viewmodels.nav.HomeState
import io.github.easeatten.ui.viewmodels.nav.HomeViewModel

enum class HomeDestination {
  DASHBOARD,
  SETTINGS,
  ;

  fun route(): String =
      when (this) {
        DASHBOARD -> "/home/dashboard"
        SETTINGS -> "/home/settings"
      }

  fun description(): String =
      when (this) {
        DASHBOARD -> "Dashboard"
        SETTINGS -> "Settings"
      }

  fun icon(): ImageVector =
      when (this) {
        DASHBOARD -> iconHome
        SETTINGS -> iconSettings
      }

  @Composable
  fun Composable(
      navController: NavController,
      homeNavController: NavController,
      vm: HomeViewModel,
      state: HomeState,
      settings: SettingsData,
      login: LoginData,
      attendance: AttendanceData,
  ) {
    when (this) {
      DASHBOARD -> {
        HomePageDashboard(
            navController,
            homeNavController,
            vm,
            state,
            settings,
            login,
            attendance,
        )
      }

      SETTINGS -> {
        HomePageSettings(navController, homeNavController, vm, state, settings, login, attendance)
      }
    }
  }
}
