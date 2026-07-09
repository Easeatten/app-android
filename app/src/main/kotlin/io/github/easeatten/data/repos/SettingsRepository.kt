package io.github.easeatten.data.repos

import android.content.Context
import io.github.easeatten.data.sources.SettingsDataStore
import io.github.easeatten.ui.theme.colorscheme.ColorScheme
import io.github.easeatten.ui.theme.colorscheme.isDynamicColorSupported
import io.github.easeatten.ui.theme.typography.Typography

class SettingsRepository(
    private val context: Context,
) {
  val settingsFlow = context.SettingsDataStore.data

  suspend fun setOnboardingDone() {
    context.SettingsDataStore.updateData { it.copy(onboardingDone = true) }
  }

  suspend fun updateThemeColorScheme(value: ColorScheme) {
    context.SettingsDataStore.updateData { it.copy(themeColorScheme = value) }
  }

  suspend fun updateThemeDarkMode(value: Boolean?) {
    context.SettingsDataStore.updateData { it.copy(themeDarkMode = value) }
  }

  suspend fun updateThemeDynamicColor(value: Boolean) {
    context.SettingsDataStore.updateData {
      it.copy(themeDynamicColor = isDynamicColorSupported && value)
    }
  }

  suspend fun updateThemeTypography(value: Typography) {
    context.SettingsDataStore.updateData { it.copy(themeTypography = value) }
  }
}
