package io.github.easeatten.ui.nav.home

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.easeatten.data.repos.SettingsRepository
import io.github.easeatten.data.repos.UserRepository
import io.github.easeatten.ui.viewmodels.nav.HomeState
import io.github.easeatten.ui.viewmodels.nav.HomeViewModel
import io.github.easeatten.ui.viewmodels.nav.HomeViewModelFactory

@Composable
fun HomeScaffold(navController: NavController) {
  val context = LocalContext.current.applicationContext

  // Data Repositories
  val settingsRepository = remember { SettingsRepository(context) }
  val userRepository = remember { UserRepository(context) }
  // `ViewModel` Synthesis
  val vmFactory = remember { HomeViewModelFactory(settingsRepository, userRepository) }
  val vm: HomeViewModel = viewModel(factory = vmFactory)

  val state by vm.state.collectAsStateWithLifecycle()
  val settings by vm.settings.collectAsStateWithLifecycle()
  val login by vm.login.collectAsStateWithLifecycle()
  val attendance by vm.attendance.collectAsStateWithLifecycle()

  val homeNavController = rememberNavController()

  Scaffold(
      bottomBar = { BottomBar(homeNavController, vm, state.navDestination) },
  ) {
    NavHost(
        modifier = Modifier.fillMaxSize().padding(it),
        navController = homeNavController,
        startDestination = HomeState().navDestination.route(),
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { fadeOut() },
    ) {
      HomeDestination.entries.forEach { destination ->
        composable(destination.route()) {
          destination.Composable(
              navController,
              homeNavController,
              vm,
              state,
              settings,
              login,
              attendance,
          )
        }
      }
    }
  }
}

@Composable
internal fun BottomBar(
    homeNavController: NavController,
    vm: HomeViewModel,
    navDestination: HomeDestination,
) {
  NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
    HomeDestination.entries.forEach { destination ->
      NavigationBarItem(
          selected = destination == navDestination,
          onClick = {
            if (destination != navDestination) {
              vm.updateNavDestination(destination)
              homeNavController.navigate(destination.route()) { popUpTo(0) }
            }
          },
          icon = {
            Icon(
                imageVector = destination.icon(),
                contentDescription = destination.description(),
            )
          },
          label = { Text(text = destination.description()) },
      )
    }
  }
}
