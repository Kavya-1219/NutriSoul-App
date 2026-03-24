package com.simats.nutrisoul

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.nutrisoul.data.UserViewModel
import com.simats.nutrisoul.ui.theme.*
import androidx.compose.ui.graphics.Brush

val healthConditionIcons = mapOf(
    "Diabetes" to "🩺",
    "PCOS" to "💊",
    "Thyroid Issues" to "🦋",
    "High Blood Pressure" to "❤️",
    "Low Blood Pressure" to "💙",
    "High Cholesterol" to "🫀",
    "Digestive Issues" to "🌿",
    "Anemia" to "🩸",
    "Food Allergies" to "⚠️"
)

@Composable
fun HealthDetailsScreen(navController: NavController, userViewModel: UserViewModel) {

    val user by userViewModel.user.collectAsState()
    val selectedHealthConditions = user?.healthConditions ?: emptyList()

    var systolic by remember { mutableStateOf("") }
    var diastolic by remember { mutableStateOf("") }
    var selectedThyroid by remember { mutableStateOf<String?>(null) }
    var selectedDiabetes by remember { mutableStateOf<String?>(null) }
    var cholesterol by remember { mutableStateOf("") }
    var selectedFoodAllergies by remember { mutableStateOf<List<String>>(emptyList()) }
    var otherAllergies by remember { mutableStateOf("") }

    val exceptionalConditions = listOf("None", "PCOS", "Digestive Issues", "Anemia")

    val shouldShowHealthDetails = selectedHealthConditions.any { it !in exceptionalConditions }

    LaunchedEffect(selectedHealthConditions) {
        if (!shouldShowHealthDetails && selectedHealthConditions.isNotEmpty()) {
            navController.navigate(Screen.MealsPerDay.route)
        }
    }

    val premiumPurple = PrimaryGreenVibrant
    val deepPurple = PrimaryGreenVibrant

    if (shouldShowHealthDetails) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(premiumPurple, deepPurple)))
        ) {
            Header(navController = navController)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(Color.White)
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (selectedHealthConditions.contains("High Blood Pressure")) {
                        BloodPressureSection(systolic, diastolic, "High", onValueChange = { s, d ->
                            systolic = s
                            diastolic = d
                        })
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    if (selectedHealthConditions.contains("Low Blood Pressure")) {
                        BloodPressureSection(systolic, diastolic, "Low", onValueChange = { s, d ->
                            systolic = s
                            diastolic = d
                        })
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    if (selectedHealthConditions.contains("Thyroid Issues")) {
                        ThyroidSection(selectedThyroid) { selectedThyroid = it }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    if (selectedHealthConditions.contains("Diabetes")) {
                        DiabetesSection(selectedDiabetes) { selectedDiabetes = it }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    if (selectedHealthConditions.contains("High Cholesterol")) {
                        CholesterolSection(cholesterol) { cholesterol = it }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    if (selectedHealthConditions.contains("Food Allergies")) {
                        FoodAllergySection(selectedFoodAllergies, otherAllergies) { sfa, oa ->
                            selectedFoodAllergies = sfa
                            otherAllergies = oa
                        }
                    }
                }
                Button(
                    onClick = {
                        user?.let { currentUser ->
                            val healthDetails = mutableListOf<String>()
                            if (systolic.isNotBlank() && diastolic.isNotBlank()) {
                                healthDetails.add("Blood Pressure: $systolic/$diastolic")
                            }
                            selectedThyroid?.let { healthDetails.add("Thyroid: $it") }
                            selectedDiabetes?.let { healthDetails.add("Diabetes Type: $it") }
                            if (cholesterol.isNotBlank()) {
                                healthDetails.add("Cholesterol: $cholesterol")
                            }
                            if (selectedFoodAllergies.isNotEmpty()) {
                                healthDetails.add("Food Allergies: ${selectedFoodAllergies.joinToString()}")
                            }
                            if (otherAllergies.isNotBlank()) {
                                healthDetails.add("Other Allergies: $otherAllergies")
                            }

                            val updatedHealthConditions = currentUser.healthConditions + healthDetails

                            val updatedUser = currentUser.copy(
                                healthConditions = updatedHealthConditions.distinct()
                            )
                            userViewModel.updateUser(updatedUser)
                            navController.navigate(Screen.MealsPerDay.route)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenVibrant)
                ) {
                    Text("Save & Continue", color = Color.White, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
private fun Header(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 24.dp)
    ) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painterResource(id = R.drawable.ic_health_details),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Health Details", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Help us personalize your nutrition plan",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun BloodPressureSection(
    systolic: String,
    diastolic: String,
    type: String,
    premiumPurple: Color = PrimaryGreenVibrant,
    onValueChange: (String, String) -> Unit
) {
    val icon = if (type == "High") healthConditionIcons["High Blood Pressure"] else healthConditionIcons["Low Blood Pressure"]
    val rangeText = if (type == "High") "Normal: <120/80 | High: ≥140/90" else "Normal: <120/80 | Low: <90/60"

    HealthDetailCard(icon = icon, title = "Blood Pressure Reading") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = systolic,
                onValueChange = { onValueChange(it, diastolic) },
                label = { Text("Systolic (top)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = premiumPurple,
                    focusedLabelColor = premiumPurple,
                    cursorColor = premiumPurple,
                    focusedTextColor = premiumPurple
                )
            )
            OutlinedTextField(
                value = diastolic,
                onValueChange = { onValueChange(systolic, it) },
                label = { Text("Diastolic (bottom)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = premiumPurple,
                    focusedLabelColor = premiumPurple,
                    cursorColor = premiumPurple,
                    focusedTextColor = premiumPurple
                )
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(rangeText, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
private fun ThyroidSection(selectedThyroid: String?, onSelect: (String) -> Unit) {
    val thyroidOptions = listOf(
        "Hypothyroidism (Underactive)",
        "Hyperthyroidism (Overactive)",
        "Hashimoto's",
        "Not sure"
    )

    HealthDetailCard(icon = healthConditionIcons["Thyroid Issues"], title = "Thyroid Condition") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            thyroidOptions.forEach { option ->
                SelectableButton(text = option, selected = selectedThyroid == option, modifier = Modifier.fillMaxWidth()) {
                    onSelect(option)
                }
            }
        }
    }
}

@Composable
private fun DiabetesSection(selectedDiabetes: String?, onSelect: (String) -> Unit) {
    val diabetesOptions = listOf("Type 1", "Type 2", "Prediabetes")

    HealthDetailCard(icon = healthConditionIcons["Diabetes"], title = "Diabetes Type") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SelectableButton(text = diabetesOptions[0], selected = selectedDiabetes == diabetesOptions[0], modifier = Modifier.weight(1f)) {
                onSelect(diabetesOptions[0])
            }
            SelectableButton(text = diabetesOptions[1], selected = selectedDiabetes == diabetesOptions[1], modifier = Modifier.weight(1f)) {
                onSelect(diabetesOptions[1])
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        SelectableButton(text = diabetesOptions[2], selected = selectedDiabetes == diabetesOptions[2], modifier = Modifier.fillMaxWidth()) {
            onSelect(diabetesOptions[2])
        }
    }
}

@Composable
private fun CholesterolSection(
    cholesterol: String, 
    premiumPurple: Color = PrimaryGreenVibrant,
    onValueChange: (String) -> Unit
) {
    HealthDetailCard(icon = healthConditionIcons["High Cholesterol"], title = "Cholesterol Level (mg/dL)") {
        OutlinedTextField(
            value = cholesterol,
            onValueChange = onValueChange,
            label = { Text("e.g., 220") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = premiumPurple,
                focusedLabelColor = premiumPurple,
                cursorColor = premiumPurple,
                focusedTextColor = premiumPurple
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("Normal: <200 | High: ≥240 mg/dL", fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
private fun FoodAllergySection(
    selectedFoodAllergies: List<String>,
    otherAllergies: String,
    premiumPurple: Color = PrimaryGreenVibrant,
    onValueChange: (List<String>, String) -> Unit
) {
    val allergyOptions = listOf(
        "Peanuts", "Shellfish", "Sesame",
        "Apollo Fish", "Milk/Dairy", "Eggs",
        "Soy", "Wheat/Gluten", "Fish"
    )

    HealthDetailCard(icon = healthConditionIcons["Food Allergies"], title = "Select Your Food Allergies") {
        Column {
            allergyOptions.chunked(3).forEach { rowOptions ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val remainingSpace = 3 - rowOptions.size
                    rowOptions.forEach { option ->
                        SelectableButton(
                            text = option,
                            selected = selectedFoodAllergies.contains(option),
                            onClick = {
                                val newSelection = if (selectedFoodAllergies.contains(option)) {
                                    selectedFoodAllergies - option
                                } else {
                                    selectedFoodAllergies + option
                                }
                                onValueChange(newSelection, otherAllergies)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (remainingSpace > 0) {
                        Spacer(modifier = Modifier.weight(remainingSpace.toFloat()))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        OutlinedTextField(
            value = otherAllergies,
            onValueChange = { onValueChange(selectedFoodAllergies, it) },
            label = { Text("Other allergies (optional)") },
            placeholder = { Text("e.g., Strawberries, Mustard") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = premiumPurple,
                focusedLabelColor = premiumPurple,
                cursorColor = premiumPurple,
                focusedTextColor = premiumPurple
            )
        )
    }
}


@Composable
private fun HealthDetailCard(icon: String?, title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Text(text = icon, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun SelectableButton(
    text: String, 
    selected: Boolean, 
    modifier: Modifier = Modifier, 
    onClick: () -> Unit
) {
    val premiumPurple = Color(0xFF8B5CF6)
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) premiumPurple else Color(0xFFF3F4F6),
            contentColor = if (selected) Color.White else Color(0xFF4B5563)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(text, fontWeight = if(selected) FontWeight.Bold else FontWeight.Medium)
    }
}



