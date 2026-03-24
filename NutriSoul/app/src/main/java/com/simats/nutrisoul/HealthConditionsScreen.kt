package com.simats.nutrisoul

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.nutrisoul.data.UserViewModel
import com.simats.nutrisoul.ui.theme.*

data class HealthCondition(val name: String, val description: String, val icon: String)

@Composable
fun HealthConditionsScreen(navController: NavController, userViewModel: UserViewModel) {

    val healthConditions = listOf(
        HealthCondition("None", "No health conditions", "✅"),
        HealthCondition("Diabetes", "Blood sugar management", "🩺"),
        HealthCondition("PCOS", "Hormonal balance", "💊"),
        HealthCondition("Thyroid Issues", "Thyroid regulation", "🦋"),
        HealthCondition("High Blood Pressure", "Blood pressure control", "❤️"),
        HealthCondition("Low Blood Pressure", "Blood pressure support", "💙"),
        HealthCondition("High Cholesterol", "Cholesterol management", "🫀"),
        HealthCondition("Digestive Issues", "Gut health", "🌿"),
        HealthCondition("Anemia", "Iron deficiency", "🩸"),
        HealthCondition("Food Allergies", "Allergy management", "⚠️")
    )

    val userState by userViewModel.user.collectAsState()
    var selectedConditions by remember(userState) {
        val initialSelection = if (userState?.healthConditions?.isNotEmpty() == true) {
            healthConditions.filter { userState?.healthConditions?.contains(it.name) == true }.toSet()
        } else {
            setOf(healthConditions.first())
        }
        mutableStateOf(initialSelection)
    }

    val premiumPurple = PrimaryGreenVibrant
    val deepPurple = PrimaryGreenVibrant

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(premiumPurple, deepPurple)))
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    Icons.Outlined.FavoriteBorder,
                    contentDescription = "Health",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Health Conditions",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Select any conditions you have",
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Step Indicator (Step 6)
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
                                if (index <= 5) Color.White else Color.White.copy(alpha = 0.4f),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        Card(
            modifier = Modifier.align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.78f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    healthConditions.forEach { condition ->
                        HealthConditionCard(
                            condition = condition,
                            isSelected = selectedConditions.contains(condition),
                            onClick = {
                                val newSelection = selectedConditions.toMutableSet()
                                if (condition.name == "None") {
                                    newSelection.clear()
                                    newSelection.add(condition)
                                } else {
                                    newSelection.removeIf { it.name == "None" }
                                    if (newSelection.contains(condition)) {
                                        newSelection.remove(condition)
                                    } else {
                                        newSelection.add(condition)
                                    }
                                    if (newSelection.isEmpty()) {
                                        newSelection.add(healthConditions.first())
                                    }
                                }
                                selectedConditions = newSelection
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryGreenVibrant.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, PrimaryGreenVibrant.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💡", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Your food and nutrition recommendations will be customized based on your health conditions.",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val conditionNames = selectedConditions.map { it.name }
                        
                        // Update the user model with selected conditions
                        userState?.let { currentUser ->
                            userViewModel.updateUser(currentUser.copy(healthConditions = conditionNames))
                        }

                        val requiresDetails = selectedConditions.any { 
                            it.name in listOf(
                                "High Blood Pressure", "Low Blood Pressure", 
                                "Thyroid Issues", "Diabetes", "High Cholesterol", "Food Allergies"
                            )
                        }

                        if (requiresDetails) {
                            navController.navigate(Screen.HealthDetails.route)
                        } else {
                            navController.navigate(Screen.MealsPerDay.route)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreenVibrant,
                        disabledContainerColor = PrimaryGreenVibrant.copy(alpha = 0.5f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    enabled = selectedConditions.isNotEmpty()
                ) {
                    Text("Continue", color = Color.White, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun HealthConditionCard(condition: HealthCondition, isSelected: Boolean, onClick: () -> Unit) {
    val premiumPurple = Color(0xFF8B5CF6)
    val borderColor = if (isSelected) premiumPurple else Color(0xFFE5E7EB)
    val containerColor = if (isSelected) premiumPurple.copy(alpha = 0.08f) else Color.White
    val titleColor = if (isSelected) Color(0xFF1F2937) else Color(0xFF4B5563)
    val subtitleColor = if (isSelected) Color(0xFF4B5563) else Color(0xFF9CA3AF)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = condition.icon, fontSize = 24.sp)

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = condition.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = titleColor
                )
                Text(
                    text = condition.description,
                    fontSize = 12.sp,
                    color = subtitleColor
                )
            }


            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(premiumPurple, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
