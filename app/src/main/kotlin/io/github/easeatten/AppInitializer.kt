package io.github.easeatten

import android.app.Application
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import io.github.easeatten.data.repos.UserRepository
import io.github.easeatten.workers.AttendanceSync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppInitializer : Application() {
    override fun onCreate() {
        super.onCreate()

        CoroutineScope(Dispatchers.IO).launch {
            UserRepository(applicationContext).refreshAttendanceData()
        }

        worker()
    }

    private fun worker() {
        val workRequest = AttendanceSync.Work.request(applicationContext)

        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork("AttendanceSync", ExistingWorkPolicy.KEEP, workRequest)

        // XXX: the following code is just for observing state and must be removed later
        WorkManager.getInstance(applicationContext)
            .getWorkInfosForUniqueWorkLiveData("AttendanceSync")
            .observeForever { workInfos ->
                workInfos.forEach { Log.d("AttendanceSync", it.state.name) }
            }
    }
}
