package io.github.easeatten.ui.viewmodels.nav

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.easeatten.data.repos.SettingsRepository
import io.github.easeatten.data.sources.SettingsData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingState(
    // User is in agreement with the license, and has clicked on the "I Agree" checkbox.
    val agreedToLicense: Boolean = false,
)

class OnboardingViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
  private val statePrivate = MutableStateFlow(OnboardingState())
  val state = statePrivate.asStateFlow()

  val settings =
      settingsRepository.settingsFlow.stateIn(
          scope = viewModelScope,
          started = SharingStarted.Eagerly,
          initialValue = SettingsData(),
      )

  fun updateAgreedToLicense(value: Boolean) {
    statePrivate.update { it.copy(agreedToLicense = value) }
  }

  fun setOnboardingDone() {
    viewModelScope.launch { settingsRepository.setOnboardingDone() }
  }
}

class OnboardingViewModelFactory(
    private val settingsRepository: SettingsRepository,
) : ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T =
      when {
        modelClass.isAssignableFrom(OnboardingViewModel::class.java) -> {
          OnboardingViewModel(settingsRepository) as T
        }

        else -> {
          throw IllegalArgumentException("Unknown ViewModel class $modelClass")
        }
      }
}
