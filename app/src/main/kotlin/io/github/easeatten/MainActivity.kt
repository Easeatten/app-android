package io.github.easeatten

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import io.github.easeatten.ui.nav.NavManager
import io.github.easeatten.ui.theme.ThemeManager
import io.github.easeatten.ui.viewmodels.SplashScreenViewModel

class MainActivity : ComponentActivity() {
  private val vm: SplashScreenViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val splashScreen = installSplashScreen()
    splashScreen.setKeepOnScreenCondition { !vm.loadingDone.value }

    enableEdgeToEdge()
    setContent { ThemeManager { NavManager() } }
  }
}
