package com.simats.nutrisoul

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.simats.nutrisoul.ui.steps.AddStepsModal
import com.simats.nutrisoul.ui.steps.RemoveStepsModal
import com.simats.nutrisoul.ui.steps.StepsViewModel
import com.simats.nutrisoul.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepsTrackingScreen(
    navController: NavController,
    viewModel: StepsViewModel = hiltViewModel()
) {
    val todaySteps by viewModel.todaySteps.collectAsStateWithLifecycle()
    val weeklyAvgSteps by viewModel.weeklyAverageSteps.collectAsStateWithLifecycle()
    val autoEnabled by viewModel.autoEnabled.collectAsStateWithLifecycle()
    val calories by viewModel.caloriesBurned.collectAsStateWithLifecycle()
    val distance by viewModel.distanceKm.collectAsStateWithLifecycle()

    var showAddSteps by remember { mutableStateOf(false) }
    var showRemoveSteps by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val needsActivity = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        val needsNotif = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

        val activityGranted = !needsActivity || (permissions[Manifest.permission.ACTIVITY_RECOGNITION] == true)
        val notificationGranted = !needsNotif || (permissions[Manifest.permission.POST_NOTIFICATIONS] == true)

        if (activityGranted && notificationGranted) {
            viewModel.setAutoTracking(true)
        } else {
            viewModel.setAutoTracking(false)
            val msg = when {
                !activityGranted && !notificationGranted -> "Activity + Notification permissions required"
                !activityGranted -> "Activity permission required"
                else -> "Notification permission required"
            }
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            HeaderSection(
                onBack = { navController.popBackStack() }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = (-40).dp)
                    .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                AutoTrackingCard(
                    enabled = autoEnabled,
                    onToggle = { checked ->
                        if (checked) {
                            val permissions = mutableListOf<String>()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                            }

                            if (permissions.isNotEmpty()) {
                                permissionLauncher.launch(permissions.toTypedArray())
                            } else {
                                viewModel.setAutoTracking(true)
                            }
                        } else {
                            viewModel.setAutoTracking(false)
                        }
                    },
                    disabled = !viewModel.isSensorAvailable
                )

                ProgressCard(todaySteps.toLong(), 10000)

                Button(
                    onClick = { /* Getting Started logic */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
                ) {
                    Icon(Icons.Default.BookmarkBorder, contentDescription = null, tint = Color.Gray)
                    Spacer(Modifier.width(8.dp))
                    Text("Getting Started", color = Color.Gray, fontWeight = FontWeight.Bold)
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🌟", fontSize = 20.sp)
                        Spacer(Modifier.width(10.dp))
                        Text("Let's get moving today!", color = SuccessGreen, fontWeight = FontWeight.Bold)
                    }
                }

                ActionButtons(
                    onAddStepsClicked = { showAddSteps = true },
                    onRemoveStepsClicked = { showRemoveSteps = true }
                )

                TipsCard()

                Spacer(Modifier.height(30.dp))
            }
        }

        if (showAddSteps) {
            AddStepsModal(
                onDismiss = { showAddSteps = false },
                onAddSteps = { input ->
                    val v = input.toIntOrNull() ?: 0
                    viewModel.addManualSteps(v)
                    showAddSteps = false
                }
            )
        }

        if (showRemoveSteps) {
            RemoveStepsModal(
                onDismiss = { showRemoveSteps = false },
                onRemoveSteps = { input ->
                    val v = input.toIntOrNull() ?: 0
                    viewModel.removeManualSteps(v)
                    showRemoveSteps = false
                }
            )
        }
    }
}

@Composable
private fun HeaderSection(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(StepsGradientStart, StepsGradientEnd)
                )
            )
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(Modifier.width(12.dp))
                Text(
                    "Steps Tracking",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Every step brings you closer!",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 56.dp)
            )
        }
    }
}

@Composable
private fun ActionButtons(onAddStepsClicked: () -> Unit, onRemoveStepsClicked: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(
            onClick = onAddStepsClicked,
            modifier = Modifier.weight(1.5f).height(60.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(listOf(StepsGradientStart, StepsGradientEnd)),
                        RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Steps", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        Button(
            onClick = onRemoveStepsClicked,
            modifier = Modifier.weight(1f).height(60.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Remove, contentDescription = null, tint = Color(0xFFEF4444))
                Spacer(Modifier.width(6.dp))
                Text("Remove", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProgressCard(steps: Long, goal: Int) {
    val progress = if (goal > 0) (steps.toFloat() / goal).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(1000), label = "")
    val density = LocalDensity.current
    val isDark = LocalDarkTheme.current

    val achievement = when {
        steps >= 15000 -> "Super Active"
        steps >= 10000 -> "Very Active"
        steps >= 7500 -> "Active"
        else -> "Getting Started"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB).copy(alpha = if (isDark) 0.1f else 1f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 18.dp.toPx()
                    drawArc(
                        color = if (isDark) Color.DarkGray else Color(0xFFF3E8FF),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        brush = Brush.linearGradient(listOf(StepsGradientStart, StepsGradientEnd)),
                        startAngle = -90f,
                        sweepAngle = 360 * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        steps.toString(),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text("steps", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${(animatedProgress * 100).toInt()}%",
                        color = StepsIconGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isDark) Color(0xFF064E3B).copy(alpha = 0.2f) else Color(0xFFE8F5E9))
                    .border(2.dp, if (isDark) Color(0xFF064E3B) else Color(0xFFC8E6C9), RoundedCornerShape(20.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🏅", fontSize = 20.sp)
                Spacer(Modifier.width(12.dp))
                Text(achievement, fontWeight = FontWeight.ExtraBold, color = if (isDark) Color(0xFFA7F3D0) else SuccessGreen, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun StatsCard(todaySteps: Int, avgSteps: Long, calories: Double, distance: Double) {
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatItem(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.LocalFireDepartment,
            value = calories.toString(),
            label = "kcal",
            iconColor = Color(0xFFFB6A2D)
        )
        StatItem(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Explore,
            value = String.format("%.2f", distance),
            label = "km",
            iconColor = Color(0xFF42A5F5)
        )
        StatItem(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.TrendingUp,
            value = avgSteps.toString(),
            label = "7-day avg",
            iconColor = StepsIconGreen
        )
    }
}

@Composable
private fun AutoTrackingCard(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    disabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (enabled) Color(0xFF8B5CF6).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SettingsAccessibility,
                    contentDescription = null,
                    tint = if (enabled) StepsIconGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("Auto Tracking", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    if (disabled) "Step sensor not available"
                    else if (enabled) "Tracking is ON"
                    else "Enable for auto logs",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                enabled = !disabled,
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = StepsIconGreen)
            )
        }
    }
}


@Composable
fun StatItem(modifier: Modifier = Modifier, icon: ImageVector, value: String, label: String, iconColor: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = label, tint = iconColor, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun TipsCard() {
    val isDark = LocalDarkTheme.current
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("💡", fontSize = 20.sp)
                Spacer(Modifier.width(16.dp))
                Text("Tips to Increase Steps", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp)
            }
            Spacer(Modifier.height(16.dp))
            TipItem("Take the stairs instead of elevators")
            TipItem("Park farther away from entrances")
            TipItem("Take short walking breaks every hour")
            TipItem("Walk while talking on the phone")
        }
    }
}

@Composable
private fun TipItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(Icons.Default.DirectionsWalk, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
    }
}



