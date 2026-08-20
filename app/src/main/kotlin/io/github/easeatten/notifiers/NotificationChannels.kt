package io.github.easeatten.notifiers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import io.github.easeatten.notifiers.content.AppUpdateNotifier
import io.github.easeatten.notifiers.content.RefreshNotifier

// Create the NotificationChannel, but only on API 26+ because
// the NotificationChannel class is not in the Support Library.
object NotificationChannels {
    // The main purpose of having multiple channels is to set different importance levels.
    private fun createRefreshNotifierChannel(context: Context) {
        val name = "Attendance Updates"
        val descriptionText = "Get updates about attendance refresh"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel =
            NotificationChannel(RefreshNotifier.channelID, name, importance).apply {
                description = descriptionText
            }
        // Register the channel with the system.
        val notificationManager: NotificationManager =
            context.getSystemService(NotificationManager::class.java) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNewUpdateNotifierChannel(context: Context) {
        val name = "New update available"
        val descriptionText = "Keep Easeatten up to date"
        // May not be always high for small bug fixes but let's keep it this way.
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel =
            NotificationChannel(AppUpdateNotifier.channelID, name, importance).apply {
                description = descriptionText
            }
        // Register the channel with the system.
        val notificationManager: NotificationManager =
            context.getSystemService(NotificationManager::class.java) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun createAll(context: Context) {
        createRefreshNotifierChannel(context)
        createNewUpdateNotifierChannel(context)
    }
}
