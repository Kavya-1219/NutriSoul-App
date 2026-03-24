package com.simats.nutrisoul

import android.app.TimePickerDialog
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.simats.nutrisoul.ui.theme.LocalDarkTheme
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun SectionHeader(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = title, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun InfoPoint(text: String) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(bottom = 4.dp)) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp, end = 8.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(0xFF22C55E))
        )
        Text(text = text, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
    }
}

@Composable
fun SleepInfoChip(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    containerColor: Color
) {
    val isDark = LocalDarkTheme.current
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = if(isDark) Color(0xFFB39DDB) else Color(0xFF7E57C2))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}

@Composable
fun WhyStressMattersCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF22C55E), Color(0xFF0D9488)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = "Heart Icon",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Why Stress & Sleep Matter for Nutrition", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(12.dp))
                InfoPoint(text = "Stress affects digestion and appetite regulation")
                InfoPoint(text = "Poor sleep increases cravings and tendency to overeat")
                InfoPoint(text = "Hormonal balance (PCOS, diabetes) is influenced by stress and sleep quality")
            }
        }
    }
}

@Composable
fun FoodStressCard(
    icon: ImageVector,
    title: String,
    description: String,
    subtext: String,
    containerColor: Color,
    iconColor: Color
) {
    val isDark = LocalDarkTheme.current
    val borderColor = iconColor.copy(alpha = 0.3f)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(2.dp, borderColor)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(10.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
                Text(subtext, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.7f), lineHeight = 18.sp)
            }
        }
    }
}

@Composable
fun FoodsToReduceStressSection() {
    val isDark = LocalDarkTheme.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(icon = Icons.Default.Spa, title = "Foods That Help Reduce Stress")

        FoodStressCard(
            icon = Icons.Default.Eco,
            title = "Magnesium-Rich Foods",
            description = "Almonds, spinach, pumpkin seeds, dark chocolate",
            subtext = "Helps calm nervous system naturally",
            containerColor = if(isDark) Color(0xFF1B5E20).copy(alpha=0.3f) else Color(0xFFE8F5E9),
            iconColor = if(isDark) Color(0xFF81C784) else Color(0xFF388E3C)
        )
        FoodStressCard(
            icon = Icons.Default.BreakfastDining,
            title = "Complex Carbohydrates",
            description = "Millets, oats, brown rice, quinoa",
            subtext = "Stabilizes mood and energy levels",
            containerColor = if(isDark) Color(0xFFE65100).copy(alpha=0.3f) else Color(0xFFFFF3E0),
            iconColor = if(isDark) Color(0xFFFFB74D) else Color(0xFFF57C00)
        )
        FoodStressCard(
            icon = Icons.Default.EmojiFoodBeverage,
            title = "Herbal Teas & Hydration",
            description = "Chamomile tea, tulsi tea, warm water with lemon",
            subtext = "Promotes relaxation and reduces tension",
            containerColor = if(isDark) Color(0xFF006064).copy(alpha=0.3f) else Color(0xFFE0F7FA),
            iconColor = if(isDark) Color(0xFF4DD0E1) else Color(0xFF0097A7)
        )
    }
}

@Composable
fun NutritionTipCard(
    icon: ImageVector,
    title: String,
    description: String,
    containerColor: Color
) {
    val borderColor = Color(0xFF5E35B1).copy(alpha = 0.2f)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(2.dp, borderColor)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF5E35B1),
                modifier = Modifier
                    .background(Color.White, CircleShape)
                    .padding(10.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
            }
        }
    }
}

@Composable
fun NutritionTipsForSleepSection() {
    val isDark = LocalDarkTheme.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(icon = Icons.Default.NightsStay, title = "Nutrition Tips for Better Sleep")

        NutritionTipCard(
            icon = Icons.Default.DinnerDining,
            title = "Light Dinner Timing",
            description = "Eat dinner 2-3 hours before bedtime for better digestion",
            containerColor = if(isDark) Color(0xFF311B92).copy(alpha=0.3f) else Color(0xFFEDE7F6)
        )
        NutritionTipCard(
            icon = Icons.Default.Fastfood,
            title = "Sleep-Supporting Foods",
            description = "Include foods rich in tryptophan like milk, bananas, dates",
            containerColor = if(isDark) Color(0xFF4A148C).copy(alpha=0.3f) else Color(0xFFF3E5F5)
        )
        NutritionTipCard(
            icon = Icons.Default.NoFood,
            title = "Avoid Late Stimulants",
            description = "Skip caffeine after 2 PM and avoid heavy meals at night",
            containerColor = if(isDark) Color(0xFFB71C1C).copy(alpha=0.3f) else Color(0xFFFFEBEE)
        )
    }
}

@Composable
fun WhyThisMattersInfoCard() {
    val isDark = LocalDarkTheme.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if(isDark) Color(0xFF1B5E20).copy(alpha=0.3f) else Color(0xFFE8F5E9)),
        border = BorderStroke(1.dp, if(isDark) Color(0xFF1B5E20) else Color(0xFFB9F6CA))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Why this matters:", fontWeight = FontWeight.Bold, color = if(isDark) Color(0xFF81C784) else Color(0xFF2E7D32))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Sleep timing and quality affect hunger hormones, digestion, and food choices. Managing stress and sleep through nutrition and simple calm tools supports your overall health goals.",
                fontSize = 14.sp,
                color = if(isDark) Color(0xFFC8E6C9) else Color(0xFF2E7D32),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun BreathingCircle(onStop: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "breathing")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scaleAnim"
    )

    // Track direction by comparing previous scale
    var prev by remember { mutableStateOf(scale) }
    var phase by remember { mutableStateOf("Inhale") }

    LaunchedEffect(Unit) {
        while (true) {
            val current = scale
            phase = if (current >= prev) "Inhale" else "Exhale"
            prev = current
            delay(80) // smooth updates
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(128.dp)
                .scale(scale)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF81D4FA).copy(alpha = 0.8f),
                            Color(0xFF039BE5).copy(alpha = 0.6f),
                            Color(0xFF01579B).copy(alpha = 0.2f)
                        )
                    ),
                    shape = CircleShape
                )
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(phase, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onStop,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7043))
        ) {
            Text("Stop", color = Color.White)
        }
    }
}

@Composable
fun QuickCalmToolsSection(
    isBreathing: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    val isDark = LocalDarkTheme.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(icon = Icons.Default.SelfImprovement, title = "Quick Calm Tools")
        Text(
            "Simple breathing technique to manage stress and support mindful eating",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1A237E).copy(alpha = 0.5f) else Color(0xFFE3F2FD))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Air,
                        contentDescription = "Breathing",
                        tint = Color(0xFF1E88E5),
                        modifier = Modifier
                            .background(Color.White, CircleShape)
                            .padding(10.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("5-Minute Calm Breathing", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            "Simple breathing pattern to calm mind before meals",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isBreathing) {
                    BreathingCircle(onStop = onStop)
                } else {
                    Button(
                        onClick = onStart,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                    ) {
                        Text(
                            "Start Breathing Exercise",
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BedtimeReminderCard(
    reminderEnabled: Boolean,
    onReminderToggled: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Bedtime Reminder", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("Get notified at bedtime", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = reminderEnabled, onCheckedChange = onReminderToggled)
        }
    }
}

@Composable
fun WindDownDialog(
    onDismiss: () -> Unit,
    onStart: () -> Unit,
    onSnooze: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.NightsStay,
                    contentDescription = "Moon",
                    tint = Color(0xFF7E57C2),
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("It's bedtime 🌙", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "Sleeping on time helps digestion and reduces cravings tomorrow.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "",
                            tint = Color(0xFF7E57C2)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Quality sleep helps control hunger hormones and supports mindful eating.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E57C2))
                ) {
                    Text("Start Wind-Down", color = Color.White)
                }
                TextButton(onClick = onSnooze, modifier = Modifier.fillMaxWidth()) {
                    Text("Remind Me in 10 Minutes")
                }
            }
        }
    }
}

@Composable
fun SleepQualitySelector(
    selectedQuality: SleepQuality,
    onQualitySelected: (SleepQuality) -> Unit
) {
    Column {
        Text("Sleep Quality", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            SleepQuality.entries.forEach { quality ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onQualitySelected(quality) }
                ) {
                    Text(quality.emoji, fontSize = 32.sp)
                    Text(quality.label, color = MaterialTheme.colorScheme.onSurface)
                    RadioButton(
                        selected = selectedQuality == quality,
                        onClick = { onQualitySelected(quality) }
                    )
                }
            }
        }
    }
}

@Composable
fun EditSleepScheduleDialog(
    schedule: SleepSchedule,
    onDismiss: () -> Unit,
    onSave: (LocalTime, LocalTime) -> Unit
) {
    var bedtime by remember { mutableStateOf(schedule.bedtime) }
    var wakeTime by remember { mutableStateOf(schedule.wakeTime) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Edit Sleep Schedule", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(24.dp))

                TimePicker(label = "Bedtime", time = bedtime, onTimeChange = { bedtime = it })
                Spacer(modifier = Modifier.height(16.dp))
                TimePicker(label = "Wake-up Time", time = wakeTime, onTimeChange = { wakeTime = it })

                Spacer(modifier = Modifier.height(24.dp))
                Row {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onSave(bedtime, wakeTime) }, modifier = Modifier.weight(1f)) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun LogSleepDialog(
    schedule: SleepSchedule,
    onDismiss: () -> Unit,
    onLog: (LocalTime, LocalTime, SleepQuality) -> Unit
) {
    var bedtime by remember { mutableStateOf(schedule.bedtime) }
    var wakeTime by remember { mutableStateOf(schedule.wakeTime) }
    var quality by remember { mutableStateOf(SleepQuality.Good) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Log Your Sleep", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(24.dp))

                TimePicker(label = "Bedtime", time = bedtime, onTimeChange = { bedtime = it })
                Spacer(modifier = Modifier.height(16.dp))
                TimePicker(label = "Wake-up Time", time = wakeTime, onTimeChange = { wakeTime = it })
                Spacer(modifier = Modifier.height(16.dp))
                SleepQualitySelector(selectedQuality = quality, onQualitySelected = { quality = it })

                Spacer(modifier = Modifier.height(24.dp))
                Row {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onLog(bedtime, wakeTime, quality) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Log")
                    }
                }
            }
        }
    }
}

@Composable
fun TimePicker(
    label: String,
    time: LocalTime,
    onTimeChange: (LocalTime) -> Unit
) {
    val context = LocalContext.current
    val formatter = remember { DateTimeFormatter.ofPattern("h:mm a") }

    val dialog = remember(time) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                onTimeChange(LocalTime.of(hourOfDay, minute))
            },
            time.hour,
            time.minute,
            false // 12-hour format
        )
    }

    Column {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)

        OutlinedButton(
            onClick = { dialog.show() }
        ) {
            Text(time.format(formatter))
        }
    }
}

@Composable
fun SleepTrackingSection(
    sleepLogs: List<SleepLog>,
    sleepSchedule: SleepSchedule,
    weeklyAverageHours: Float,
    onEditClick: () -> Unit,
    onLogClick: () -> Unit,
    onReminderToggled: (Boolean) -> Unit,
    reminderEnabled: Boolean
) {
    val todaySleepLog = sleepLogs.firstOrNull { it.date.isEqual(LocalDate.now()) }
    val isDark = LocalDarkTheme.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(icon = Icons.Default.NightsStay, title = "Sleep Tracking")
        Text("Quality sleep for a healthier you", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.NightsStay,
                    contentDescription = "Moon",
                    tint = if(isDark) Color(0xFFB39DDB) else Color(0xFF7E57C2),
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Last Night's Sleep", color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (todaySleepLog != null) {
                    Text(todaySleepLog.duration, fontSize = 48.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                } else {
                    Text("--", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (todaySleepLog != null) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = if(isDark) todaySleepLog.quality.bgColor.copy(alpha=0.3f) else todaySleepLog.quality.bgColor)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("${todaySleepLog.quality.emoji} ${todaySleepLog.quality.label}", fontWeight = FontWeight.Medium, fontSize = 16.sp, color = if(isDark) Color.White else todaySleepLog.quality.color)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SleepInfoChip(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Alarm,
                        value = sleepSchedule.bedtime.format(DateTimeFormatter.ofPattern("h:mm a")),
                        label = "Bedtime",
                        containerColor = if(isDark) Color(0xFF4A148C).copy(alpha=0.3f) else Color(0xFFF3E5F5)
                    )
                    SleepInfoChip(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.TrendingUp,
                        value = "${String.format("%.1f", weeklyAverageHours)}h",
                        label = "7-Day Avg",
                        containerColor = if(isDark) Color(0xFF4A148C).copy(alpha=0.3f) else Color(0xFFF3E5F5)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onEditClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E57C2))
                ) {
                    Text("Edit Sleep Schedule", modifier = Modifier.padding(vertical = 8.dp), color = Color.White)
                }
                OutlinedButton(
                    onClick = onLogClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, Color(0xFF22C55E))
                ) {
                     Text("Log Today's Sleep", color = Color(0xFF22C55E), modifier = Modifier.padding(vertical = 8.dp), fontWeight = FontWeight.Bold)
                }
            }
        }
        BedtimeReminderCard(reminderEnabled = reminderEnabled, onReminderToggled = onReminderToggled)
    }
}

@Composable
fun RecentSleepHistoryCard(sleepLogs: List<SleepLog>) {
    val isDark = LocalDarkTheme.current
    if (sleepLogs.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionHeader(icon = Icons.Default.History, title = "Recent Sleep History")
                Spacer(modifier = Modifier.height(16.dp))
                sleepLogs
                    .sortedByDescending { it.date }
                    .take(7)
                    .forEach { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = if(isDark) log.quality.bgColor.copy(alpha=0.3f) else log.quality.bgColor)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(log.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text("🌙 ${log.bedtime.format(DateTimeFormatter.ofPattern("h:mm a"))} - ☀️ ${log.wakeTime.format(DateTimeFormatter.ofPattern("h:mm a"))}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${log.quality.emoji} ${log.quality.label}", fontWeight = FontWeight.Medium, color = if(isDark) Color.White else log.quality.color)
                                    Text(log.duration, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
            }
        }
    }
}


