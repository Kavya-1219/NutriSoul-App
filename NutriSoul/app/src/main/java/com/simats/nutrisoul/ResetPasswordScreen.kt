package com.simats.nutrisoul

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.simats.nutrisoul.ui.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    navController: NavController,
    vm: ResetPasswordViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(vm.uiState) {
        when (val s = vm.uiState) {
            is ResetUiState.Message -> {
                snackbarHostState.showSnackbar(s.text)
                vm.clearUiMessage()
            }
            is ResetUiState.Error -> {
                snackbarHostState.showSnackbar(s.message)
                vm.clearUiMessage()
            }
            ResetUiState.Done -> {
                // after success, go back to login & clear reset screen from stack
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.ResetPassword.route) { inclusive = true }
                }
            }
            else -> Unit
        }
    }

    val isLoading = vm.uiState is ResetUiState.Loading

    Box(
        modifier = Modifier.fillMaxSize().background(PrimaryGreen)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(start = 16.dp, top = 16.dp)
                .clickable { vm.back { navController.navigateUp() } }
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("Back to Login", color = Color.White)
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(92.dp).clip(CircleShape).background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(50.dp))
            }
            Spacer(Modifier.height(18.dp))

            Text("Reset Password", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Spacer(Modifier.height(6.dp))

            val subtitle = when (vm.step) {
                ResetStep.EMAIL -> "Enter your email to receive an OTP"
                ResetStep.OTP -> "Enter the 6-digit OTP sent to your email"
                ResetStep.NEW_PASSWORD -> "Create a new password for your account"
            }
            Text(subtitle, color = Color.White.copy(alpha = 0.9f))
        }

        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.60f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            ResetPasswordContent(vm = vm)
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun ResetPasswordContent(vm: ResetPasswordViewModel) {
    val isLoading = vm.uiState is ResetUiState.Loading
    Column(
        modifier = Modifier.fillMaxSize().padding(22.dp)
    ) {
        Spacer(Modifier.height(10.dp))

        when (vm.step) {
            ResetStep.EMAIL -> {
                Text("Email", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = vm.email,
                    onValueChange = vm::onEmailChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    placeholder = { Text("Enter your registered email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        cursorColor = PrimaryGreen
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text("We’ll email you an OTP to verify it’s you.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                Spacer(Modifier.weight(1f))
                Button(
                    onClick = vm::sendOtp,
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    enabled = vm.isEmailValid && !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) { Text("Send OTP", color = Color.White) }
            }

            ResetStep.OTP -> {
                Text("Enter OTP", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = vm.otp,
                    onValueChange = vm::onOtpChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Enter 6-digit OTP") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        cursorColor = PrimaryGreen
                    )
                )

                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Check your email for the code.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    TextButton(
                        onClick = vm::resendOtp,
                        enabled = vm.resendSeconds == 0 && !isLoading
                    ) {
                        val text = if (vm.resendSeconds == 0) "Resend OTP" else "Resend in ${vm.resendSeconds}s"
                        Text(text, color = PrimaryGreen)
                    }
                }

                Spacer(Modifier.weight(1f))
                Button(
                    onClick = vm::verifyOtp,
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    enabled = vm.isOtpValid && !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) { Text("Verify OTP", color = Color.White) }
            }

            ResetStep.NEW_PASSWORD -> {
                Text("New Password", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = vm.newPassword,
                    onValueChange = vm::onNewPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    placeholder = { Text("Enter new password") },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        cursorColor = PrimaryGreen
                    )
                )
                Spacer(Modifier.height(12.dp))

                Text("Confirm Password", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = vm.confirmPassword,
                    onValueChange = vm::onConfirmPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    placeholder = { Text("Re-enter new password") },
                    visualTransformation = PasswordVisualTransformation(),
                    isError = vm.confirmPassword.isNotBlank() && !vm.isPasswordMatch,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        cursorColor = PrimaryGreen
                    )
                )

                Spacer(Modifier.height(8.dp))
                val helper = when {
                    vm.newPassword.isNotBlank() && !vm.isPasswordValid -> "Password must be at least 6 characters."
                    vm.confirmPassword.isNotBlank() && !vm.isPasswordMatch -> "Passwords do not match."
                    else -> " "
                }
                Text(helper, style = MaterialTheme.typography.bodySmall, color = if (helper.trim() == "") Color.Transparent else MaterialTheme.colorScheme.error)

                Spacer(Modifier.weight(1f))
                Button(
                    onClick = vm::confirmReset,
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    enabled = vm.isPasswordValid && vm.isPasswordMatch && !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) { Text("Change Password", color = Color.White) }
            }
        }
    }
}



