package com.simats.nutrisoul

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.ui.graphics.Brush
import com.simats.nutrisoul.ui.theme.*
import com.simats.nutrisoul.composables.UnitSelector
import com.simats.nutrisoul.data.UserViewModel
import com.simats.nutrisoul.data.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyDetailsScreen(navController: NavHostController, userViewModel: UserViewModel) {

    var height by remember { mutableStateOf("161") }
    var weight by remember { mutableStateOf("65") }
    var heightUnit by remember { mutableStateOf("cm") }
    var weightUnit by remember { mutableStateOf("kg") }

    val bmi = calculateBmi(height, weight, heightUnit, weightUnit)
    val bmiCategory = getBmiCategory(bmi)
    val bmiProgress = (bmi.toFloat() / 40f).coerceIn(0f, 1f)
    val bmiColor = getBmiColor(bmiCategory)

    val userState by userViewModel.user.collectAsState()

    LaunchedEffect(userState) {
        userState?.let { u ->
            if (u.height > 0) height = u.height.toInt().toString()
            if (u.weight > 0) weight = u.weight.toInt().toString()
        }
    }


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
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.Balance,
                contentDescription = "Body Details",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))


            Text(
                "Body Details",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Help us calculate your nutrition needs",
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Step Indicator (Step 2)
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
                                if (index <= 1) Color.White else Color.White.copy(alpha = 0.4f),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        Card(
            modifier = Modifier
                .fillMaxSize(),
            shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                /* ---------------- HEIGHT ---------------- */
                Text("Height", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Enter height", color = Color.Gray.copy(alpha=0.6f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = premiumPurple,
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            cursorColor = premiumPurple
                        )
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    UnitSelector(
                        units = listOf("cm", "ft"),
                        selectedUnit = heightUnit,
                        onUnitSelected = { heightUnit = it },
                        activeColor = premiumPurple
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                /* ---------------- WEIGHT ---------------- */
                Text("Current Weight", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Enter weight", color = Color.Gray.copy(alpha=0.6f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = premiumPurple,
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            cursorColor = premiumPurple
                        )
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    UnitSelector(
                        units = listOf("kg", "lbs"),
                        selectedUnit = weightUnit,
                        onUnitSelected = { weightUnit = it },
                        activeColor = premiumPurple
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                /* ---------------- BMI CARD ---------------- */
                BmiCard(bmi, bmiCategory, bmiProgress, bmiColor)

                Spacer(modifier = Modifier.height(16.dp))

                /* ---------------- BMR INFO CARD ---------------- */
                BmrInfoCard(premiumPurple)


                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        val currentUser = userViewModel.user.value
                        var hValue = height.toFloatOrNull() ?: 0.0f
                        var wValue = weight.toFloatOrNull() ?: 0.0f

                        // Convert to Metric (cm/kg) for backend/calculations
                        if (heightUnit == "ft") {
                            hValue *= 30.48f
                        }
                        if (weightUnit == "lbs") {
                            wValue *= 0.453592f
                        }
                        
                        val updatedUser = currentUser?.copy(
                            height = hValue,
                            weight = wValue,
                            currentWeight = wValue
                        ) ?: User(
                            email = "", 
                            height = hValue,
                            weight = wValue,
                            currentWeight = wValue
                        )
                        userViewModel.updateUser(updatedUser)
                        navController.navigate(Screen.FoodPreferences.route)
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
                    )
                ) {
                    Text("Continue", color = Color.White, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

/* ---------------- UNIT SELECTOR ---------------- */


/* ---------------- BMI CARD ---------------- */

@Composable
fun BmiCard(bmi: Double, category: String, progress: Float, bmiColor: Color) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(Color(0xFFF3F8FF)),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Color(0xFFD3E6FF))

    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Your BMI", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.DarkGray)
                Text(category, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = bmiColor,
                    trackColor = Color(0xFFE5E7EB)
                )
            }
            Text(
                String.format("%.1f", bmi),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = bmiColor
            )
        }
    }
}

@Composable
fun BmrInfoCard(premiumPurple: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(premiumPurple.copy(alpha = 0.05f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = Icons.Outlined.Lightbulb,
                contentDescription = "Info",
                tint = premiumPurple,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                "Your height and weight help us calculate your BMR (Basal Metabolic Rate) and daily calorie requirements.",
                fontSize = 14.sp,
                color = Color.DarkGray
            )
        }
    }
}


fun calculateBmi(height: String, weight: String, heightUnit: String, weightUnit: String): Double {
    val heightValue = height.toDoubleOrNull() ?: 0.0
    val weightValue = weight.toDoubleOrNull() ?: 0.0

    if (heightValue == 0.0 || weightValue == 0.0) {
        return 0.0
    }

    val heightInMeters = if (heightUnit == "cm") {
        heightValue / 100
    } else { // ft
        heightValue * 0.3048
    }

    val weightInKg = if (weightUnit == "kg") {
        weightValue
    } else { // lbs
        weightValue * 0.453592
    }

    return weightInKg / (heightInMeters * heightInMeters)
}

fun getBmiCategory(bmi: Double): String {
    return when {
        bmi < 18.5 -> "Underweight"
        bmi < 25 -> "Normal"
        bmi < 30 -> "Overweight"
        else -> "Obese"
    }
}

@Composable
fun getBmiColor(category: String): Color {
    return when (category) {
        "Underweight" -> Color(0xFF3B82F6)
        "Normal" -> Color(0xFF10B981)
        "Overweight" -> Color(0xFFFFA500) // Orange
        "Obese" -> Color.Red
        else -> Color.Gray
    }
}
