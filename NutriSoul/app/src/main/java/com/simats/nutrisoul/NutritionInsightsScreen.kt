package com.simats.nutrisoul

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.simats.nutrisoul.ui.theme.LocalDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionInsightsScreen(
    navController: NavController,
    viewModel: NutritionInsightsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Nutrition Insights") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFFF3E5F5), Color(0xFFFFFFFF))))
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Header()

            when (state) {
                NutritionInsightsUiState.Loading -> LoadingBody()
                NutritionInsightsUiState.Empty -> EmptyBody()
                is NutritionInsightsUiState.Success -> {
                    val data = (state as NutritionInsightsUiState.Success).data
                    Body(data)
                }
            }
        }
    }
}

@Composable
private fun Header() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF6366F1), Color(0xFF4F46E5))
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = "Performance Overview",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Track your nutrition trends for the last 7 days",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun LoadingBody() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(18.dp))
        CircularProgressIndicator()
        Spacer(Modifier.height(12.dp))
        Text("Loading insights…", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyBody() {
    val isDark = LocalDarkTheme.current
    Column(modifier = Modifier.padding(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(72.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(34.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text("No Data Yet", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Start logging your meals to see personalized nutrition insights and track your progress over time.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(14.dp))
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = if(isDark) Color(0xFF1E3A8A).copy(alpha=0.3f) else Color(0xFFEFF6FF))
                ) {
                    Text(
                        "💡 Tip: Log meals for at least 3–4 days to get meaningful insights!",
                        modifier = Modifier.padding(12.dp),
                        color = if(isDark) Color(0xFF93C5FD) else Color(0xFF1E40AF),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun Body(data: NutritionInsightsData) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        WeeklyConsistencyCard(data)
        AverageDailyIntakeCard(data)
        MacroDistributionCard(data)
        InsightsCard(data)
    }
}

@Composable
private fun WeeklyConsistencyCard(data: NutritionInsightsData) {
    val isDark = LocalDarkTheme.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB).copy(alpha = if (isDark) 0.1f else 1f))
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Weekly Consistency", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = Color(0xFFFFC107))
            }

            Spacer(Modifier.height(18.dp))

            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                CircularProgressIndicator(
                    progress = data.weeklyConsistency.coerceIn(0f, 1f),
                    modifier = Modifier.size(112.dp),
                    strokeWidth = 10.dp,
                    color = Color(0xFF8B5CF6),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${data.consistencyPercent}%", fontWeight = FontWeight.Bold, fontSize = 26.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text("Logged", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(14.dp))
            Text(
                "${data.daysLogged} out of ${data.totalDays} days logged this week",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun AverageDailyIntakeCard(data: NutritionInsightsData) {
    val status = data.calorieStatus
    val isDark = LocalDarkTheme.current

    val statusBg = when (status.tone) {
        StatusTone.GOOD -> if(isDark) Color(0xFF064E3B) else Color(0xFFECFDF5)
        StatusTone.OK -> if(isDark) Color(0xFF1E3A8A) else Color(0xFFEFF6FF)
        StatusTone.WARN -> if(isDark) Color(0xFF7C2D12) else Color(0xFFFFF7ED)
        StatusTone.INFO -> if(isDark) Color(0xFF78350F) else Color(0xFFFFFBEB)
        StatusTone.NEUTRAL -> MaterialTheme.colorScheme.surfaceVariant
    }

    val barColor = when (status.tone) {
        StatusTone.GOOD, StatusTone.OK -> Color(0xFF10B981)
        StatusTone.WARN -> Color(0xFFF97316)
        StatusTone.INFO -> Color(0xFFF59E0B)
        StatusTone.NEUTRAL -> Color(0xFF9CA3AF)
    }

    val progress = if (data.targetCalories > 0)
        (data.averageCalories / data.targetCalories.toFloat()).coerceIn(0f, 1f)
    else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB).copy(alpha = if (isDark) 0.1f else 1f))
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Average Daily Intake", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                Icon(Icons.Filled.DateRange, contentDescription = null, tint = Color(0xFF8B5CF6))
            }

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = statusBg)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Calories", fontWeight = FontWeight.SemiBold, color = if(status.tone == StatusTone.NEUTRAL) MaterialTheme.colorScheme.onSurfaceVariant else Color.Unspecified)
                        Spacer(Modifier.weight(1f))
                        Text("${status.emoji} ${status.label}", fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("${data.averageCalories}", fontWeight = FontWeight.Bold, fontSize = 26.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("/ ${data.targetCalories} kcal", color = if(isDark) Color.LightGray else Color.Gray, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(6.dp)),
                        color = barColor,
                        trackColor = Color(0xFFE5E7EB).copy(alpha = 0.3f)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MacroChip(
                    modifier = Modifier.weight(1f),
                    name = "Protein",
                    value = String.format("%.1fg", data.averageProtein),
                    percentage = "${data.proteinPercentage}%",
                    bg = if(isDark) Color(0xFF1E3A8A).copy(alpha=0.2f) else Color(0xFFE0F2FE),
                    fg = if(isDark) Color(0xFF93C5FD) else Color(0xFF0284C7)
                )
                MacroChip(
                    modifier = Modifier.weight(1f),
                    name = "Carbs",
                    value = String.format("%.1fg", data.averageCarbs),
                    percentage = "${data.carbsPercentage}%",
                    bg = if(isDark) Color(0xFF7C2D12).copy(alpha=0.2f) else Color(0xFFFEF3C7),
                    fg = if(isDark) Color(0xFFFDBA74) else Color(0xFFD97706)
                )
                MacroChip(
                    modifier = Modifier.weight(1f),
                    name = "Fats",
                    value = String.format("%.1fg", data.averageFats),
                    percentage = "${data.fatsPercentage}%",
                    bg = if(isDark) Color(0xFF78350F).copy(alpha=0.2f) else Color(0xFFFEE2E2),
                    fg = if(isDark) Color(0xFFFCA5A5) else Color(0xFFDC2626)
                )
            }
        }
    }
}

@Composable
private fun MacroDistributionCard(data: NutritionInsightsData) {
    val isDark = LocalDarkTheme.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(Modifier.padding(24.dp)) {
            Text("Macro Distribution", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(20.dp))

            MacroDistributionItem("Protein", data.proteinPercentage, "Range: 15–30%", Color(0xFF0284C7))
            Spacer(Modifier.height(18.dp))
            MacroDistributionItem("Carbohydrates", data.carbsPercentage, "Range: 45–65%", Color(0xFFD97706))
            Spacer(Modifier.height(18.dp))
            MacroDistributionItem("Fats", data.fatsPercentage, "Range: 20–35%", Color(0xFFDC2626))
        }
    }
}

@Composable
private fun MacroDistributionItem(name: String, pct: Int, recommended: String, color: Color) {
    val progress = (pct / 100f).coerceIn(0f, 1f)
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text("$pct%", color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(6.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(recommended, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
    }
}

@Composable
private fun InsightsCard(data: NutritionInsightsData) {
    val isDark = LocalDarkTheme.current
    val insights = buildList {
        if (data.consistencyPercent >= 80) add("✓ Perfect consistency this week!")
        if (data.consistencyPercent < 50) add("• Consider logging more meals for precision")
        if (data.calorieStatus.label == "Excellent") add("✓ Calorie intake is perfectly on target")

        val proteinOk = data.proteinPercentage in 15..30
        if (proteinOk) add("✓ Protein distribution is ideal")
        if (!proteinOk) add("• Adjust protein intake for better balance")

        if (data.fatsPercentage > 35) add("• Fat intake is above recommended range")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if(isDark) Color(0xFF1E1B4B) else Color(0xFFEEF2FF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.2f))
    ) {
        Column(Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(0xFF6366F1).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = Color(0xFF4F46E5), modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(16.dp))
                Text("Smart Insights", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = if(isDark) Color(0xFFC7D2FE) else Color(0xFF3730A3))
            }

            Spacer(Modifier.height(20.dp))
            insights.forEachIndexed { i, line ->
                Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(bottom = if (i != insights.lastIndex) 12.dp else 0.dp)) {
                    val iconColor = if (line.startsWith("✓")) Color(0xFF10B981) else Color(0xFFF59E0B)
                    Icon(
                        if (line.startsWith("✓")) Icons.Filled.TrackChanges else Icons.Filled.TrendingUp,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp).padding(top = 2.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        line.removeRange(0, 1).trim(),
                        color = if(isDark) Color(0xFFE0E7FF) else Color(0xFF4338CA),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun MacroChip(
    modifier: Modifier,
    name: String,
    value: String,
    percentage: String,
    bg: Color,
    fg: Color
) {
    Box(
        modifier = modifier
            .background(bg, RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(name, fontSize = 12.sp, color = fg)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = fg)
            Text(percentage, fontSize = 12.sp, color = fg)
        }
    }
}


