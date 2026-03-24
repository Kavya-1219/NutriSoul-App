package com.simats.nutrisoul

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.LocalTime
import java.util.Calendar

private const val REQ_BEDTIME_ALARM = 7001
private const val REQ_SNOOZE_ALARM = 7002

private fun bedtimePI(context: Context, userEmail: String): PendingIntent {
    val intent = Intent(context, BedtimeReminderReceiver::class.java).apply {
        action = "ACTION_BEDTIME_REMINDER"
        putExtra("USER_EMAIL", userEmail)
    }
    return PendingIntent.getBroadcast(
        context,
        REQ_BEDTIME_ALARM,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
}

private fun snoozePI(context: Context, userEmail: String): PendingIntent {
    val intent = Intent(context, BedtimeReminderReceiver::class.java).apply {
        action = "ACTION_BEDTIME_SNOOZE"
        putExtra("USER_EMAIL", userEmail)
    }
    return PendingIntent.getBroadcast(
        context,
        REQ_SNOOZE_ALARM,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
}

fun scheduleBedtimeReminder(context: Context, userEmail: String, bedtime: LocalTime) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // cancel previous to avoid duplicates
    alarmManager.cancel(bedtimePI(context, userEmail))

    val calendar = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        set(Calendar.HOUR_OF_DAY, bedtime.hour)
        set(Calendar.MINUTE, bedtime.minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        // If time passed today, schedule tomorrow
        if (timeInMillis <= System.currentTimeMillis()) {
            add(Calendar.DATE, 1)
        }
    }

    val triggerAt = calendar.timeInMillis
    val pi = bedtimePI(context, userEmail)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    } else {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
    }
}

fun cancelBedtimeReminder(context: Context, userEmail: String) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(bedtimePI(context, userEmail))
}

fun scheduleSnoozeReminder(context: Context, userEmail: String, minutes: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    alarmManager.cancel(snoozePI(context, userEmail))

    val triggerAt = System.currentTimeMillis() + minutes * 60_000L
    val pi = snoozePI(context, userEmail)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    } else {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
    }
}
