package com.simats.nutrisoul.steps

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.simats.nutrisoul.R
import com.simats.nutrisoul.data.AppDatabase
import com.simats.nutrisoul.data.SessionManager
import com.simats.nutrisoul.data.StepsEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class StepTrackingService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepCounter: Sensor? = null

    private lateinit var store: StepsStore
    private lateinit var sessionManager: SessionManager
    private lateinit var database: AppDatabase

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()

        store = StepsStore(applicationContext)
        sessionManager = SessionManager(applicationContext)
        database = AppDatabase.getDatabase(applicationContext)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        startForeground(NOTIFICATION_ID, buildNotification("Tracking your steps..."))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val sensor = stepCounter
        if (sensor == null) {
            stopSelf()
            return START_NOT_STICKY
        }
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        return START_STICKY
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }

    override fun onSensorChanged(event: SensorEvent) {
        // ✅ Treat step counter as Long to avoid float rounding quirks
        val totalSinceBoot = event.values.firstOrNull()?.toLong() ?: return
        Log.d("STEPS", "sensor totalSinceBoot=${totalSinceBoot}")

        scope.launch {
            val email = sessionManager.currentUserEmailFlow().first() ?: "guest"
            val resetData = store.updateFromStepCounter(user = email, totalSinceBoot = totalSinceBoot)
            
            // If a new day started, save the previous day's data to the database
            resetData?.let { (date, steps) ->
                database.stepsDao().upsert(
                    StepsEntity(
                        date = LocalDate.parse(date),
                        steps = steps.toLong(),
                        goal = 10000 // Default goal, could be fetched from preferences
                    )
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(text: String): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Step Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("NutriSoul")
            .setContentText(text)
            .setSmallIcon(R.drawable.nutrisoul)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "steps_tracking"
        private const val NOTIFICATION_ID = 1001
    }
}
