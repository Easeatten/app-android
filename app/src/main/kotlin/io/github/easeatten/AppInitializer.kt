package io.github.easeatten

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import io.github.easeatten.data.repos.UserRepository
import io.github.easeatten.notifiers.NotificationChannels
import io.github.easeatten.workers.AttendanceSync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppInitializer : Application() {
    companion object {
        lateinit var notificationManager: NotificationManager
    }

    override fun onCreate() {
        super.onCreate()
        // Get access to Android's notification manager.
        // Android manages the actual notification service and we are retrieving that.
        notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Create channels for notifications
        NotificationChannels.createAll(applicationContext)

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
