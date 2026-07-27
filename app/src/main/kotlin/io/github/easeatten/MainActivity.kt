package io.github.easeatten

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.lifecycleScope
import io.github.easeatten.ui.nav.NavManager
import io.github.easeatten.ui.theme.ThemeManager
import io.github.easeatten.ui.viewmodels.SplashScreenViewModel
import io.github.easeatten.ui.widgets.AttendanceGlance
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  private val vm: SplashScreenViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val splashScreen = installSplashScreen()
    splashScreen.setKeepOnScreenCondition { !vm.loadingDone.value }

    lifecycleScope.launch { AttendanceGlance().updateAll(this@MainActivity) }

    enableEdgeToEdge()
    setContent { ThemeManager { NavManager() } }
  }
}
