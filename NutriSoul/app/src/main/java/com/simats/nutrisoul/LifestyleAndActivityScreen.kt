package com.simats.nutrisoul

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.nutrisoul.data.UserViewModel
import com.simats.nutrisoul.ui.theme.*
import androidx.compose.ui.graphics.Brush

@Composable
fun LifestyleAndActivityScreen(navController: NavController, userViewModel: UserViewModel) {

    val activityLevels = listOf(
        ActivityLevel("Sedentary", "Little or no exercise, desk job", "Examples: Office work, studying, minimal movement"),
        ActivityLevel("Lightly Active", "Light exercise 1-3 days/week", "Examples: Walking, light housework, casual cycling"),
        ActivityLevel("Moderately Active", "Moderate exercise 3-5 days/week", "Examples: Regular gym, sports, active job"),
        ActivityLevel("Very Active", "Hard exercise 6-7 days/week", "Examples: Intense training, athletic activities")
    )

    var selectedLevel by remember { mutableStateOf<ActivityLevel?>(null) }

    val premiumPurple = PrimaryGreenVibrant
    val deepPurple = PrimaryGreenVibrant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(premiumPurple, deepPurple)))
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.MonitorHeart,
                contentDescription = "Lifestyle & Activity",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Lifestyle & Activity",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Your daily motion",
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Step Indicator (Step 4)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(6) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(
                                if (index <= 3) Color.White else Color.White.copy(alpha = 0.4f),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text("Select your typical activity level", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    activityLevels.forEach { level ->
                        ActivityLevelCard(
                            level = level,
                            isSelected = selectedLevel == level,
                            onClick = { selectedLevel = level }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Activity Impact Info Card
                ActivityInfoCard()

                Spacer(modifier = Modifier.height(24.dp))

                 Button(
                    onClick = {
                        selectedLevel?.let { level ->
                            userViewModel.user.value?.let { currentUser ->
                                userViewModel.updateUser(currentUser.copy(activityLevel = level.title))
                            }
                        }
                        navController.navigate(Screen.Goals.route)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                     shape = RoundedCornerShape(18.dp),
                     colors = ButtonDefaults.buttonColors(
                         containerColor = premiumPurple,
                         disabledContainerColor = premiumPurple.copy(alpha = 0.5f)
                     ),
                     elevation = ButtonDefaults.buttonElevation(
                         defaultElevation = 0.dp
                     ),
                    enabled = selectedLevel != null
                ) {
                    Text("Continue to Goals", color = Color.White, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

data class ActivityLevel(val title: String, val description: String, val examples: String)

@Composable
fun ActivityLevelCard(level: ActivityLevel, isSelected: Boolean, onClick: () -> Unit) {
    val premiumPurple = PrimaryGreenVibrant
    val borderColor = if (isSelected) premiumPurple else Color(0xFFE5E7EB)
    val containerColor = if (isSelected) premiumPurple.copy(alpha = 0.08f) else Color.White

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = getActivityLevelIcon(level.title)),
                contentDescription = level.title,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.size(16.dp))
            Column {
                Text(level.title, fontWeight = FontWeight.Bold)
                Text(level.description, color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(level.examples, color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

private fun getActivityLevelIcon(level: String): Int {
    return when (level) {
        "Sedentary" -> R.drawable.la1
        "Lightly Active" -> R.drawable.la2
        "Moderately Active" -> R.drawable.la3
        "Very Active" -> R.drawable.la4
        else -> R.drawable.la1
    }
}
@Composable
fun ActivityInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF2F8)),
        border = BorderStroke(1.dp, Color(0xFFFBCFE8))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.Lightbulb,
                contentDescription = null,
                tint = Color(0xFFDB2777),
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    "Did you know?",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9D174D),
                    fontSize = 14.sp
                )
                Text(
                    "Your activity level accounts for 15-30% of your daily calorie needs. Choosing the right level helps us calculate your TDEE (Total Daily Energy Expenditure) accurately.",
                    fontSize = 12.sp,
                    color = Color(0xFFBE185D),
                    lineHeight = 18.sp
                )
            }
        }
    }
}



