package com.simats.nutrisoul

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.nutrisoul.data.User
import com.simats.nutrisoul.data.UserViewModel
import com.simats.nutrisoul.ui.theme.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.Brush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalDetailsScreen(
    onNavigateBack: () -> Unit,
    onContinueClicked: () -> Unit,
    userViewModel: UserViewModel
) {
    var fullName by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("") }

    val ageInt = age.toIntOrNull()
    val isAgeValid = ageInt != null && ageInt in 10..120

    val isContinueEnabled =
        fullName.isNotBlank() && isAgeValid && selectedGender.isNotEmpty()

    val premiumPurple = PrimaryGreenVibrant
    val deepPurple = PrimaryGreenVibrant

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(premiumPurple, deepPurple)))
    ) {

        // Top section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }

            Text(
                text = "Personal Details",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Let's get to know you",
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Step Indicator
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
                                if (index <= 0) Color.White else Color.White.copy(alpha = 0.4f),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
        }

        // Bottom Card
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.75f),
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

                Text("Full Name *", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter your full name", color = Color.Gray.copy(alpha=0.6f)) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = premiumPurple,
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        cursorColor = premiumPurple
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Age *", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = age,
                    onValueChange = { if (it.length <= 3) age = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter your age (10-120)", color = Color.Gray.copy(alpha=0.6f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = age.isNotEmpty() && !isAgeValid,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = premiumPurple,
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        cursorColor = premiumPurple,
                        errorBorderColor = Color.Red
                    )
                )
                if (age.isNotEmpty() && !isAgeValid) {
                    Text(
                        text = "Please enter a valid age (10-120)",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text("Gender *", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GenderButton(
                        text = "Male",
                        selected = selectedGender == "Male",
                        onClick = { selectedGender = "Male" },
                        modifier = Modifier.weight(1f),
                        activeColor = premiumPurple
                    )
                    GenderButton(
                        text = "Female",
                        selected = selectedGender == "Female",
                        onClick = { selectedGender = "Female" },
                        modifier = Modifier.weight(1f),
                        activeColor = premiumPurple
                    )
                    GenderButton(
                        text = "Other",
                        selected = selectedGender == "Other",
                        onClick = { selectedGender = "Other" },
                        modifier = Modifier.weight(1f),
                        activeColor = premiumPurple
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Motivational Message from Figma
                MotivationalCard()

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        val currentUser = userViewModel.user.value
                        val updatedUser = currentUser?.copy(
                            name = fullName,
                            age = age.toIntOrNull() ?: 0,
                            gender = selectedGender
                        ) ?: User(
                            email = userViewModel.userName.value.ifBlank { "" }, // Use placeholder or logged in email
                            name = fullName,
                            age = age.toIntOrNull() ?: 0,
                            gender = selectedGender
                        )
                        userViewModel.updateUser(updatedUser)
                        onContinueClicked()
                    },
                    enabled = isContinueEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = premiumPurple,
                        disabledContainerColor = premiumPurple.copy(alpha = 0.5f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    )
                ) {
                    Text("Continue", color = Color.White, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun GenderButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(16.dp),
        border = if (selected) BorderStroke(2.dp, activeColor) else BorderStroke(1.dp, Color(0xFFE5E7EB)),
        colors = if (selected) ButtonDefaults.outlinedButtonColors(containerColor = activeColor.copy(alpha = 0.08f)) else ButtonDefaults.outlinedButtonColors()
    ) {
        Text(
            text = text,
            color = if (selected) activeColor else Color(0xFF4B5563),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
@Composable
fun MotivationalCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FF)),
        border = BorderStroke(1.dp, Color(0xFFBFDBFE))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text("💪", fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    "You're Starting Your Journey!",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A),
                    fontSize = 14.sp
                )
                Text(
                    "Every great achievement starts with the decision to try. We're here to support you every step of the way!",
                    fontSize = 12.sp,
                    color = Color(0xFF1E40AF),
                    lineHeight = 18.sp
                )
            }
        }
    }
}



