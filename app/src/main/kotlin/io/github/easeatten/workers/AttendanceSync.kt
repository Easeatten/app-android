package io.github.easeatten.workers

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import io.github.easeatten.data.repos.UserRepository
import io.github.easeatten.data.sources.SettingsData
import io.github.easeatten.data.sources.SettingsDataStore
import java.io.IOException
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AttendanceSync(val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {
    companion object Work {
        const val LOGGER = "WORKER"
        const val WORK_TAG = "AttendanceSync"
        private const val HOURS = 24L

        fun request(): OneTimeWorkRequest {
            val constraints =
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

            val now = LocalTime.now()
            val nextRefresh = LocalTime.of(23, 59, 59)

            // Calculating the delay here which will be added to the next work request.
            // This is done because OneTimeWorkRequest would attempt to run immediately otherwise.
            val timeDiff = now.until(nextRefresh, ChronoUnit.SECONDS)

            // OneTimeWorkRequest has been used instead of PeriodicWorkRequest
            // because of the compounding delay cause by the latter.
            // In case of the former we are using a calculated delay.
            return OneTimeWorkRequestBuilder<AttendanceSync>()
                .setInitialDelay(timeDiff, TimeUnit.SECONDS)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.DEFAULT_BACKOFF_DELAY_MILLIS,
                    TimeUnit.MINUTES,
                )
                .addTag(WORK_TAG)
                .build()
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun doWork(): Result {
        var settings = SettingsData()
        CoroutineScope(Dispatchers.IO).launch {
            settings = applicationContext.SettingsDataStore.data.first()
        }

        val result =
            try {
                Log.i(LOGGER, "Refreshing attendance")
                // This is the main work that is to be done.
                UserRepository(appContext).refreshAttendanceData()

                Result.success()
            } catch (e: IOException) {
                Log.e(LOGGER, "Retry", e)
                Result.retry()
            } catch (e: Exception) {
                Log.e(LOGGER, "Failure", e)
                Result.failure()
            }

        WorkManager.getInstance(applicationContext)
            // Remove WorkInfo for completed work. This keeps the history clean.
            .pruneWork()

        // For success or failure enqueue next work.
        if (result != Result.retry() && settings.autoRefreshAttendance)
            WorkManager.getInstance(applicationContext)
                .enqueueUniqueWork(WORK_TAG, ExistingWorkPolicy.APPEND, request())

        return result
    }
}
