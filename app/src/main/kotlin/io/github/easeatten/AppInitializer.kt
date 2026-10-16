package io.github.easeatten

import android.app.Application
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import io.github.easeatten.workers.AttendanceSync
import java.time.LocalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppInitializer : Application() {
    override fun onCreate() {
        super.onCreate()

        CoroutineScope(Dispatchers.IO).launch {
            val workReq = AttendanceSync.Work.request(applicationContext)
            WorkManager.getInstance(applicationContext)
                .enqueueUniqueWork("AttendanceSyncWork", ExistingWorkPolicy.KEEP, workReq)
        }

        // XXX: the following code is just for observing work state and may be removed later.
        WorkManager.getInstance(applicationContext)
            .getWorkInfosForUniqueWorkLiveData("AttendanceSyncWork")
            .observeForever { workInfos ->
                workInfos.forEach {
                    Log.d(AttendanceSync.LOGGER, "${it.state.name} : ${LocalTime.now()}")
                }
            }
    }
}
