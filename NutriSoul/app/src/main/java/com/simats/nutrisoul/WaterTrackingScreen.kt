package com.simats.nutrisoul

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.simats.nutrisoul.data.UserViewModel
import com.simats.nutrisoul.ui.theme.*

@Composable
fun WaterTrackingScreen(navController: NavController, userViewModel: UserViewModel) {
    val user by userViewModel.user.collectAsStateWithLifecycle()
    val dailyGoal = 2275
    var selectedGlassSize by remember { mutableStateOf(250) }
    val isDark = LocalDarkTheme.current

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
            WaterTrackingHeader(navController)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = (-40).dp)
                    .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                WaterProgress(user?.todaysWaterIntake ?: 0, dailyGoal)
                
                TodayProgress(user?.todaysWaterIntake ?: 0, selectedGlassSize, dailyGoal)
                
                HydrationJourneyMessage(user?.todaysWaterIntake ?: 0, dailyGoal)
                
                GlassSizeSelector(selectedGlassSize) { selectedGlassSize = it }
                
                WaterActionButtons(
                    onAdd = { userViewModel.updateWaterIntake(selectedGlassSize) },
                    onRemove = { userViewModel.updateWaterIntake(-selectedGlassSize) },
                    glassSize = selectedGlassSize,
                    canRemove = (user?.todaysWaterIntake ?: 0) > 0
                )
                
                WaterStats(dailyGoal)
                
                HydrationBenefits()
                
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun WaterTrackingHeader(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(WaterGradientStart, WaterGradientEnd)
                )
            )
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "Water Tracking",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Stay hydrated, stay healthy!",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun WaterProgress(waterIntake: Int, dailyGoal: Int) {
    val progress = if (dailyGoal > 0) (waterIntake.toFloat() / dailyGoal).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "WaterProgress"
    )
    val percentage = (progress * 100).toInt()
    val isDark = LocalDarkTheme.current

    Card(
        modifier = Modifier.size(260.dp),
        shape = CircleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB).copy(alpha = if (isDark) 0.1f else 1f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 16.dp.toPx()
                drawArc(
                    color = if (isDark) Color.DarkGray else Color(0xFFF3E8FF),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                drawArc(
                    brush = Brush.linearGradient(
                        colors = listOf(WaterGradientStart, WaterGradientEnd)
                    ),
                    startAngle = -90f,
                    sweepAngle = 360 * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = WaterIconBlue,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    "$waterIntake",
                    fontSize = 52.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text("ml / $dailyGoal ml", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                Text("$percentage%", color = WaterIconBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun TodayProgress(waterIntake: Int, glassSize: Int, dailyGoal: Int) {
    val glasses = waterIntake / glassSize
    val targetGlasses = dailyGoal / glassSize
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Today's Progress", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
        Text("$glasses / $targetGlasses glasses", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = WaterIconBlue)
        Text("(${glassSize}ml per glass)", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
    }
}

@Composable
private fun HydrationJourneyMessage(waterIntake: Int, dailyGoal: Int) {
    val isDark = LocalDarkTheme.current
    val percentage = if (dailyGoal > 0) (waterIntake.toFloat() / dailyGoal) * 100 else 0f
    
    val message = when {
        percentage >= 100 -> "🎉 Excellent! Goal met!"
        percentage >= 75 -> "💧 Almost there! Keep it up!"
        percentage >= 50 -> "👍 Good progress! Halfway!"
        percentage >= 25 -> "💪 Great start! Keep going!"
        else -> "🌟 Start your journey today!"
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if(isDark) Color(0xFF1E3A8A).copy(alpha=0.3f) else Color(0xFFEFF6FF)
        ),
        border = BorderStroke(1.dp, WaterGradientStart.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Waves, contentDescription = null, tint = WaterIconBlue)
            Spacer(Modifier.width(12.dp))
            Text(message, fontWeight = FontWeight.ExtraBold, color = if(isDark) Color(0xFF93C5FD) else WaterIconBlue, fontSize = 16.sp)
        }
    }
}

@Composable
private fun GlassSizeSelector(selectedGlass: Int, onGlassSizeSelected: (Int) -> Unit) {
    val glassSizes = listOf(200, 250, 300, 350, 400, 500)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Glass Size", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            glassSizes.take(3).forEach { size ->
                GlassSizeButton(size, selectedGlass, onGlassSizeSelected, Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            glassSizes.takeLast(3).forEach { size ->
                GlassSizeButton(size, selectedGlass, onGlassSizeSelected, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun GlassSizeButton(size: Int, selectedGlass: Int, onGlassSizeSelected: (Int) -> Unit, modifier: Modifier) {
    val isSelected = size == selectedGlass
    Button(
        onClick = { onGlassSizeSelected(size) },
        modifier = modifier.height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) WaterIconBlue else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            "${size}ml", 
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun WaterActionButtons(onAdd: () -> Unit, onRemove: () -> Unit, glassSize: Int, canRemove: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(
            onClick = onRemove,
            enabled = canRemove,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE5E7EB),
                contentColor = Color(0xFF4B5563)
            ),
            modifier = Modifier.weight(1f).height(60.dp),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Remove")
            Spacer(Modifier.width(6.dp))
            Text("Remove", fontWeight = FontWeight.Bold)
        }
        
        Button(
            onClick = onAdd,
            modifier = Modifier.weight(1.5f).height(60.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(listOf(WaterGradientStart, WaterGradientEnd)),
                        RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Add ${glassSize}ml", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun WaterStats(dailyGoal: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        StatCard(
            label = "Daily Goal",
            value = "$dailyGoal ml",
            subValue = "10 glasses",
            icon = Icons.Default.TrackChanges,
            iconColor = Color(0xFF10B981),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "7-Day Avg",
            value = "0 ml",
            subValue = "0% of goal",
            icon = Icons.Default.TrendingUp,
            iconColor = WaterIconBlue,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, subValue: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(10.dp))
            Text(label, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Text(value, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp)
            Text(subValue, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        }
    }
}

@Composable
private fun HydrationBenefits() {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = WaterIconBlue)
                Spacer(Modifier.width(10.dp))
                Text("Benefits of Hydration", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.height(16.dp))
            BenefitItem("Boosts metabolism - Keeps you energized")
            BenefitItem("Reduces appetite - Supports weight management")
            BenefitItem("Improves skin - Keeps skin glowing")
            BenefitItem("Better focus - Helps mental clarity")
        }
    }
}

@Composable
private fun BenefitItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 6.dp)
    ) {
        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = WaterIconBlue, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}



