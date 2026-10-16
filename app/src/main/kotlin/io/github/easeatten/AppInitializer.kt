package io.github.easeatten

import android.app.Application
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import io.github.easeatten.data.sources.SettingsData
import io.github.easeatten.data.sources.SettingsDataStore
import io.github.easeatten.workers.AttendanceSync
import java.time.LocalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppInitializer : Application() {
    override fun onCreate() {
        super.onCreate()

        var settings = SettingsData()

        CoroutineScope(Dispatchers.IO).launch {
            settings = applicationContext.SettingsDataStore.data.first()
        }

        // If auto refresh is enabled.
        if (settings.autoRefreshAttendance) {
            val workReq = AttendanceSync.Work.request()
            WorkManager.getInstance(applicationContext)
                .enqueueUniqueWork(AttendanceSync.WORK_TAG, ExistingWorkPolicy.KEEP, workReq)

            // XXX: the following code is just for observing work state and may be removed later.
            WorkManager.getInstance(applicationContext)
                .getWorkInfosForUniqueWorkLiveData(AttendanceSync.WORK_TAG)
                .observeForever { workInfos ->
                    workInfos.forEach {
                        Log.d(AttendanceSync.LOGGER, "${it.state.name} : ${LocalTime.now()}")
                    }
                }
        } else
            WorkManager.getInstance(applicationContext).cancelAllWorkByTag(AttendanceSync.WORK_TAG)
    }
}
