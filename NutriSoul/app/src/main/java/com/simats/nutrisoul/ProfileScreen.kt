package com.simats.nutrisoul

import android.net.Uri
import com.simats.nutrisoul.R
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.simats.nutrisoul.data.UserViewModel
import com.simats.nutrisoul.ui.theme.LocalDarkTheme

@Composable
fun ProfileScreen(navController: NavController, userViewModel: UserViewModel) {
    val user by userViewModel.user.collectAsStateWithLifecycle()
    var isEditing by remember { mutableStateOf(false) }
    var showPasswordChange by remember { mutableStateOf(false) }
    val isDark = LocalDarkTheme.current

    // Local state for editing fields
    var name by remember(user) { mutableStateOf(user?.name ?: "") }
    var gender by remember(user) { mutableStateOf(user?.gender ?: "") }
    var age by remember(user) { mutableStateOf(user?.age?.toString() ?: "") }
    var height by remember(user) { mutableStateOf(user?.height?.toString() ?: "") }
    var weight by remember(user) { mutableStateOf(user?.weight?.toString() ?: "") }
    var goalWeight by remember(user) { mutableStateOf(user?.targetWeight?.toString() ?: "") }
    var fitnessGoal by remember(user) { mutableStateOf(user?.goals?.joinToString(", ") ?: "") }
    var dietType by remember(user) { mutableStateOf(user?.dietType ?: "") }
    var foodAllergies by remember(user) { mutableStateOf(user?.foodAllergies?.joinToString(", ") ?: "") }
    var foodDislikes by remember(user) { mutableStateOf(user?.foodDislikes ?: "") }
    var activityLevel by remember(user) { mutableStateOf(user?.activityLevel ?: "") }
    var healthConditions by remember(user) { mutableStateOf(user?.healthConditions?.joinToString(", ") ?: "") }
    var systolic by remember(user) { mutableStateOf(user?.systolic?.toString() ?: "") }
    var diastolic by remember(user) { mutableStateOf(user?.diastolic?.toString() ?: "") }
    var thyroid by remember(user) { mutableStateOf(user?.thyroidCondition ?: "") }
    var diabetes by remember(user) { mutableStateOf(user?.diabetesType ?: "") }
    var cholesterol by remember(user) { mutableStateOf(user?.cholesterolLevel ?: "") }
    var mealsPerDay by remember(user) { mutableStateOf(user?.mealsPerDay?.toString() ?: "") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { userViewModel.setProfilePictureUri(it) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            ProfileHeader(
                onBack = { navController.popBackStack() },
                profileImageUrl = user?.profilePictureUrl,
                onEditImage = { imagePickerLauncher.launch("image/*") }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = (-60).dp)
                    .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // User Details Section
                CardPremium(title = "Personal Information", icon = Icons.Default.Person) {
                    EditableField("Full Name", name, isEditing) { name = it }
                    EditableField("Gender", gender, isEditing) { gender = it }
                    EditableField("Age", age, isEditing, KeyboardType.Number) { age = it }
                }

                // Body Attributes
                CardPremium(title = "Body Attributes", icon = Icons.Default.Straighten) {
                    EditableField("Height (cm)", height, isEditing, KeyboardType.Number) { height = it }
                    EditableField("Current Weight (kg)", weight, isEditing, KeyboardType.Number) { weight = it }
                    EditableField("Goal Weight (kg)", goalWeight, isEditing, KeyboardType.Number) { goalWeight = it }
                    
                    if (!isEditing && user != null && user!!.height > 0) {
                        val bmi = user!!.weight / ((user!!.height / 100f) * (user!!.height / 100f))
                        Text(
                            text = "Calculated BMI: ${String.format("%.1f", bmi)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF10B981),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                // Diet & Preferences
                CardPremium(title = "Dietary Preferences", icon = Icons.Default.Restaurant) {
                    EditableField("Diet Type (e.g. Vegan, Keto)", dietType, isEditing) { dietType = it }
                    EditableField("Food Allergies", foodAllergies, isEditing) { foodAllergies = it }
                    EditableField("Food Dislikes", foodDislikes, isEditing) { foodDislikes = it }
                    EditableField("Meals Per Day", mealsPerDay, isEditing, KeyboardType.Number) { mealsPerDay = it }
                }

                // Lifestyle & Goals
                CardPremium(title = "Lifestyle & Goals", icon = Icons.Default.FitnessCenter) {
                    EditableField("Activity Level", activityLevel, isEditing) { activityLevel = it }
                    EditableField("Primary Fitness Goal", fitnessGoal, isEditing) { fitnessGoal = it }
                }

                // Health Conditions
                CardPremium(title = "Health Records", icon = Icons.Default.MedicalServices) {
                    EditableField("Health Conditions", healthConditions, isEditing) { healthConditions = it }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            EditableField("Systolic BP", systolic, isEditing, KeyboardType.Number) { systolic = it }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            EditableField("Diastolic BP", diastolic, isEditing, KeyboardType.Number) { diastolic = it }
                        }
                    }
                    EditableField("Thyroid Condition", thyroid, isEditing) { thyroid = it }
                    EditableField("Diabetes Type", diabetes, isEditing) { diabetes = it }
                    EditableField("Cholesterol Level", cholesterol, isEditing) { cholesterol = it }
                }

                // Action Buttons
                if (isEditing) {
                    Button(
                        onClick = {
                            user?.let { currentUser ->
                                userViewModel.updateUser(
                                    currentUser.copy(
                                        name = name,
                                        gender = gender,
                                        age = age.toIntOrNull() ?: 0,
                                        height = height.toFloatOrNull() ?: 0f,
                                        weight = weight.toFloatOrNull() ?: 0f,
                                        targetWeight = goalWeight.toFloatOrNull() ?: 0f,
                                        goals = fitnessGoal.split(",").map { it.trim() }.filter { it.isNotBlank() },
                                        dietType = dietType,
                                        foodAllergies = foodAllergies.split(",").map { it.trim() }.filter { it.isNotBlank() },
                                        foodDislikes = foodDislikes,
                                        activityLevel = activityLevel,
                                        healthConditions = healthConditions.split(",").map { it.trim() }.filter { it.isNotBlank() },
                                        systolic = systolic.toIntOrNull() ?: 0,
                                        diastolic = diastolic.toIntOrNull() ?: 0,
                                        thyroidCondition = thyroid,
                                        diabetesType = diabetes,
                                        cholesterolLevel = cholesterol,
                                        mealsPerDay = mealsPerDay.toIntOrNull() ?: 0
                                    )
                                )
                            }
                            isEditing = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669))),
                                    RoundedCornerShape(20.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Save Changes", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { isEditing = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(2.dp, Color(0xFF10B981))
                    ) {
                        Text("Edit Profile", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                // Password Section
                CardPremium(title = "Security", icon = Icons.Default.Lock) {
                    TextButton(onClick = { showPasswordChange = true }) {
                        Text("Change Password", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }

        if (showPasswordChange) {
            ChangePasswordDialog(
                onDismiss = { showPasswordChange = false },
                onPasswordChanged = { old, new ->
                    userViewModel.changePassword(old, new)
                    showPasswordChange = false
                }
            )
        }
    }
}

@Composable
private fun ProfileHeader(onBack: () -> Unit, profileImageUrl: String?, onEditImage: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF10B981), Color(0xFF059669))
                )
            )
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(Modifier.width(12.dp))
                Text("Your Profile", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            }

            Spacer(Modifier.height(16.dp))

            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier.size(110.dp),
                    shape = CircleShape,
                    border = BorderStroke(4.dp, Color.White),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.nutrisoul),
                        placeholder = painterResource(id = R.drawable.nutrisoul)
                    )
                }
                IconButton(
                    onClick = onEditImage,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFF10B981), CircleShape)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Edit Portrait", tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun CardPremium(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun EditableField(label: String, value: String, isEditing: Boolean, keyboardType: KeyboardType = KeyboardType.Text, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        if (isEditing) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF10B981),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        } else {
            Text(
                text = value.ifEmpty { "Not set" },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
            Divider(modifier = Modifier.padding(top = 8.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun ChangePasswordDialog(onDismiss: () -> Unit, onPasswordChanged: (String, String) -> Unit) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text("Current Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (newPassword == confirmPassword) onPasswordChanged(oldPassword, newPassword) },
                enabled = oldPassword.isNotEmpty() && newPassword.isNotEmpty() && newPassword == confirmPassword,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("UPDATE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}



