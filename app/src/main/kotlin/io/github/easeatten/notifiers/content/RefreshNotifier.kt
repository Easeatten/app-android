package io.github.easeatten.notifiers.content

import android.content.Context
import androidx.core.app.NotificationCompat
import io.github.easeatten.AppInitializer
import io.github.easeatten.R
import io.github.easeatten.notifiers.INotifiers

object RefreshNotifier : INotifiers {
    override val uniqueID = 1
    override val channelID = "easeatten.attendance_refresh"

    // The public-function definitions are sorted in descending order
    // according to their expected frequency of getting called.
    fun notifySuccess(context: Context) {
        val builder =
            NotificationCompat.Builder(context, channelID)
                .setContentTitle("Attendance Refresh Successful")
                .setContentText("Your attendance has been refreshed successfully")
                .setPriority(NotificationCompat.PRIORITY_LOW)
        builder.setContent()

        AppInitializer.notificationManager.notify(uniqueID, builder.build())
    }

    fun notifyRetryLater(context: Context) {
        val builder =
            NotificationCompat.Builder(context, channelID)
                .setContentTitle("Attendance Refresh Paused")
                .setContentText("Attendance was not refreshed\nRetrying later")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
        builder.setContent()

        AppInitializer.notificationManager.notify(uniqueID, builder.build())
    }

    fun notifyFailure(context: Context) {
        val builder =
            NotificationCompat.Builder(context, channelID)
                .setContentTitle("Attendance Refresh Failed")
                .setContentText("Could not refresh attendance data\nTry to refresh manually")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
        builder.setContent()

        AppInitializer.notificationManager.notify(uniqueID, builder.build())
    }

    override fun NotificationCompat.Builder.setContent(): NotificationCompat.Builder {
        this.setSmallIcon(R.drawable.ic_launcher_foreground)
            .setCategory("Attendance Refresh")
            .setChannelId(channelID)
            .setShowWhen(true)

        return this
    }
}
