package com.simats.nutrisoul

import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class BedtimeAlarmActivity : ComponentActivity() {

    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show even on lock screen + turn screen on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        startAlarmSoundAndVibration()

        val userEmail = intent.getStringExtra("USER_EMAIL") ?: ""

        setContent {
            BedtimePopupUI(
                onStartWindDown = {
                    stopAlarmSoundAndVibration()
                    finish()
                },
                onSnooze10 = {
                    stopAlarmSoundAndVibration()
                    if (userEmail.isNotBlank()) {
                        MindCarePrefs.saveSnoozeUntil(this, userEmail, System.currentTimeMillis() + 10 * 60 * 1000L)
                        scheduleSnoozeReminder(this, userEmail, 10)
                    }
                    finish()
                },
                onDismiss = {
                    stopAlarmSoundAndVibration()
                    finish()
                }
            )
        }
    }

    private fun startAlarmSoundAndVibration() {
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        ringtone = RingtoneManager.getRingtone(this, alarmUri)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ringtone?.audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        }

        try {
            ringtone?.play()
        } catch (_: Exception) {}

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        try {
            val pattern = longArrayOf(0, 800, 400, 800, 400, 800)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (_: Exception) {}
    }

    private fun stopAlarmSoundAndVibration() {
        try {
            ringtone?.stop()
        } catch (_: Exception) {}
        try {
            vibrator?.cancel()
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        stopAlarmSoundAndVibration()
        super.onDestroy()
    }
}

@Composable
private fun BedtimePopupUI(
    onStartWindDown: () -> Unit,
    onSnooze10: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(color = Color(0xFF0B1220)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111A2E))
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("It’s bedtime 🌙", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Sleep on time helps cravings, digestion, and mood tomorrow.",
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Spacer(Modifier.height(18.dp))

                    Button(onClick = onStartWindDown, modifier = Modifier.fillMaxWidth()) {
                        Text("Start Wind-Down")
                    }
                    Spacer(Modifier.height(10.dp))

                    OutlinedButton(onClick = onSnooze10, modifier = Modifier.fillMaxWidth()) {
                        Text("Snooze 10 min")
                    }
                    Spacer(Modifier.height(10.dp))

                    TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                        Text("Dismiss", color = Color.White)
                    }
                }
            }
        }
    }
}
