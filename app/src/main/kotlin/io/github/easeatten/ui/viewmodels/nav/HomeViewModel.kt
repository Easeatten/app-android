package io.github.easeatten.ui.viewmodels.nav

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.easeatten.data.repos.SettingsRepository
import io.github.easeatten.data.repos.UserRepository
import io.github.easeatten.data.sources.AttendanceData
import io.github.easeatten.data.sources.LoginData
import io.github.easeatten.data.sources.SettingsData
import io.github.easeatten.ui.nav.home.HomeDestination
import io.github.easeatten.ui.theme.colorscheme.ColorScheme
import io.github.easeatten.ui.theme.typography.Typography
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeState(
    val navDestination: HomeDestination = HomeDestination.DASHBOARD,
)

class HomeViewModel(
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository,
) : ViewModel() {
  private val statePrivate = MutableStateFlow(HomeState())
  val state = statePrivate.asStateFlow()

  val settings =
      settingsRepository.settingsFlow.stateIn(
          scope = viewModelScope,
          started = SharingStarted.Eagerly,
          initialValue = SettingsData(),
      )

  val login =
      userRepository.loginFlow.stateIn(
          scope = viewModelScope,
          started = SharingStarted.Eagerly,
          initialValue = LoginData(),
      )

  val attendance =
      userRepository.attendanceFlow.stateIn(
          scope = viewModelScope,
          started = SharingStarted.WhileSubscribed(),
          initialValue = AttendanceData(),
      )

  fun updateNavDestination(destination: HomeDestination) {
    statePrivate.update { it.copy(navDestination = destination) }
  }

  fun updateColorScheme(colorScheme: ColorScheme) {
    viewModelScope.launch { settingsRepository.updateThemeColorScheme(colorScheme) }
  }

  fun updateDarkMode(choice: Boolean?) {
    viewModelScope.launch { settingsRepository.updateThemeDarkMode(choice) }
  }

  fun updateDynamicColor(enable: Boolean) {
    viewModelScope.launch { settingsRepository.updateThemeDynamicColor(enable) }
  }

  fun updateTypography(typography: Typography) {
    viewModelScope.launch { settingsRepository.updateThemeTypography(typography) }
  }

  fun logout() {
    viewModelScope.launch { userRepository.unregisterUser() }
  }
}

class HomeViewModelFactory(
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository,
) : ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T =
      when {
        modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
          HomeViewModel(settingsRepository, userRepository) as T
        }

        else -> {
          throw IllegalArgumentException("Unknown ViewModel class $modelClass")
        }
      }
}
