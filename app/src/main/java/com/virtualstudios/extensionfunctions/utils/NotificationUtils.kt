package com.virtualstudios.extensionfunctions.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.virtualstudios.extensionfunctions.BuildConfig
import com.virtualstudios.extensionfunctions.R


val NOTIFICATION_CHANNEL_ID: String = "BuildConfig.NOTIFICATION_CHANNEL_ID"

fun createNotificationChannel(
    context: Context,
    channelId: String,
    channelName: String
) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = ContextCompat.getSystemService(
                context,
                NotificationManager::class.java
            )
            if (manager != null && manager.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(
                    channelId,
                    channelName, NotificationManager.IMPORTANCE_HIGH
                )
                channel.enableLights(true)
                manager.createNotificationChannel(channel)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun sendNotification(
    context: Context,
    title: String,
    info: String,
    channelId: String,
    notificationId: Int,
    pendingIntent: PendingIntent?
) {
    val manager = ContextCompat.getSystemService(
        context,
        NotificationManager::class.java
    )
    val builder = NotificationCompat.Builder(context, channelId)
    builder.setContentTitle(title)
    builder.setContentText(info)
    if (pendingIntent != null) builder.setContentIntent(pendingIntent)
    //builder.setSmallIcon(R.drawable.notification_icon_transparent_white)
    builder.setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.logo))
//    if (context is App) {
//        builder.setLights((context as App).getNotificationLightColor(), 3000, 3000)
//    }
    builder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
    builder.setAutoCancel(true)
    val notification = builder.build()
    if (manager == null) {
//        Debug.LogE(TAG, "Notification mananger is null")
        return
    }
    manager.notify(notificationId, notification)
}