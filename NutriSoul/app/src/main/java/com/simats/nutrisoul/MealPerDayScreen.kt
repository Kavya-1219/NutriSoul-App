package com.simats.nutrisoul

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BreakfastDining
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.EmojiFoodBeverage
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.nutrisoul.data.UserViewModel


@Composable
fun MealPerDayScreen(navController: NavController, userViewModel: UserViewModel) {

    var mealCount by remember { mutableStateOf(3) }

    val mealPatterns = mapOf(
        2 to listOf(MealOption("Breakfast", Icons.Default.BreakfastDining), MealOption("Dinner", Icons.Default.LunchDining)),
        3 to listOf(MealOption("Breakfast", Icons.Default.BreakfastDining), MealOption("Lunch", Icons.Default.LunchDining), MealOption("Dinner", Icons.Default.EmojiFoodBeverage)),
        4 to listOf(MealOption("Breakfast", Icons.Default.BreakfastDining), MealOption("Lunch", Icons.Default.LunchDining), MealOption("Snack", Icons.Default.Cake), MealOption("Dinner", Icons.Default.EmojiFoodBeverage)),
        5 to listOf(MealOption("Breakfast", Icons.Default.BreakfastDining), MealOption("Snack", Icons.Default.Cake), MealOption("Lunch", Icons.Default.LunchDining), MealOption("Snack", Icons.Default.Cake), MealOption("Dinner", Icons.Default.EmojiFoodBeverage)),
        6 to listOf(MealOption("Early Breakfast", Icons.Default.BreakfastDining), MealOption("Breakfast", Icons.Default.BreakfastDining), MealOption("Mid-Morning Snack", Icons.Default.Cake), MealOption("Lunch", Icons.Default.LunchDining), MealOption("Evening Snack", Icons.Default.Cake), MealOption("Dinner", Icons.Default.EmojiFoodBeverage))
    )

    val infoTexts = mapOf(
        2 to "Eating 2 meals might suit a specific eating window or intermittent fasting.",
        3 to "Eating 3 meals helps maintain steady energy and metabolism throughout the day.",
        4 to "Adding a snack can help manage hunger and prevent overeating at main meals.",
        5 to "Frequent small meals can help maintain blood sugar levels and consistent energy.",
        6 to "Six small meals daily can sustain metabolism and keep your energy levels peak."
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                )
            )
            .verticalScroll(rememberScrollState())
    ) {

        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }
            Icon(Icons.Default.Star, contentDescription = "Almost Done", tint = Color.White, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Almost Done!", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text("One last preference to personalize your plan", color = Color.White.copy(alpha = 0.9f))
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Main Content Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Meals Per Day", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                Text("How many times do you prefer to eat daily?", color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(20.dp))

                // Meal Counter
                MealCounter(mealCount) { newCount ->
                    if (newCount in 2..6) mealCount = newCount
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Meal Pattern
                MealPatternCard(mealPatterns[mealCount] ?: emptyList())
                Spacer(modifier = Modifier.height(16.dp))

                // Info Box
                InfoCard(infoTexts[mealCount] ?: "")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Plan Ready Card
        PlanReadyCard(navController, mealCount, userViewModel)

        Spacer(modifier = Modifier.height(32.dp))
    }
}

data class MealOption(val name: String, val icon: ImageVector)

@Composable
fun MealCounter(count: Int, onCountChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        IconButton(
            onClick = { onCountChange(count - 1) },
            modifier = Modifier.clip(CircleShape).background(Color(0xFFF0F0F0))
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease meals", tint = Color.DarkGray)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$count", fontSize = 36.sp, fontWeight = FontWeight.Bold)
            Text("meals", fontSize = 14.sp, color = Color.Gray)
        }
        IconButton(
            onClick = { onCountChange(count + 1) },
            modifier = Modifier.clip(CircleShape).background(Color(0xFFF0F0F0))

        ) {
            Icon(Icons.Default.Add, contentDescription = "Increase meals", tint = Color.DarkGray)
        }
    }
}

@Composable
fun MealPatternCard(meals: List<MealOption>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Your Meal Pattern", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                meals.forEach { meal ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = meal.icon,
                            contentDescription = meal.name,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(meal.name, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun InfoCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        border = BorderStroke(1.dp, Color(0xFFC8E6C9))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.padding(end = 12.dp))
            Text(text, fontSize = 13.sp, color = Color(0xFF388E3C))
        }
    }
}

@Composable
fun PlanReadyCard(navController: NavController, mealCount: Int, userViewModel: UserViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF7E57C2))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Your Plan is Ready!", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "We've created a personalized nutrition plan based on your goals, health conditions, and lifestyle. Let's get started!",
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    userViewModel.user.value?.let { currentUser ->
                        val updatedUser = currentUser.copy(mealsPerDay = mealCount)
                        userViewModel.updateUser(updatedUser)
                        navController.navigate(Screen.Home.route) { popUpTo(Screen.Splash.route) { inclusive = true } }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Start Your Journey", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}



