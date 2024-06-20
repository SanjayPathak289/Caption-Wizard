package com.wrongcode.captionwizard

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ServiceApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel("running_channel",
            "Running Notifications",
            NotificationManager.IMPORTANCE_LOW)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}