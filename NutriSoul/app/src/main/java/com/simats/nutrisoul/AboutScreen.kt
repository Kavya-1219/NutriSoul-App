package com.simats.nutrisoul

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.nutrisoul.ui.theme.LocalDarkTheme

@Composable
fun AboutScreen(navController: NavController) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Header(navController)
                }
                item {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .offset(y = (-80).dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MissionCard()
                        FeaturesCard()
                        HealthConditionsCard()
                        DisclaimerCard()
                        MadeWithLoveCard()
                    }
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
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF10B981), Color(0xFF059669))
                ),
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .padding(top = 16.dp, bottom = 100.dp, start = 16.dp, end = 16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "App Icon",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Personalised Nutrition",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun MissionCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.TrackChanges,
                    contentDescription = "Our Mission",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Our Mission",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "To empower individuals on their health journey by providing personalized nutrition guidance, AI-powered recommendations, and comprehensive tracking tools. We believe everyone deserves access to professional-grade nutrition planning tailored to their unique needs.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FeaturesCard() {
    val isDark = LocalDarkTheme.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Key Features",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
            FeatureItem(
                icon = Icons.Filled.AutoAwesome,
                iconBgColor = if(isDark) Color(0xFF0284C7).copy(alpha=0.2f) else Color(0xFFE0F2FE),
                iconColor = if(isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                title = "AI-Powered Nutrition",
                subtitle = "Advanced AI analyzes your meals and provides personalized recommendations"
            )
            Spacer(modifier = Modifier.height(16.dp))
            FeatureItem(
                icon = Icons.Default.TrackChanges,
                iconBgColor = if(isDark) Color(0xFF9333EA).copy(alpha=0.2f) else Color(0xFFF3E8FF),
                iconColor = if(isDark) Color(0xFFC084FC) else Color(0xFF9333EA),
                title = "Personalized Plans",
                subtitle = "Custom meal plans based on your goals, health conditions, and preferences"
            )
            Spacer(modifier = Modifier.height(16.dp))
            FeatureItem(
                icon = Icons.Default.Shield,
                iconBgColor = if(isDark) Color(0xFF16A34A).copy(alpha=0.2f) else Color(0xFFF0FDF4),
                iconColor = if(isDark) Color(0xFF4ADE80) else Color(0xFF16A34A),
                title = "Privacy First",
                subtitle = "Your data is stored locally on your device. We never share your information"
            )
            Spacer(modifier = Modifier.height(16.dp))
            FeatureItem(
                icon = Icons.Default.Group,
                iconBgColor = if(isDark) Color(0xFFF97316).copy(alpha=0.2f) else Color(0xFFFFF7ED),
                iconColor = if(isDark) Color(0xFFFB923C) else Color(0xFFF97316),
                title = "Comprehensive Tracking",
                subtitle = "Track food, water, steps, sleep, and more in one integrated platform"
            )
        }
    }
}

@Composable
fun FeatureItem(icon: ImageVector, iconBgColor: Color, iconColor: Color, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}


@Composable
private fun HealthConditionsCard() {
    val conditions = listOf(
        "Diabetes Management", "PCOS Support", "High Blood Pressure",
        "Stress Management", "Weight Loss", "Muscle Gain",
        "Heart Health", "Food Allergies"
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Health Conditions We Support",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            val rows = conditions.chunked(2)
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { item ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(vertical = 8.dp, horizontal = 12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = item, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun DisclaimerCard() {
    val isDark = LocalDarkTheme.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if(isDark) Color(0xFF78350F).copy(alpha=0.3f) else Color(0xFFFEF3C7)),
        border = BorderStroke(1.dp, if(isDark) Color(0xFF78350F) else Color(0xFFFDE68A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Medical Disclaimer",
                    tint = if(isDark) Color(0xFFFBBF24) else Color(0xFFD97706),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Medical Disclaimer",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if(isDark) Color(0xFFFBBF24) else Color(0xFF92400E)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "This app is for informational purposes only and should not replace professional medical advice. Always consult with a healthcare provider or registered dietitian before making significant changes to your diet or exercise routine, especially if you have existing health conditions.",
                style = MaterialTheme.typography.bodyMedium,
                color = if(isDark) Color(0xFFFDE68A) else Color(0xFF92400E),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun MadeWithLoveCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Heart",
                tint = Color.Red,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "Made with ❤️ for your health journey",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "© 2026 NutriSoul App. All rights reserved.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
