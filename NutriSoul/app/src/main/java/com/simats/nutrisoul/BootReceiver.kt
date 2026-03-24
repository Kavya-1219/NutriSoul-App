package com.simats.nutrisoul

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        
        val user = MindCarePrefs.getLastUser(context) ?: return
        
        if (!MindCarePrefs.loadReminderEnabled(context, user)) return
        val schedule = MindCarePrefs.loadSchedule(context, user)
        scheduleBedtimeReminder(context, user, schedule.bedtime)
    }
}
