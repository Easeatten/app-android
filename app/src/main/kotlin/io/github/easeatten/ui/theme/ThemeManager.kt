package io.github.easeatten.ui.theme

import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.glance.GlanceComposable
import androidx.glance.GlanceTheme
import androidx.glance.material3.ColorProviders
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.easeatten.data.repos.SettingsRepository
import io.github.easeatten.data.repos.UserRepository
import io.github.easeatten.data.sources.SettingsData
import io.github.easeatten.ui.theme.colorscheme.isDynamicColorSupported
import io.github.easeatten.ui.viewmodels.SimpleViewModel
import io.github.easeatten.ui.viewmodels.SimpleViewModelFactory

@Composable
fun ThemeManager(content: @Composable () -> Unit) {
    val context = LocalContext.current.applicationContext
    val window = LocalActivity.current!!.window

    // Data Repositories
    val settingsRepository = remember { SettingsRepository(context) }
    val userRepository = remember { UserRepository(context) }
    // `ViewModel` Synthesis
    val vmFactory = remember { SimpleViewModelFactory(settingsRepository, userRepository) }
    val vm: SimpleViewModel = viewModel(factory = vmFactory)

    val settings by vm.settings.collectAsStateWithLifecycle()

    val darkMode = settings.themeDarkMode ?: isSystemInDarkTheme()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        window.isNavigationBarContrastEnforced = false
    }

    val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
    windowInsetsController.isAppearanceLightStatusBars = !darkMode
    windowInsetsController.isAppearanceLightNavigationBars = !darkMode

    MaterialTheme(
        colorScheme =
            when {
                isDynamicColorSupported && settings.themeDynamicColor -> {
                    val ctx = LocalContext.current
                    if (darkMode) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
                }

                else -> {
                    settings.themeColorScheme.get().getColorScheme(darkMode)
                }
            },
        typography = settings.themeTypography.get().typography,
        content = content,
    )
}

@GlanceComposable
@Composable
fun GlanceThemeManager(settings: SettingsData, content: @GlanceComposable @Composable () -> Unit) {
    GlanceTheme(
        colors =
            when {
                isDynamicColorSupported -> GlanceTheme.colors
                else ->
                    ColorProviders(
                        light = settings.themeColorScheme.get().getColorScheme(false),
                        dark = settings.themeColorScheme.get().getColorScheme(true),
                    )
            },
        content = content,
    )
}
