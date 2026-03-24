package com.simats.nutrisoul

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.simats.nutrisoul.data.UserViewModel
import com.simats.nutrisoul.ui.theme.*
import com.simats.nutrisoul.data.network.AiTipDto

private data class Recommendation(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconBg: Color,
    val iconTint: Color,
    val cardBg: Color,
    val border: Color
)

@Composable
fun AiTipsScreen(
    navController: NavController, 
    userViewModel: UserViewModel,
    mealPlanViewModel: MealPlanViewModel = hiltViewModel()
) {
    val userState by userViewModel.user.collectAsStateWithLifecycle()
    val user = userState
    val tips by mealPlanViewModel.tips.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        Log.d("Analytics", "AI_TIPS_SCREEN_OPENED")
    }

    val headerBrush = Brush.linearGradient(
        colors = listOf(AiGradientStart, AiGradientEnd)
    )

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(headerBrush, shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                        .padding(top = 18.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(32.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier.background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(50))
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.navigate_back),
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = stringResource(R.string.ai_assistant_icon),
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.ai_tips_title),
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            item {
                if (user == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-40).dp)
                            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = AiGradientStart)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                stringResource(R.string.analyzing_profile),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            if (user != null) {
                val recList = tips.map { tip ->
                    Recommendation(
                        title = tip.title,
                        description = tip.description,
                        icon = mapIcon(tip.icon),
                        iconBg = parseColor(tip.iconBg),
                        iconTint = parseColor(tip.iconTint),
                        cardBg = parseColor(tip.cardBg),
                        border = parseColor(tip.borderColor)
                    )
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-40).dp)
                            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                            .padding(horizontal = 20.dp, vertical = 24.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.personalized_insights_for),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = user.name.ifBlank { stringResource(R.string.default_user_name) },
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp).offset(y = (-40).dp)) {
                        val defaultGoal = stringResource(R.string.default_goal)
                        GoalCard(goal = user.goals.firstOrNull() ?: defaultGoal)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.padding(horizontal = 20.dp).offset(y = (-40).dp)) {
                        Text(
                            text = stringResource(R.string.your_personalized_plan),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                items(recList) { rec ->
                    Box(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 16.dp).offset(y = (-40).dp)) {
                        RecommendationCard(rec)
                    }
                }

                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 16.dp).offset(y = (-40).dp)) {
                        ProTipsCard()
                    }
                }

                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp).offset(y = (-40).dp)) {
                        NoteCard()
                    }
                }
            }
        }
    }
}

private fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color.Gray
    }
}

private fun mapIcon(name: String): ImageVector {
    return when (name.lowercase()) {
        "star" -> Icons.Default.Star
        "favorite_border" -> Icons.Default.FavoriteBorder
        "health_and_safety" -> Icons.Default.HealthAndSafety
        "bloodtype" -> Icons.Default.Bloodtype
        "directions_run" -> Icons.AutoMirrored.Filled.DirectionsRun
        "trending_down" -> Icons.AutoMirrored.Filled.TrendingDown
        "trending_up" -> Icons.AutoMirrored.Filled.TrendingUp
        "water_drop" -> Icons.Default.WaterDrop
        "lightbulb" -> Icons.Default.Lightbulb
        "track_changes" -> Icons.Default.TrackChanges
        else -> Icons.Default.TipsAndUpdates
    }
}

@Composable
private fun GoalCard(goal: String) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB).copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(AiGradientStart, AiGradientEnd)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.TrackChanges,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.your_goal_label), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(goal, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    stringResource(R.string.goal_card_description),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun RecommendationCard(rec: Recommendation) {
    val isDark = LocalDarkTheme.current
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) rec.cardBg.copy(alpha = 0.15f) else rec.cardBg),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) rec.border.copy(alpha = 0.3f) else rec.border),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(rec.iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(rec.icon, contentDescription = rec.title, tint = rec.iconTint, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(rec.title, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(8.dp))
                Text(rec.description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
            }
        }
    }
}

@Composable
private fun ProTipsCard() {
    val isDark = LocalDarkTheme.current
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF500724).copy(alpha = 0.2f) else Color(0xFFFDF2F8)),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0xFF500724) else Color(0xFFFBCFE8))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AiGradientStart),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Star, contentDescription = stringResource(R.string.pro_tips_icon), tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(stringResource(R.string.pro_tips_label), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            }

            Spacer(modifier = Modifier.height(12.dp))

            val tips = listOf(
                stringResource(R.string.pro_tip_1),
                stringResource(R.string.pro_tip_2),
                stringResource(R.string.pro_tip_3),
                stringResource(R.string.pro_tip_4)
            )
            tips.forEach { tip ->
                Row(modifier = Modifier.padding(bottom = 6.dp)) {
                    Text("•  ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(tip, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
private fun NoteCard() {
    val isDark = LocalDarkTheme.current
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFFEFF6FF).copy(alpha = 0.15f) else Color(0xFFEFF6FF)),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0xFFBFDBFE).copy(alpha = 0.3f) else Color(0xFFBFDBFE))
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Icon(
                Icons.Default.Lightbulb,
                contentDescription = stringResource(R.string.medical_disclaimer_icon),
                tint = ProteinBlue,
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                stringResource(R.string.medical_disclaimer),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 16.sp
            )
        }
    }
}
