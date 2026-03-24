package com.simats.nutrisoul

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.simats.nutrisoul.data.meal.model.Meal as ModelMeal
import com.simats.nutrisoul.data.meal.model.MealPlan as ModelMealPlan
import com.simats.nutrisoul.data.meal.model.totalCalories
import com.simats.nutrisoul.data.meal.model.totalCarbs
import com.simats.nutrisoul.data.meal.model.totalFats
import com.simats.nutrisoul.data.meal.model.totalProtein
import com.simats.nutrisoul.data.DailyTotalsUi
import com.simats.nutrisoul.ui.theme.*
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodaysMealPlanScreen(
    navController: NavController,
    viewModel: MealPlanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val tips by viewModel.tips.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val todayTotals by viewModel.todayTotals.collectAsState()
    val alternativeMeals by viewModel.alternativeMeals.collectAsState()

    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val s = uiState) {
                is MealPlanUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is MealPlanUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(s.message, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                is MealPlanUiState.Ready -> {
                    val plan = s.plan
                    val targetGoal = userProfile?.targetCalories ?: plan.targetCalories
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item {
                            PremiumHeader(plan, todayTotals, targetGoal, navController, onRefresh = { viewModel.refresh() })
                        }
                        
                        item {
                            Spacer(Modifier.height(12.dp))
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                PersonalizedBanner(
                                    goal = userProfile?.goals?.firstOrNull() ?: "General Health",
                                    diet = userProfile?.dietaryRestrictions?.firstOrNull() ?: "Balanced"
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                if (tips.isNotEmpty()) {
                                    AiSuggestionSection(tips = tips)
                                } else {
                                    AiSuggestionSection(tip = "Generating personalized tips...")
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                        }

                        items(plan.meals) { mealItem ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                MealCard(
                                    meal = mealItem,
                                    isEaten = mealItem.isEaten,
                                    onEatenToggle = {
                                        viewModel.markAsEaten(mealItem)
                                    }
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                        }

                        item {
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                InfoNote()
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { 
                showSheet = false
                viewModel.selectMealTypeForAlternatives(null)
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    text = "Change Meal",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                
                if (alternativeMeals.isEmpty()) {
                    Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                } else {
                    alternativeMeals.forEach { alt ->
                        AlternativeMealRow(
                            meal = alt,
                            onSelect = {
                                viewModel.swapMeal(alt.mealType, alt)
                                showSheet = false
                            }
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PremiumHeader(plan: ModelMealPlan, totals: DailyTotalsUi, targetCalories: Int, navController: NavController, onRefresh: () -> Unit) {
    val consumed = (totals.calories ?: 0.0).toInt()
    val target = if (targetCalories > 0) targetCalories else 2000
    val progress = if (target <= 0) 0f else min(1f, consumed.toFloat() / target.toFloat())
    val delta = target - consumed

    val gradient = Brush.linearGradient(
        listOf(StepsGradientStart, StepsGradientEnd)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        StepsGradientStart,
                        StepsGradientStart.copy(alpha = 0.9f),
                        StepsGradientEnd
                    )
                )
            )
            .padding(top = 24.dp, bottom = 30.dp, start = 20.dp, end = 20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        .size(44.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    "Today's Plan",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.weight(1f))
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        .size(44.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                }
            }
            
            Spacer(Modifier.height(20.dp))
        Text("Consumed: $consumed kcal", color = Color.White.copy(alpha = 0.95f), fontWeight = FontWeight.Bold)
        Text(
            "Protein: ${totals.protein.toInt()}g • Carbs: ${totals.carbs.toInt()}g • Fats: ${totals.fats.toInt()}g",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 13.sp
        )

        Spacer(Modifier.height(10.dp))

        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.25f)
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Target: $target kcal", color = Color.White.copy(alpha = 0.95f), fontSize = 14.sp)

            val badgeColor = if (delta >= 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
            val badgeText = if (delta >= 0) "${delta} kcal left" else "${-delta} kcal over"

            Box(
                modifier = Modifier
                    .background(badgeColor, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    badgeText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF1B5E20)
                )
            }
            }
        }
    }
}

@Composable
private fun AiSuggestionSection(tips: List<com.simats.nutrisoul.data.network.AiTipDto>) {
    val isDark = LocalDarkTheme.current
    val tip = tips.firstOrNull() ?: return
    
    val cardBg = if (isDark) {
        try { Color(android.graphics.Color.parseColor(tip.cardBg)) } catch(e: Exception) { PrimaryGreenVibrant.copy(alpha=0.15f) }
    } else {
        try { Color(android.graphics.Color.parseColor(tip.cardBg)) } catch(e: Exception) { PrimaryGreenLight }
    }
    
    val borderColor = try { Color(android.graphics.Color.parseColor(tip.borderColor)) } catch(e: Exception) { if(isDark) PrimaryGreenVibrant.copy(alpha=0.3f) else PrimaryGreenVibrant.copy(alpha=0.1f) }
    val iconTint = try { Color(android.graphics.Color.parseColor(tip.iconTint)) } catch(e: Exception) { PrimaryGreenVibrant }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                when(tip.icon.lowercase()) {
                    "star" -> Icons.Default.Star
                    "info" -> Icons.Default.Info
                    "trending_up" -> Icons.Default.TrendingUp
                    "local_fire_department" -> Icons.Default.LocalFireDepartment
                    else -> Icons.Default.AutoAwesome
                }, 
                contentDescription = null, 
                tint = iconTint
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(tip.title, fontWeight = FontWeight.Bold, color = iconTint)
                Text(tip.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun AiSuggestionSection(tip: String) {
    val isDark = LocalDarkTheme.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = if(isDark) PrimaryGreenVibrant.copy(alpha=0.15f) else PrimaryGreenLight),
        border = BorderStroke(1.dp, if(isDark) PrimaryGreenVibrant.copy(alpha=0.3f) else PrimaryGreenVibrant.copy(alpha=0.1f))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, contentDescription = null, tint = PrimaryGreenVibrant)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("AI Personalized Tip", fontWeight = FontWeight.Bold, color = if(isDark) PrimaryGreenLight else PrimaryGreenVibrant)
                Text(tip, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun PersonalizedBanner(goal: String, diet: String) {
    val isDark = LocalDarkTheme.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if(isDark) Color(0xFF1E3A8A).copy(alpha=0.3f) else Color(0xFFEAF2FF), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = if(isDark) Color(0xFF93C5FD) else Color(0xFF2F5BEA))
        Spacer(Modifier.width(10.dp))
        Column {
            Text("Personalized for Your Goals", fontWeight = FontWeight.Bold, color = if(isDark) Color(0xFF93C5FD) else Color(0xFF1A3DBA))
            Text(
                "This plan is customized for ${goal.replace('_', ' ')} and your $diet diet.",
                fontSize = 12.sp,
                color = if(isDark) Color(0xFFA5B4FC) else Color(0xFF1A3DBA)
            )
        }
    }
}

@Composable
private fun MealCard(meal: ModelMeal, isEaten: Boolean, onEatenToggle: () -> Unit) {
    val isDark = LocalDarkTheme.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(mealIcon(meal.mealType), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(8.dp))
                Text(meal.mealType.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.weight(1f))
                // Edit button removed as per requirements
            }

            Text(meal.title, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                MacroChip("Calories", "${meal.calories}", if(isDark) Color(0xFF064E3B).copy(alpha=0.4f) else LightGreenBackground, if(isDark) Color(0xFFA7F3D0) else SuccessGreen)
                MacroChip("Protein", "${meal.protein}g", if(isDark) WaterBlueDark.copy(alpha=0.2f) else WaterBlue, if(isDark) WaterBlueDark else WaterBlueDark)
                MacroChip("Carbs", "${meal.carbs}g", if(isDark) CalorieOrange.copy(alpha=0.2f) else CalorieOrangeLight, if(isDark) CalorieOrange else CalorieOrange)
                MacroChip("Fats", "${meal.fats}g", if(isDark) Color(0xFF78350F).copy(alpha=0.4f) else Color(0xFFFFFBEB), if(isDark) Color(0xFFFCD34D) else Color(0xFFCA8A04))
            }

            Spacer(Modifier.height(12.dp))
            Text("ITEMS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(8.dp))
            meal.items.forEach { item ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(item.name, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text(item.quantity, fontSize = 12.sp, color = Color(0xFF2ECC71))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${item.calories} kcal", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            "P:${item.protein}g C:${item.carbs}g F:${item.fats}g",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Divider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
            }

            Spacer(Modifier.height(16.dp))

            if (isEaten) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
                        .clickable { onEatenToggle() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = SuccessGreen)
                    Spacer(Modifier.width(8.dp))
                    Text("Eaten", color = SuccessGreen, fontWeight = FontWeight.Bold)
                }
            } else {
                OutlinedButton(
                    onClick = onEatenToggle,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SuccessGreen),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SuccessGreen)
                ) {
                    Text("Mark as Eaten", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun MacroChip(label: String, value: String, bg: Color, fg: Color) {
    Column(
        modifier = Modifier
            .background(bg.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .border(1.dp, bg.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = fg.copy(alpha = 0.8f))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = fg)
    }
}

@Composable
private fun AlternativeMealRow(meal: ModelMeal, onSelect: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(mealIcon(meal.mealType), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(meal.title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "${meal.calories} kcal • P ${meal.protein}g • C ${meal.carbs}g • F ${meal.fats}g",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text("Select", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun InfoNote() {
    val isDark = LocalDarkTheme.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if(isDark) PrimaryGreenVibrant.copy(alpha=0.2f) else PrimaryGreenLight)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.Info, contentDescription = null, tint = if(isDark) PrimaryGreenLight else PrimaryGreenVibrant)
            Spacer(Modifier.width(10.dp))
            Text(
                "Note: This meal plan is generated based on your profile. Tap refresh to generate a new plan.",
                color = if(isDark) PrimaryGreenLight.copy(alpha=0.8f) else PrimaryGreenVibrant,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun mealIcon(type: String) = when (type.lowercase()) {
    "breakfast" -> Icons.Default.BreakfastDining
    "lunch" -> Icons.Default.LunchDining
    "dinner" -> Icons.Default.DinnerDining
    "snack" -> Icons.Default.Fastfood
    else -> Icons.Default.Restaurant
}



