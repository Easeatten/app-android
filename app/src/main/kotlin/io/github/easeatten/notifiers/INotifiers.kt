package io.github.easeatten.notifiers

import androidx.core.app.NotificationCompat

interface INotifiers {
    // Must be unique for each implementation.
    val uniqueID: Int
    val channelID: String

    fun NotificationCompat.Builder.setContent(): NotificationCompat.Builder {
        return this
    }
}
