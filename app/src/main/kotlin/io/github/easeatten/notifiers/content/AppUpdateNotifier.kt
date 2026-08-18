package io.github.easeatten.notifiers.content

import android.content.Context
import androidx.core.app.NotificationCompat
import io.github.easeatten.AppInitializer
import io.github.easeatten.notifiers.INotifiers

// Code for what happens when the user clicks the notification will be added later.
object AppUpdateNotifier : INotifiers {
    override val uniqueID = 2
    override val channelID = "easeatten.app_update"

    fun notifyMinorUpdate(context: Context, message: String) {
        val builder =
            NotificationCompat.Builder(context, channelID)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        builder.setContent()

        AppInitializer.notificationManager.notify(AppUpdateNotifier.uniqueID, builder.build())
    }

    fun notifyMajorUpdate(context: Context, message: String) {
        val builder =
            NotificationCompat.Builder(context, channelID)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        builder.setContent()

        AppInitializer.notificationManager.notify(AppUpdateNotifier.uniqueID, builder.build())
    }

    fun notifyUrgentUpdates(context: Context, message: String) {
        val builder =
            NotificationCompat.Builder(context, channelID)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
        builder.setContent()

        AppInitializer.notificationManager.notify(AppUpdateNotifier.uniqueID, builder.build())
    }

    override fun NotificationCompat.Builder.setContent(): NotificationCompat.Builder {
        this.setContentTitle("New Update Available")
            .setCategory("App updates")
            .setChannelId(AppUpdateNotifier.channelID)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setShowWhen(true)

        return this
    }
}
