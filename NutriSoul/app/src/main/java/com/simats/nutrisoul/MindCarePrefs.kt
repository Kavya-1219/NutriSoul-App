package com.simats.nutrisoul

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime

object MindCarePrefs {
    private const val PREF = "mind_care_prefs"
    private const val KEY_BEDTIME = "bedtime"
    private const val KEY_WAKETIME = "waketime"
    private const val KEY_REMINDER = "reminder_enabled"
    private const val KEY_LOGS = "sleep_logs"
    private const val KEY_PENDING_WINDDOWN = "pending_winddown"
    private const val KEY_SNOOZE_UNTIL = "snooze_until"
    private const val KEY_LAST_USER = "last_user"

    private fun k(base: String, user: String) = "${base}_${user.lowercase().trim()}"

    fun saveSchedule(context: Context, user: String, schedule: SleepSchedule) {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        sp.edit()
            .putString(k(KEY_BEDTIME, user), schedule.bedtime.toString())
            .putString(k(KEY_WAKETIME, user), schedule.wakeTime.toString())
            .apply()
    }

    fun loadSchedule(context: Context, user: String): SleepSchedule {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val bed = sp.getString(k(KEY_BEDTIME, user), "22:00") ?: "22:00"
        val wake = sp.getString(k(KEY_WAKETIME, user), "06:00") ?: "06:00"
        return SleepSchedule(LocalTime.parse(bed), LocalTime.parse(wake))
    }

    fun saveReminderEnabled(context: Context, user: String, enabled: Boolean) {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        sp.edit().putBoolean(k(KEY_REMINDER, user), enabled).apply()
    }

    fun loadReminderEnabled(context: Context, user: String): Boolean {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return sp.getBoolean(k(KEY_REMINDER, user), false)
    }

    fun saveLogs(context: Context, user: String, logs: List<SleepLog>) {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val arr = JSONArray()
        logs.sortedByDescending { it.date }.take(7).forEach { log ->
            val obj = JSONObject()
            obj.put("date", log.date.toString())
            obj.put("bedtime", log.bedtime.toString())
            obj.put("wakeTime", log.wakeTime.toString())
            obj.put("duration", log.duration)
            obj.put("durationMinutes", log.durationMinutes)
            obj.put("quality", log.quality.name)
            arr.put(obj)
        }
        sp.edit().putString(k(KEY_LOGS, user), arr.toString()).apply()
    }

    fun loadLogs(context: Context, user: String): List<SleepLog> {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val raw = sp.getString(k(KEY_LOGS, user), null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<SleepLog>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val date = LocalDate.parse(obj.getString("date"))
                val bedtime = LocalTime.parse(obj.getString("bedtime"))
                val wakeTime = LocalTime.parse(obj.getString("wakeTime"))
                val duration = obj.getString("duration")
                val durationMinutes = obj.getInt("durationMinutes")
                val quality = SleepQuality.valueOf(obj.getString("quality"))
                list.add(SleepLog(date, bedtime, wakeTime, duration, durationMinutes, quality))
            }
            list.sortedByDescending { it.date }.take(7)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun setPendingWindDown(context: Context, user: String, pending: Boolean) {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        sp.edit().putBoolean(k(KEY_PENDING_WINDDOWN, user), pending).apply()
    }

    fun consumePendingWindDown(context: Context, user: String): Boolean {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val key = k(KEY_PENDING_WINDDOWN, user)
        val pending = sp.getBoolean(key, false)
        if (pending) sp.edit().putBoolean(key, false).apply()
        return pending
    }

    fun saveSnoozeUntil(context: Context, user: String, millis: Long) {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        sp.edit().putLong(k(KEY_SNOOZE_UNTIL, user), millis).apply()
    }

    fun getSnoozeUntil(context: Context, user: String): Long {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return sp.getLong(k(KEY_SNOOZE_UNTIL, user), 0L)
    }

    fun clearSnooze(context: Context, user: String) {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        sp.edit().remove(k(KEY_SNOOZE_UNTIL, user)).apply()
    }

    fun setLastUser(context: Context, user: String) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
            .putString(KEY_LAST_USER, user).apply()
    }

    fun getLastUser(context: Context): String? {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_LAST_USER, null)
    }
}
