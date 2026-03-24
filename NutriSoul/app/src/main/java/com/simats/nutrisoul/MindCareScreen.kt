package com.simats.nutrisoul

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.simats.nutrisoul.data.UserViewModel

@Composable
fun MindCareScreen(
    navController: NavController,
    viewModel: MindCareViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userState by userViewModel.user.collectAsStateWithLifecycle()
    val userEmail = userState?.email ?: ""
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onReminderToggled(true, context, userEmail)
        } else {
            Toast.makeText(context, "Notification permission is required for reminders", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(userEmail) {
        if (userEmail.isNotEmpty()) {
            viewModel.init(context, userEmail)
        }
    }

    MindCareScreenContent(navController, uiState, viewModel) { enabled ->
        if (enabled) {
            // Check Notification Permission (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    return@MindCareScreenContent
                }
            }

            // Check Exact Alarm Permission (Android 12+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (!alarmManager.canScheduleExactAlarms()) {
                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).also { intent ->
                        context.startActivity(intent)
                    }
                    return@MindCareScreenContent
                }
            }
            viewModel.onReminderToggled(true, context, userEmail)
        } else {
            viewModel.onReminderToggled(false, context, userEmail)
        }
    }

    if (uiState.showSleepScheduleDialog) {
        EditSleepScheduleDialog(
            schedule = uiState.sleepSchedule,
            onDismiss = viewModel::onDismissScheduleDialog,
            onSave = { bedtime, wakeTime ->
                viewModel.onSaveSchedule(bedtime, wakeTime, context, userEmail)
            }
        )
    }

    if (uiState.showWindDownDialog) {
        WindDownDialog(
            onDismiss = viewModel::onDismissWindDownDialog,
            onStart = {
                viewModel.onDismissWindDownDialog()
                viewModel.onStartBreathing()
            },
            onSnooze = {
                viewModel.onDismissWindDownDialog()
                viewModel.onSnooze10Min(context, userEmail)
            }
        )
    }

    if (uiState.showLogSleepDialog) {
        LogSleepDialog(
            schedule = uiState.sleepSchedule,
            onDismiss = viewModel::onDismissLogSleepDialog,
            onLog = { b, w, q ->
                viewModel.onLogSleep(b, w, q)
                viewModel.persistAfterLog(context, userEmail)
            }
        )
    }
}

@Composable
fun MindCareScreenContent(
    navController: NavController,
    uiState: MindCareUiState,
    viewModel: MindCareViewModel,
    onReminderToggled: (Boolean) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF22C55E), Color(0xFF0D9488))
                        )
                    )
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 48.dp)
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Mind Care",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Calm mind, better sleep, healthier food choices",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-40).dp)
                    .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                WhyStressMattersCard()
                SleepTrackingSection(
                    sleepLogs = uiState.sleepLogs,
                    sleepSchedule = uiState.sleepSchedule,
                    weeklyAverageHours = uiState.weeklyAverageHours,
                    onEditClick = viewModel::onEditScheduleClicked,
                    onLogClick = viewModel::onLogTodaySleepClicked,
                    onReminderToggled = onReminderToggled,
                    reminderEnabled = uiState.reminderEnabled
                )
                RecentSleepHistoryCard(sleepLogs = uiState.sleepLogs)
                FoodsToReduceStressSection()
                NutritionTipsForSleepSection()
                QuickCalmToolsSection(
                    isBreathing = uiState.isBreathing,
                    onStart = viewModel::onStartBreathing,
                    onStop = viewModel::onStopBreathing
                )
                WhyThisMattersInfoCard()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MindCareScreenPreview() {
    MindCareScreen(rememberNavController())
}



