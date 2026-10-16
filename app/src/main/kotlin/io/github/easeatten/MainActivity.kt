package io.github.easeatten

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkManager
import io.github.easeatten.ui.nav.NavManager
import io.github.easeatten.ui.theme.ThemeManager
import io.github.easeatten.ui.viewmodels.SplashScreenViewModel
import io.github.easeatten.ui.widgets.AttendanceGlance
import io.github.easeatten.workers.AttendanceSync
import java.time.LocalTime
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

        // XXX: the following code is just for observing work state and may be removed later.
        WorkManager.getInstance(applicationContext)
            .getWorkInfosForUniqueWorkLiveData(AttendanceSync.WORK_TAG)
            .observeForever { workInfos ->
                workInfos.forEach {
                    Log.d(AttendanceSync.LOGGER, "${it.state.name} : ${LocalTime.now()}")
                }
            }
    }
}
