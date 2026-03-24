package com.simats.nutrisoul

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.simats.nutrisoul.ui.theme.PrimaryGreen

private val ErrorRed = Color(0xFFB00020)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController, authViewModel: AuthViewModel = hiltViewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    val authState by authViewModel.authState.collectAsState()

    when (authState) {
        is AuthState.Authenticated -> {
            AlertDialog(
                onDismissRequest = { /* Prevent dismissing by clicking outside */ },
                containerColor = Color.White,
                title = {
                    Text(
                        "Welcome to NutriSoul 🎉",
                        style = MaterialTheme.typography.titleLarge,
                        color = PrimaryGreen
                    )
                },
                text = {
                    Text("Your account has been created successfully. Let's begin your health journey.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            authViewModel.resetAuthState()
                            navController.navigate(Screen.PersonalDetails.route) {
                                popUpTo(Screen.Register.route) {
                                    inclusive = true
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Text("Continue", color = Color.White)
                    }
                }
            )
        }
        is AuthState.Error -> {
            val error = (authState as AuthState.Error).message
            AlertDialog(
                onDismissRequest = { authViewModel.resetAuthState() },
                containerColor = Color.White,
                title = { Text("Registration Failed") },
                text = { Text(error) },
                confirmButton = {
                    Button(
                        onClick = { authViewModel.resetAuthState() },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Text("OK", color = Color.White)
                    }
                }
            )
        }
        else -> Unit
    }

    val isEmailValid = Validation.isEmailValid(email)
    val isPasswordValid = Validation.isPasswordValid(password)
    val doPasswordsMatch = password == confirmPassword
    val isButtonEnabled = isEmailValid && isPasswordValid && doPasswordsMatch && authState !is AuthState.Loading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryGreen)
    ) {
        if (authState is AuthState.Loading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 4.dp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        IconButton(onClick = { navController.navigateUp() }, modifier = Modifier.padding(8.dp)) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.nutrisoul),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(95.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "NutriSoul",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 30.sp,
                    letterSpacing = 2.sp
                ),
                color = Color.White
            )
            Text(
                text = "Nourish Your Body. Elevate Your Soul.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    letterSpacing = 0.5.sp
                ),
                color = Color(0xFFD0F0D8)
            )
        }

        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.70f),
            shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 12.dp
            ),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Email Address", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Enter your email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = PrimaryGreen,
                        cursorColor = PrimaryGreen
                    ),
                    shape = RoundedCornerShape(14.dp), 
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = email.isNotEmpty() && !isEmailValid
                )
                if (email.isNotEmpty() && !isEmailValid) {
                    Text(
                        "Invalid email format",
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Password", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Enter your password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = PrimaryGreen,
                        cursorColor = PrimaryGreen
                    ),
                    shape = RoundedCornerShape(14.dp),
                    isError = password.isNotEmpty() && !isPasswordValid
                )
                Text(
                    Validation.passwordHint(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (password.isNotEmpty() && !isPasswordValid) ErrorRed else Color.Gray,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Confirm Password", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm your password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                            Icon(
                                imageVector = if (isConfirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (isConfirmPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = PrimaryGreen,
                        cursorColor = PrimaryGreen
                    ),
                    shape = RoundedCornerShape(14.dp),
                    isError = confirmPassword.isNotEmpty() && !doPasswordsMatch
                )
                if (confirmPassword.isNotEmpty() && !doPasswordsMatch) {
                    Text(
                        "Passwords do not match.",
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { authViewModel.register(email, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                    enabled = isButtonEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        disabledContainerColor = Color(0xFF9E9E9E)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp
                    )
                ) {
                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Already have an account?")
                    TextButton(onClick = { navController.navigate(Screen.Login.route) }) {
                        Text("Login Now", color = PrimaryGreen)
                    }
                }
            }
        }
    }
}



