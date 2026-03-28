package com.simats.nutrisoul

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.simats.nutrisoul.data.User
import com.simats.nutrisoul.data.UserViewModel
import com.simats.nutrisoul.steps.StepTrackingService
import com.simats.nutrisoul.ui.DailyTotalsUi
import com.simats.nutrisoul.ui.steps.StepsViewModel
import com.simats.nutrisoul.ui.theme.*
import kotlin.math.abs
import java.util.Calendar

@Composable
fun HomeScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    homeViewModel: HomeViewModel = hiltViewModel(),
    stepsViewModel: StepsViewModel = hiltViewModel()
) {
    val user by userViewModel.user.collectAsStateWithLifecycle()
    val isLoading by userViewModel.isLoading.collectAsStateWithLifecycle()

    val totals by homeViewModel.todayTotals.collectAsStateWithLifecycle()
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    val stepsToday by stepsViewModel.todaySteps.collectAsStateWithLifecycle()
    val autoEnabled by stepsViewModel.autoEnabled.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(autoEnabled) {
        val intent = Intent(context, StepTrackingService::class.java)
        if (autoEnabled) context.startForegroundService(intent) else context.stopService(intent)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator(color = PrimaryGreen)
                user == null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.error_loading_user_data), color = MaterialTheme.colorScheme.onBackground)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { userViewModel.retryLoadUserData() }) { Text(stringResource(R.string.retry)) }
                    }
                }
                else -> {
                    HomeDashboardContent(
                        navController = navController,
                        user = user!!,
                        totals = totals,
                        ui = homeUiState,
                        userViewModel = userViewModel,
                        todaysSteps = stepsToday.toInt()
                    )
                }
            }
        }
    }
}

private fun dailyCalorieGoal(user: User): Int {
    val bmr = user.bmr.takeIf { it > 0 } ?: 1500

    val activityFactor = when (user.activityLevel?.lowercase()) {
        "sedentary" -> 1.2
        "light" -> 1.375
        "moderate" -> 1.55
        "active" -> 1.725
        else -> 1.375
    }

    val tdee = (bmr * activityFactor).toInt()

    val goalLower = (user.goals.firstOrNull() ?: "").trim().lowercase()

    val safeAdjustment = if (goalLower.contains("lose") || goalLower.contains("gain")) {
        val weightDiff = user.targetWeight - user.currentWeight
        val totalCaloriesToShift = weightDiff * 7700
        val days = (user.targetWeeks.coerceAtLeast(1) * 7)
        (totalCaloriesToShift / days).toInt().coerceIn(-1000, 1000)
    } else 0
    
    val goal = tdee + safeAdjustment
    
    return goal.coerceAtLeast(1200)
}

private fun caloriesBurnedFromSteps(steps: Int, weightKg: Float): Int {
    val factor = 0.04 * (weightKg / 70f).coerceIn(0.6f, 1.6f)
    return (steps * factor).toInt().coerceAtLeast(0)
}

@Composable
private fun HomeDashboardContent(
    navController: NavController,
    user: User,
    totals: DailyTotalsUi,
    ui: HomeUiState,
    userViewModel: UserViewModel,
    todaysSteps: Int
) {
    // Priority: 1) Totals from repository (immediate local sync) 2) Backend sync from user profile
    val consumedCals = maxOf(
        (totals.calories ?: 0.0).toInt().coerceAtLeast(0),
        user.todaysCalories.toInt()
    )
    
    // Use targetCalories from user profile if set (> 0), otherwise fallback to local calculation
    val goal = if (user.targetCalories > 0) user.targetCalories else dailyCalorieGoal(user).coerceIn(1200, 4500)
    
    val burnedCals = caloriesBurnedFromSteps(todaysSteps, user.currentWeight)
    val remaining = goal - consumedCals + burnedCals

    val profilePictureUri by userViewModel.profilePictureUri.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        DashboardHeader(
            name = user.name,
            profilePictureUri = profilePictureUri
        )

        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .offset(y = (-40).dp)
                .zIndex(2f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            DailyCaloriesCardFigma(
                consumed = consumedCals,
                target = goal,
                remaining = remaining,
                burned = burnedCals
            )
        }

        Spacer(modifier = Modifier.height((-40).dp))

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            if (hasWeightGoal(user.goals.firstOrNull() ?: "", user.targetWeight)) {
                WeightProgressCardFigma(
                    currentWeight = user.currentWeight,
                    targetWeight = user.targetWeight,
                    goal = user.goals.firstOrNull() ?: ""
                )
            }

            TodayActivitySection(
                navController = navController,
                waterMl = user.todaysWaterIntake.toInt(),
                steps = todaysSteps
            )

            QuickActionsCard(navController)

            DailyTipCardFigma(
                tip = ui.dailyTip.ifBlank {
                    if (consumedCals < goal / 2) stringResource(R.string.tip_under_calorie_target)
                    else if (consumedCals > (goal * 1.2).toInt()) stringResource(R.string.tip_over_calorie_target)
                    else stringResource(R.string.tip_on_track)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DashboardHeader(
    name: String,
    profilePictureUri: Uri?
) {
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..11 -> R.string.greeting_morning
            in 12..17 -> R.string.greeting_afternoon
            else -> R.string.greeting_evening
        }
    }

    val headerBrush = Brush.verticalGradient(
        listOf(PrimaryGreenVibrant, PrimaryGreenVibrant)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            .background(headerBrush)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    stringResource(greeting),
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 14.sp
                )
                Text(
                    name.ifBlank { stringResource(R.string.default_user_name) },
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                if (profilePictureUri != null) {
                    AsyncImage(
                        model = profilePictureUri,
                        contentDescription = stringResource(R.string.profile_picture_content_description),
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("👤", fontSize = 22.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun DailyCaloriesCardFigma(
    consumed: Int,
    target: Int,
    remaining: Int,
    burned: Int
) {
    val safeTarget = target.coerceAtLeast(1)
    val progress = (consumed.toFloat() / safeTarget.toFloat()).coerceIn(0f, 1f)

    Column(modifier = Modifier.padding(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.daily_calorie_goal_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.weight(1f))
            Icon(
                Icons.Outlined.LocalFireDepartment,
                contentDescription = null,
                tint = CalorieOrange
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                consumed.toString(),
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "/ $target",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        ) {
            val barBrush = if (consumed > target) {
                Brush.horizontalGradient(listOf(CalorieOrange, ErrorRed))
            } else {
                Brush.horizontalGradient(listOf(PrimaryGreenVibrant, PrimaryGreenVibrant))
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(999.dp))
                    .background(barBrush)
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = WaterBlueDark,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (remaining >= 0) stringResource(R.string.remaining_kcal_format, remaining) else stringResource(R.string.over_by_kcal_format, abs(remaining)),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = stringResource(R.string.burned_kcal_format, burned),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = PrimaryGreenVibrant
            )
        }
    }
}

private fun hasWeightGoal(goal: String, targetWeight: Float): Boolean {
    val isLose = goal.equals("Weight Loss", ignoreCase = true) || goal.equals("Lose Weight", ignoreCase = true)
    val isGain = goal.equals("Gain Muscle", ignoreCase = true) || goal.equals("Gain Weight", ignoreCase = true)
    return (isLose || isGain) && targetWeight > 0f
}

@Composable
private fun WeightProgressCardFigma(
    currentWeight: Float,
    targetWeight: Float,
    goal: String
) {
    val isLose = goal.equals("Weight Loss", ignoreCase = true) || goal.equals("Lose Weight", ignoreCase = true)
    val icon = if (isLose) Icons.Default.TrendingDown else Icons.Default.TrendingUp
    val isDark = LocalDarkTheme.current

    val cardBrush = if (isDark) {
        Brush.horizontalGradient(listOf(Color(0xFF1E3A8A).copy(alpha=0.3f), Color(0xFF312E81).copy(alpha=0.3f)))
    } else {
        Brush.horizontalGradient(listOf(ProteinBlueLight, Color(0xFFE0E7FF)))
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(cardBrush)
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.weight_progress_title),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.weight(1f))
                    Icon(icon, contentDescription = null, tint = if(isDark) Color(0xFF60A5FA) else ProteinBlue)
                }

                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    WeightMiniBox(label = stringResource(R.string.current_label), value = stringResource(R.string.weight_kg_format, currentWeight), valueColor = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                    WeightMiniBox(label = stringResource(R.string.target_label), value = stringResource(R.string.weight_kg_format, targetWeight), valueColor = if(isDark) Color(0xFF60A5FA) else ProteinBlue, modifier = Modifier.weight(1f))
                }

                Spacer(Modifier.height(10.dp))

                val diff = abs(currentWeight - targetWeight)
                Text(
                    text = stringResource(R.string.weight_to_go_format, diff),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WeightMiniBox(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(2.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

@Composable
private fun TodayActivitySection(
    navController: NavController,
    waterMl: Int,
    steps: Int
) {
    val isDark = LocalDarkTheme.current
    Column {
        Text(
            stringResource(R.string.todays_activity_title),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

            ActivityWidget(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.WaterDrop,
                iconTint = Color(0xFF3B82F6),
                titleValue = waterMl.toString(),
                subtitle = stringResource(R.string.ml_water_label),
                background = if (isDark) {
                    Brush.linearGradient(listOf(WaterBlueDark.copy(alpha=0.2f), WaterBlueDark.copy(alpha=0.3f)))
                } else {
                    Brush.linearGradient(listOf(WaterBlue, Color(0xFFE0F2F1)))
                },
                onClick = { navController.navigate(Screen.WaterTracking.route) }
            )

            ActivityWidget(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.DirectionsWalk,
                iconTint = PrimaryGreenVibrant,
                titleValue = steps.toString(),
                subtitle = stringResource(R.string.steps_today_label),
                background = if (isDark) {
                    Brush.linearGradient(listOf(PrimaryGreenVibrant.copy(alpha=0.2f), PrimaryGreenVibrant.copy(alpha=0.3f)))
                } else {
                    Brush.linearGradient(listOf(PrimaryGreenLight, Color(0xFFDCFCE7)))
                },
                onClick = { navController.navigate(Screen.StepsTracking.route) }
            )
        }
    }
}

@Composable
private fun ActivityWidget(
    modifier: Modifier,
    icon: ImageVector,
    iconTint: Color,
    titleValue: String,
    subtitle: String,
    background: Brush,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .padding(16.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    titleValue,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun QuickActionsCard(navController: NavController) {
    Column {
        Text(
            stringResource(R.string.quick_actions_title),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                QuickActionItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Add,
                    label = stringResource(R.string.log_food_label),
                    description = stringResource(R.string.log_food_desc),
                    iconTint = SuccessGreen,
                    onClick = { navController.navigate(Screen.LogFood.route) }
                )
                QuickActionItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.CameraAlt,
                    label = stringResource(R.string.meal_plan_label),
                    description = stringResource(R.string.meal_plan_desc),
                    iconTint = WaterBlueDark,
                    onClick = { navController.navigate(Screen.MealPlan.route) }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                QuickActionItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.AutoAwesome,
                    label = stringResource(R.string.ai_tips_title),
                    description = stringResource(R.string.ai_tips_desc),
                    iconTint = VibrantPurple,
                    onClick = { navController.navigate(Screen.AiTips.route) }
                )
                QuickActionItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.History,
                    label = stringResource(R.string.history_title),
                    description = stringResource(R.string.history_desc),
                    iconTint = CalorieOrange,
                    onClick = { navController.navigate(Screen.History.route) }
                )
            }
        }
    }
}

@Composable
fun QuickActionItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    description: String,
    iconTint: Color = VibrantPurple,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F1F1))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconTint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint)
            }
            Spacer(Modifier.height(8.dp))
            Text(label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            // Text(description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DailyTipCardFigma(tip: String) {
    val isDark = LocalDarkTheme.current
    val tipBrush = if (isDark) {
        Brush.horizontalGradient(listOf(DeepPurple.copy(alpha=0.3f), Color(0xFF5B21B6).copy(alpha=0.3f)))
    } else {
        Brush.horizontalGradient(listOf(LightPurple, ExtraLightPurple))
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(tipBrush)
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if(isDark) DeepPurple else LightPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = if(isDark) Color(0xFFA5B4FC) else VibrantPurple,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        stringResource(R.string.daily_tip_label),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = if(isDark) Color(0xFFC4B5FD) else DeepPurple
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        tip,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
