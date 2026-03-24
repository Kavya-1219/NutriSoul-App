package com.simats.nutrisoul

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Logout
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.simats.nutrisoul.data.UserViewModel

@Composable
fun SettingsScreen(
    navController: NavController,
    userViewModel: UserViewModel = hiltViewModel()
) {
    val darkMode by userViewModel.darkMode.collectAsStateWithLifecycle()
    val userName by userViewModel.userName.collectAsStateWithLifecycle()
    val profilePictureUri by userViewModel.profilePictureUri.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                try {
                    val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    navController.context.contentResolver.takePersistableUriPermission(it, flags)
                } catch (_: SecurityException) {}
                userViewModel.setProfilePictureUri(it)
            }
        }
    )

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        userViewModel.logout()
                        showLogoutDialog = false
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                ) { Text("Logout") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Header()

            ProfilePictureSection(
                profilePictureUri = profilePictureUri?.toString(),
                userName = userName,
                onImageClick = { imagePickerLauncher.launch(arrayOf("image/*")) }
            )

            SettingsOptions(
                darkMode = darkMode,
                onDarkModeChange = { userViewModel.setDarkMode(it) },
                onProfileClick = { navController.navigate("profile") },
                onHelpClick = { navController.navigate("help") },
                onAboutClick = { navController.navigate("about") },
                onLogoutClick = { showLogoutDialog = true }
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Header() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF6A1B9A), Color(0xFF8E24AA))
                )
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Customize your experience",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun ProfilePictureSection(
    profilePictureUri: String?,
    userName: String,
    onImageClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .offset(y = (-60).dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (profilePictureUri != null) {
                    AsyncImage(
                        model = profilePictureUri,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFB39DDB), Color(0xFF7E57C2))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Default Profile",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Camera icon removed to restrict changes to Profile Settings
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                // Subtitle removed as photo change is now in Profile Settings
            }
        }
    }
}

@Composable
private fun SettingsOptions(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onProfileClick: () -> Unit,
    onHelpClick: () -> Unit,
    onAboutClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {

        SectionTitle("APPEARANCE")
        CardBlock {
            // Sun icon when OFF (yellow), Moon icon when ON (indigo)
            val darkIcon = if (darkMode) Icons.Default.DarkMode else Icons.Default.WbSunny
            val darkTint = if (darkMode) Color(0xFF5C6BC0) else Color(0xFFFFB300) // indigo / yellow

            SettingItem(
                icon = darkIcon,
                iconTint = darkTint,
                title = "Dark Mode",
                subtitle = if (darkMode) "On" else "Off",
                trailing = {
                    Switch(
                        checked = darkMode,
                        onCheckedChange = onDarkModeChange
                    )
                },
                onClick = { onDarkModeChange(!darkMode) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("ACCOUNT")
        CardBlock {
            SettingItem(
                icon = Icons.Default.AccountCircle,
                iconTint = Color(0xFF43A047),
                title = "Profile Settings",
                subtitle = "Manage your profile",
                onClick = onProfileClick,
                trailing = {
                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = "Open",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("HELP & SUPPORT")
        CardBlock {
            SettingItem(
                icon = Icons.Default.Help,
                iconTint = Color(0xFFFB8C00),
                title = "Help & Support",
                subtitle = "FAQs, Chat with AI",
                onClick = onHelpClick,
                trailing = {
                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = "Open",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            SettingItem(
                icon = Icons.Default.Info,
                iconTint = Color(0xFF8E24AA),
                title = "About",
                subtitle = "Version 1.0.0",
                onClick = onAboutClick,
                trailing = {
                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = "Open",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("LOGOUT")
        CardBlock {
            SettingItem(
                icon = Icons.Default.Logout,
                iconTint = Color(0xFFE53935),
                title = "Logout",
                subtitle = "Sign out of your account",
                titleColor = Color(0xFFE53935),
                subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = onLogoutClick,
                trailing = {
                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = "Open",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun CardBlock(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconTint
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                color = titleColor
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = subtitleColor
            )
        }
        if (trailing != null) trailing()
    }
}



