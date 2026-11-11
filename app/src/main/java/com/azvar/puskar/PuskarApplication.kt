package com.azvar.puskar

import android.app.Application
import com.azvar.puskar.data.reminder.EventReminderWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PuskarApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        EventReminderWorker.createChannel(this)
    }
}
