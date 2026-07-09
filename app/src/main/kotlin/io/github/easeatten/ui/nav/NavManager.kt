package io.github.easeatten.ui.nav

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import io.github.easeatten.data.repos.SettingsRepository
import io.github.easeatten.data.repos.UserRepository
import io.github.easeatten.ui.nav.home.HomeScaffold
import io.github.easeatten.ui.nav.login.DetailsPage
import io.github.easeatten.ui.nav.onboarding.LandingPage
import io.github.easeatten.ui.nav.onboarding.LicensePage
import io.github.easeatten.ui.viewmodels.SimpleViewModel
import io.github.easeatten.ui.viewmodels.SimpleViewModelFactory
import io.github.easeatten.ui.viewmodels.SplashScreenViewModel

@Composable
fun NavManager() {
  val context = LocalContext.current.applicationContext

  // Data Repositories
  val settingsRepository = remember { SettingsRepository(context) }
  val userRepository = remember { UserRepository(context) }
  // `ViewModel` Synthesis
  val vmFactory = remember { SimpleViewModelFactory(settingsRepository, userRepository) }
  val vm: SimpleViewModel = viewModel(factory = vmFactory)
  val vmSplash: SplashScreenViewModel = viewModel()

  val settings by vm.settings.collectAsStateWithLifecycle()
  val login by vm.login.collectAsStateWithLifecycle()
  val loadingDone by vmSplash.loadingDone.collectAsStateWithLifecycle()

  // Keep the splash on screen until the data store objects are ready.
  if (!loadingDone) {
    if (settings.valid && login.valid) vmSplash.setLoadingDone() else return
  }

  val navController = rememberNavController()
  val startDestination = remember {
    if (!settings.onboardingDone) {
      NavDestination.ONBOARDING.route()
    } else if (!login.loggedIn) {
      NavDestination.LOGIN.route()
    } else {
      NavDestination.HOME.route()
    }
  }

  Surface(modifier = Modifier.fillMaxSize()) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn() + slideInHorizontally { it } },
        exitTransition = { fadeOut() + slideOutHorizontally { -it } },
        popEnterTransition = { fadeIn() + slideInHorizontally { -it } },
        popExitTransition = { fadeOut() + slideOutHorizontally { it } },
    ) {
      navigation(
          route = NavDestination.ONBOARDING.route(),
          startDestination = NavDestination.ONBOARDING_LANDING.route(),
      ) {
        composable(NavDestination.ONBOARDING_LANDING.route()) { LandingPage(navController) }
        composable(NavDestination.ONBOARDING_LICENSE.route()) { LicensePage(navController) }
      }
      navigation(
          route = NavDestination.LOGIN.route(),
          startDestination = NavDestination.LOGIN_DETAILS.route(),
      ) {
        composable(NavDestination.LOGIN_DETAILS.route()) { DetailsPage(navController) }
      }
      composable(NavDestination.HOME.route()) { HomeScaffold(navController) }
    }
  }
}
