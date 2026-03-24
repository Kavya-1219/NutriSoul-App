package com.simats.nutrisoul

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.simats.nutrisoul.ui.theme.*

@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val state by viewModel.historyData.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(StepsGradientStart, StepsGradientEnd)
                                )
                            )
                            .padding(horizontal = 20.dp, vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { navController.popBackStack() },
                                    modifier = Modifier.background(Color.White.copy(alpha = 0.2f), androidx.compose.foundation.shape.CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = "History",
                                    color = Color.White,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Text(
                                text = "Your nutrition journey",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 16.sp,
                                modifier = Modifier.padding(start = 56.dp, top = 6.dp)
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Summary Stats card (glass look)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.18f)),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    StatBlock(
                                        title = "Days Logged",
                                        value = state.daysLogged.toString()
                                    )
                                    StatBlock(
                                        title = "Total Meals",
                                        value = state.totalMeals.toString()
                                    )
                                }
                            }
                        }
                    }
                }

                if (state.dayLogs.isEmpty()) {
                     item {
                         Box(modifier = Modifier.fillMaxWidth().height(200.dp).offset(y = (-40).dp), contentAlignment = Alignment.Center) {
                             Text("No history found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                         }
                     }
                } else {
                    items(state.dayLogs) { day ->
                        Box(modifier = Modifier.padding(horizontal = 18.dp).padding(bottom = 14.dp).offset(y = (-40).dp)) {
                            DayHistoryCard(day)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBlock(title: String, value: String) {
    Column {
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DayHistoryCard(day: DayLogUi) {
    val isDark = LocalDarkTheme.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            // Date header + calories
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = SuccessGreen
                )
                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = day.label, // Today / Yesterday / Date
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Outlined.LocalFireDepartment,
                    contentDescription = null,
                    tint = WarningOrange
                )
                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "${day.totalCalories}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "kcal",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            // Macros chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MacroChip(
                    title = "Protein",
                    value = "${day.totalProtein}g",
                    bg = if(isDark) WaterBlueDark.copy(alpha=0.4f) else WaterBlue,
                    fg = if(isDark) Color(0xFF93C5FD) else WaterBlueDark,
                    modifier = Modifier.weight(1f)
                )
                MacroChip(
                    title = "Carbs",
                    value = "${day.totalCarbs}g",
                    bg = if(isDark) CalorieOrange.copy(alpha=0.4f) else CalorieOrangeLight,
                    fg = if(isDark) Color(0xFFFDBA74) else CalorieOrange,
                    modifier = Modifier.weight(1f)
                )
                MacroChip(
                    title = "Fats",
                    value = "${day.totalFats}g",
                    bg = if(isDark) Color(0xFF78350F).copy(alpha=0.4f) else Color(0xFFFFFBEB),
                    fg = if(isDark) Color(0xFFFCD34D) else Color(0xFFCA8A04),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "LOGGED FOODS (${day.foods.size})",
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            day.foods.forEachIndexed { index, food ->
                if (index != 0) Divider(color = MaterialTheme.colorScheme.outlineVariant)
                FoodRow(food)
            }
        }
    }
}

@Composable
private fun MacroChip(
    title: String,
    value: String,
    bg: Color,
    fg: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .padding(vertical = 10.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = fg)
        }
    }
}

@Composable
private fun FoodRow(food: LoggedFoodUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Eco,
            contentDescription = null,
            tint = SuccessGreen
        )
        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = food.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = food.quantity,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${food.calories} kcal",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = food.time,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
