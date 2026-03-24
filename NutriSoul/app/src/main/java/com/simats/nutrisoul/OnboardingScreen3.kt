package com.simats.nutrisoul

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.simats.nutrisoul.ui.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen3(onGetStartedClicked: () -> Unit, onSkipClicked: () -> Unit, onLoginClicked: () -> Unit) {
    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    TextButton(onClick = onSkipClicked) {
                        Text(text = "Skip", color = Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BottomAppBar(
                    containerColor = Color.Transparent,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Button(
                        onClick = onGetStartedClicked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(text = "Get Started >")
                    }
                }
                TextButton(onClick = onLoginClicked) {
                    Text(text = "Already have an account? Login", color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFF2FBF5), Color.White)
                    )
                )
                .padding(paddingValues)
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo4),
                contentDescription = "Onboarding Image",
                modifier = Modifier.size(200.dp)
            )
            Spacer(modifier = Modifier.height(64.dp))
            Text(
                text = "Holistic Wellness Support",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Manage weight, build muscle, reduce stress, and improve your overall well-being",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(64.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(width = 24.dp, height = 8.dp)
                        .clip(CircleShape)
                        .background(PrimaryGreen)
                )
            }
        }
    }
}