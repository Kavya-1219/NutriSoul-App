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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.runtime.LaunchedEffect
import com.simats.nutrisoul.ui.theme.*
import com.simats.nutrisoul.composables.UnitSelector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Brush

@Composable
fun GoalWeightScreen(navController: NavController, userViewModel: UserViewModel) {
    var targetWeight by remember { mutableStateOf("60") }
    var weightUnit by remember { mutableStateOf("kg") }

    val user by userViewModel.user.collectAsState()

    LaunchedEffect(user) {
        user?.let { u ->
            if (u.targetWeight > 0) targetWeight = u.targetWeight.toInt().toString()
            // Assuming weightUnit is also stored in user or derived. For now, default to kg.
            // if (u.weightUnit.isNotEmpty()) weightUnit = u.weightUnit
            if (u.currentWeight > 0) { /* currentWeight is read-only in this screen usually */ }
        }
    }

    val premiumPurple = PrimaryGreenVibrant
    val deepPurple = PrimaryGreenVibrant

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(premiumPurple, deepPurple)))
    ) {
        if (user == null) {
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
            return@Box
        }

        val currentUser = user!!

    val currentWeight = currentUser.currentWeight.toInt()
    // targetWeight state is now managed above and initialized from user
    // var targetWeight by remember(currentUser.targetWeight) {
    //     mutableStateOf(if (currentUser.targetWeight != 0f) currentUser.targetWeight.toInt().toString() else "")
    // }
    val goals = currentUser.goals
    val isLoseWeight = goals.contains("Lose weight")
    val isGainWeight = goals.contains("Gain weight") || goals.contains("Gain muscle")
    val goalType = when {
        isLoseWeight -> "Lose weight"
        isGainWeight -> "Gain weight"
        else -> "Maintain weight"
    }

    var selectedTimelineWeeks by remember(currentUser.targetWeeks) {
        mutableStateOf(if (currentUser.targetWeeks != 0) currentUser.targetWeeks else 7)
    }

    val target = targetWeight.toIntOrNull()
    val weightDifference =
        when (goalType) {
            "Lose weight" -> if (target != null) (currentWeight - target).coerceAtLeast(0) else 0
            "Gain weight", "Gain muscle" -> if (target != null) (target - currentWeight).coerceAtLeast(0) else 0
            else -> 0
        }

    val estimatedWeeks = if (weightDifference > 0) {
        if (isLoseWeight) {
            (weightDifference / 0.5).toInt().coerceAtLeast(4) // Standard 0.5kg/week for Lose
        } else {
            (weightDifference / 0.4).toInt().coerceAtLeast(4) // Standard 0.4kg/week for Gain
        }
    } else 12

    val timelineOptions = remember(estimatedWeeks) {
        if (estimatedWeeks == 0) emptyList()
        else {
            val minWeeks = (estimatedWeeks * 0.8).toInt().coerceAtLeast(4)
            val maxWeeks = (estimatedWeeks * 1.5).toInt()
            listOf(
                TimelineInfo("${minWeeks} weeks", minWeeks, "Aggressive pace", if (isLoseWeight) "~1 kg/week" else "~0.75 kg/week", false, Color.White, Color(0xFFE5E7EB)),
                TimelineInfo("${estimatedWeeks} weeks", estimatedWeeks, "Recommended pace", if (isLoseWeight) "~0.5 kg/week" else "~0.4 kg/week", true, PrimaryGreenVibrant.copy(alpha = 0.08f), PrimaryGreenVibrant),
                TimelineInfo("${maxWeeks} weeks", maxWeeks, "Gradual pace", if (isLoseWeight) "~0.25 kg/week" else "~0.2 kg/week", false, Color.White, Color(0xFFE5E7EB))
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(premiumPurple, deepPurple)))
    ) {

        HeaderSection(navController)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color(0xFFF7F7F7))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                WeightInfoSection(
                    currentWeight = currentWeight,
                    targetWeight = targetWeight,
                    onTargetWeightChange = { targetWeight = it },
                    showDifference = (goalType == "Lose weight" || goalType == "Gain weight"),
                    goalType = goalType,
                    weightDifference = weightDifference,
                    weightUnit = weightUnit,
                    onUnitSelected = { weightUnit = it }
                )

                if (goalType == "Lose weight" || goalType == "Gain weight" || goalType == "Gain muscle") {
                    Spacer(modifier = Modifier.height(24.dp))
                    EstimatedTimelineCard(estimatedWeeks)
                    Spacer(modifier = Modifier.height(32.dp))
                    TimelineSection(
                        timelines = timelineOptions,
                        selectedWeeks = selectedTimelineWeeks,
                        onWeeksSelected = { selectedTimelineWeeks = it }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    SafetyTipCard()
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            ContinueButton(
                modifier = Modifier.padding(24.dp),
                enabled = targetWeight.isNotEmpty(),
                onClick = {
                    var targetVal = targetWeight.toFloatOrNull() ?: 0f
                    if (weightUnit == "lbs") {
                        targetVal *= 0.453592f
                    }
                    userViewModel.updateTargetWeight(targetVal, estimatedWeeks)
                    navController.navigate(Screen.HealthConditions.route)
                }
            )
        }
    }
}
}

@Composable
fun HeaderSection(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(painterResource(id = R.drawable.ic_target), contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Goal Weight", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Set your target weight", color = Color.White.copy(alpha = 0.9f), fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Step Indicator (Step 5)
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
                            if (index <= 4) Color.White else Color.White.copy(alpha = 0.4f),
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}


@Composable
fun WeightInfoSection(
    currentWeight: Int,
    targetWeight: String,
    onTargetWeightChange: (String) -> Unit,
    showDifference: Boolean,
    goalType: String,
    weightDifference: Int,
    weightUnit: String,
    onUnitSelected: (String) -> Unit
) {
    Column {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PrimaryGreenVibrant.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, PrimaryGreenVibrant.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Current Weight", color = Color.Gray, fontSize = 16.sp)
                Text("$currentWeight kg", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PrimaryGreenVibrant)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = targetWeight,
                onValueChange = onTargetWeightChange,
                modifier = Modifier.weight(1f),
                label = { Text("Target Weight") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreenVibrant,
                    focusedLabelColor = PrimaryGreenVibrant,
                    cursorColor = PrimaryGreenVibrant,
                    focusedTextColor = PrimaryGreenVibrant
                ),
                trailingIcon = {
                    Icon(Icons.AutoMirrored.Filled.TrendingDown, contentDescription = null, tint = PrimaryGreenVibrant)
                }
            )

            UnitSelector(
                units = listOf("kg", "lbs"),
                selectedUnit = weightUnit,
                onUnitSelected = onUnitSelected,
                activeColor = PrimaryGreenVibrant
            )
        }
        if (showDifference && weightDifference > 0) {
            Spacer(modifier = Modifier.height(20.dp))

            val label = if (goalType == "Lose weight") "Weight to lose" else "Weight to gain"

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF8B5CF6).copy(alpha = 0.05f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text(
                        "$weightDifference.0 kg",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreenVibrant
                    )
                }
            }
        }
    }
}

data class TimelineInfo(
    val duration: String,
    val weeks: Int,
    val pace: String,
    val rate: String,
    val recommended: Boolean,
    val containerColor: Color,
    val borderColor: Color
)

@Composable
fun TimelineSection(timelines: List<TimelineInfo>, selectedWeeks: Int, onWeeksSelected: (Int) -> Unit) {
    Text("Choose Your Timeline", fontWeight = FontWeight.Bold, fontSize = 18.sp)
    Spacer(modifier = Modifier.height(16.dp))

    timelines.forEach { timeline ->
        TimelineOptionCard(
            info = timeline,
            selected = selectedWeeks == timeline.weeks,
            onClick = { onWeeksSelected(timeline.weeks) }
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun EstimatedTimelineCard(weeks: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PrimaryGreenVibrant.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, PrimaryGreenVibrant.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = PrimaryGreenVibrant)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Estimated Timeline", fontWeight = FontWeight.Bold, color = PrimaryGreenVibrant)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("~$weeks weeks", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PrimaryGreenVibrant)
            Text("Based on healthy weight loss of 0.5 kg/week", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun TimelineOptionCard(
    info: TimelineInfo,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        border = BorderStroke(if (selected) 2.dp else 1.dp, info.borderColor),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = info.containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(info.duration, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (info.recommended) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(PrimaryGreenVibrant, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Recommended", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(info.pace, color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.TrendingDown, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                Text(info.rate, color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun SafetyTipCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFFEF3C7))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.padding(top=2.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Safe weight loss is 0.5–1 kg per week. Rapid changes can be harmful. Consult a doctor for personalized advice.",
                color = Color(0xFF92400E),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun ContinueButton(modifier: Modifier = Modifier, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryGreenVibrant,
            disabledContainerColor = PrimaryGreenVibrant.copy(alpha = 0.5f)
        )
    ) {
        Text("Continue", color = Color.White, fontSize = 18.sp)
    }
}
