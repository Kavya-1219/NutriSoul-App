package com.simats.nutrisoul

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.graphics.Brush
import com.simats.nutrisoul.ui.theme.*

@Composable
fun FoodPreferencesScreen(navController: NavController, userViewModel: UserViewModel) {
    var selectedDiet by remember { mutableStateOf<String?>(null) }
    var selectedAllergies by remember { mutableStateOf(emptySet<String>()) }
    var foodDislikes by remember { mutableStateOf("") }

    val premiumPurple = PrimaryGreenVibrant
    val deepPurple = PrimaryGreenVibrant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(premiumPurple, deepPurple)))
    ) {
        Header(onBackClicked = { navController.popBackStack() })
        Card(
            modifier = Modifier.fillMaxSize(),
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
                DietTypeSection(selectedDiet) { selectedDiet = it }
                Spacer(modifier = Modifier.height(24.dp))
                FoodAllergiesSection(selectedAllergies) { selectedAllergies = it }
                Spacer(modifier = Modifier.height(24.dp))
                FoodDislikesSection(foodDislikes) { foodDislikes = it }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { 
                        userViewModel.user.value?.let { currentUser ->
                            val updatedUser = currentUser.copy(
                                dietaryRestrictions = selectedDiet?.let { listOf(it) } ?: emptyList(),
                                foodAllergies = selectedAllergies.toList(),
                                dislikes = foodDislikes.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            )
                            userViewModel.updateUser(updatedUser)
                        }
                        navController.navigate(Screen.LifestyleAndActivity.route)
                     },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = premiumPurple
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text("Continue", color = Color.White, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun Header(onBackClicked: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onBackClicked,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_fork_knife),
            contentDescription = "Food Preferences",
            modifier = Modifier.size(40.dp),
            tint = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Food Preferences",
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Your diet, your rules",
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Step Indicator (Step 3)
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
                            if (index <= 2) Color.White else Color.White.copy(alpha = 0.4f),
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun DietTypeSection(selectedDiet: String?, onSelect: (String) -> Unit) {
    val dietTypes = listOf(
        "Vegetarian" to "Plant-based diet",
        "Non-Vegetarian" to "Includes meat & fish",
        "Eggetarian" to "Veg + Eggs",
        "Vegan" to "No animal products"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Diet Type", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            dietTypes.subList(0, 2).forEach { (diet, description) ->
                DietTypeCard(diet, description, diet == selectedDiet) { onSelect(diet) }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            dietTypes.subList(2, 4).forEach { (diet, description) ->
                DietTypeCard(diet, description, diet == selectedDiet) { onSelect(diet) }
            }
        }
    }
}

@Composable
private fun RowScope.DietTypeCard(diet: String, description: String, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .weight(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) PrimaryGreenVibrant.copy(alpha = 0.08f) else Color.White),
        border = BorderStroke(1.dp, if (isSelected) PrimaryGreenVibrant else Color(0xFFE5E7EB))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = getDietIcon(diet)),
                contentDescription = diet,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(diet, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(description, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun getDietIcon(diet: String): Int {
    return when (diet) {
        "Vegetarian" -> R.drawable.vegetarian
        "Non-Vegetarian" -> R.drawable.non_vegetarian
        "Eggetarian" -> R.drawable.eggetarian
        "Vegan" -> R.drawable.vegan
        else -> R.drawable.vegetarian
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun FoodAllergiesSection(selectedAllergies: Set<String>, onSelect: (Set<String>) -> Unit) {
    val allergies = listOf("Nuts", "Dairy/Milk", "Gluten", "Eggs", "Soy", "Shellfish", "Fish", "Peanuts")

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Food Allergies", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            allergies.forEach { allergy ->
                FilterChip(
                    selected = selectedAllergies.contains(allergy),
                    onClick = {
                        val newSelection = if (selectedAllergies.contains(allergy)) {
                            selectedAllergies - allergy
                        } else {
                            selectedAllergies + allergy
                        }
                        onSelect(newSelection)
                    },
                    label = { Text(allergy, color = if(selectedAllergies.contains(allergy)) PrimaryGreenVibrant else Color(0xFF4B5563)) },
                    shape = RoundedCornerShape(14.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryGreenVibrant.copy(alpha = 0.1f),
                        selectedLabelColor = PrimaryGreenVibrant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = Color(0xFFE5E7EB),
                        selectedBorderColor = PrimaryGreenVibrant
                    )
                )
            }
        }
    }
}

@Composable
private fun FoodDislikesSection(foodDislikes: String, onValueChange: (String) -> Unit) {

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Food Dislikes (Optional)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = foodDislikes,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("E.g., Bitter gourd, Mushrooms, Broccoli", color = Color.Gray.copy(alpha=0.6f)) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryGreenVibrant,
                unfocusedBorderColor = Color(0xFFE5E7EB),
                cursorColor = PrimaryGreenVibrant
            )
        )
    }
}
