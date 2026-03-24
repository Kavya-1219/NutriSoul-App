@file:OptIn(ExperimentalMaterial3Api::class)

package com.simats.nutrisoul

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.*

private val faqs = listOf(
    "How do I log my meals?" to "Go to the 'Log Food' screen from the home page. You can search for foods from our database, add manually by entering details, or scan/upload food images for automatic nutrition detection.",
    "How accurate are the calorie calculations?" to "Our calorie calculations are based on scientifically validated formulas (Mifflin-St Jeor equation) and take into account your age, gender, weight, height, and activity level. For best results, update your profile regularly.",
    "Can I customize my meal plan?" to "Yes! Go to the Meal Plan screen and tap the edit button on any meal. You can adjust portions, remove items, or generate a completely new plan by tapping the refresh button.",
    "How do I track water intake?" to "Use the water tracking widget on the home screen. Tap the + button each time you drink a glass of water (250ml). Your daily goal is calculated based on your body weight.",
    "What if I have food allergies?" to "Your food allergies are already saved from your profile setup. The meal plan automatically excludes all your allergic foods. You can update allergies anytime in Profile Settings.",
    "How do I change my goal?" to "Go to Profile Settings and edit your goal. The app will recalculate your daily calorie target and adjust meal recommendations accordingly.",
    "Can I use the app offline?" to "Yes! All your data is stored locally on your device. You can log meals and view your history offline. However, AI chat and food image scanning require internet connection.",
    "How do I track my progress?" to "Check the 'Insights' screen for weekly statistics, macro distribution, and consistency tracking. The Home screen shows your daily progress and weight goals.",
    "Is my data secure?" to "Yes! All your data is stored locally on your device and is not shared with third parties. We do not collect or sell your personal information.",
    "How do I reset my password?" to "Go to Profile Settings, then scroll down to 'Change Password'. You'll need to enter your current password to set a new one."
)

private data class Message(val text: String, val isUser: Boolean, val timestamp: String)

@Composable
fun HelpSupportScreen(
    navController: NavController,
    viewModel: NutritionInsightsViewModel = hiltViewModel()
) {
    var activeTab by remember { mutableStateOf("faqs") }
    val insightsState by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            Header(navController)
            Column(Modifier.offset(y = (-64).dp)) {
                TabSwitcher(activeTab) { activeTab = it }
                when (activeTab) {
                    "faqs" -> FaqsTab { activeTab = "chat" }
                    "chat" -> ChatTab(viewModel)
                    "insights" -> AiInsightsTab(insightsState)
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
                    colors = listOf(Color(0xFFFFA726), Color(0xFFF4511E))
                )
            )
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp)
    ) {
        Column {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.HelpOutline, contentDescription = "Help", tint = Color.White, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "Help & Support", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Text(text = "We're here to help you succeed", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.9f))
        }
    }
}

@Composable
private fun TabSwitcher(activeTab: String, onTabSelected: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            val faqsGradient = Brush.verticalGradient(colors = listOf(Color(0xFFFFA726), Color(0xFFF4511E)))
            val chatGradient = Brush.verticalGradient(colors = listOf(Color(0xFFFFA726), Color(0xFFF4511E)))

            val faqsModifier = if (activeTab == "faqs") {
                Modifier.background(faqsGradient)
            } else {
                Modifier.background(Color.Transparent)
            }

            val chatModifier = if (activeTab == "chat") {
                Modifier.background(chatGradient)
            } else {
                Modifier.background(Color.Transparent)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .then(faqsModifier)
                    .clickable { onTabSelected("faqs") },
                contentAlignment = Alignment.Center
            ) {
                Text("FAQs", color = if (activeTab == "faqs") Color.White else MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(12.dp))
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .then(chatModifier)
                    .clickable { onTabSelected("chat") },
                contentAlignment = Alignment.Center
            ) {
                Text("AI Chat", color = if (activeTab == "chat") Color.White else MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(12.dp))
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .then(if (activeTab == "insights") Modifier.background(chatGradient) else Modifier.background(Color.Transparent))
                    .clickable { onTabSelected("insights") },
                contentAlignment = Alignment.Center
            ) {
                // renamed to AI Insights for better clarity with real data
                Text("AI Insights", color = if (activeTab == "insights") Color.White else MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(12.dp))
            }
        }
    }
}

@Composable
fun FaqsTab(onChatClick: () -> Unit) {
    var expandedFaq by remember { mutableStateOf<Int?>(null) }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        itemsIndexed(faqs) { index, faq ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                onClick = { expandedFaq = if (expandedFaq == index) null else index }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = faq.first,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (expandedFaq == index) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand"
                        )
                    }
                    AnimatedVisibility(visible = expandedFaq == index) {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = faq.second, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Still need help?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Can't find what you're looking for? Try our AI chat or contact support.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onChatClick, colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Chat, contentDescription = "Chat", modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Chat with AI")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatTab(viewModel: NutritionInsightsViewModel = hiltViewModel()) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val chatResponse by viewModel.chatResponse.collectAsStateWithLifecycle()
    val isLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()

    val chatMessages = remember {
        mutableStateListOf(
            Message("Hi! I'm NutriSoul AI, powered by Gemini. I know your nutrition profile and can give you personalized advice. How can I help you today? 🥗", false, SimpleDateFormat("hh:mm a", Locale.US).format(Date()))
        )
    }
    var inputMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // When AI responds, add response to messages
    LaunchedEffect(chatResponse) {
        chatResponse?.let { response ->
            chatMessages.add(Message(response, false, SimpleDateFormat("hh:mm a", Locale.US).format(Date())))
            viewModel.clearChatResponse()
        }
    }

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }
            items(chatMessages) { message ->
                ChatMessage(message)
            }
            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(topStart = 0.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("NutriSoul AI is thinking...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
        ChatInput(
            input = inputMessage,
            onInputChange = { inputMessage = it },
            onSend = {
                if (inputMessage.isNotBlank() && !isLoading) {
                    val userMessageText = inputMessage
                    chatMessages.add(Message(userMessageText, true, SimpleDateFormat("hh:mm a", Locale.US).format(Date())))
                    inputMessage = ""
                    keyboardController?.hide()
                    viewModel.sendChatMessage(userMessageText)
                }
            }
        )
    }
}

@Composable
private fun ChatMessage(message: Message) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        // ✅ Fix 2: Theme-aware bubble colors
        val userBubble = Color(0xFF1976D2)
        val aiBubble = MaterialTheme.colorScheme.surfaceVariant
        val aiText = MaterialTheme.colorScheme.onSurfaceVariant

        Card(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = RoundedCornerShape(
                topStart = if (message.isUser) 16.dp else 0.dp,
                topEnd = if (message.isUser) 0.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) userBubble else aiBubble
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.text,
                    color = if (message.isUser) Color.White else aiText
                )
                Text(
                    text = message.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (message.isUser) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun ChatInput(
    input: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() })
            )
            IconButton(
                onClick = onSend,
                enabled = input.isNotBlank(),
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (input.isNotBlank()) Color(0xFF1976D2) else MaterialTheme.colorScheme.outlineVariant)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}

@Composable
fun AiInsightsTab(state: NutritionInsightsUiState) {
    when (state) {
        is NutritionInsightsUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is NutritionInsightsUiState.Empty -> {
            Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(
                    "Log more meals to see AI nutrition trends and predictions!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        is NutritionInsightsUiState.Success -> {
            val data = state.data
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "AI Consumption Trends",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Your average intake is ${data.averageCalories} kcal. You are currently ${data.calorieStatus.label} your target.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Real Data Chart (Visualization of Macro percentages)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val macros = listOf(
                            "Protein" to data.proteinPercentage / 100f,
                            "Carbs" to data.carbsPercentage / 100f,
                            "Fats" to data.fatsPercentage / 100f
                        )
                        macros.forEach { (label, value) ->
                            val barColor = when(label) {
                                "Protein" -> Color(0xFF4ADE80)
                                "Carbs" -> Color(0xFF60A5FA)
                                else -> Color(0xFFF87171)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .width(40.dp)
                                        .fillMaxHeight(value.coerceIn(0.1f, 1f))
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        .background(barColor)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = label, style = MaterialTheme.typography.labelSmall)
                                Text(text = "${(value * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, "Insight", tint = Color(0xFFFFB300))
                        Spacer(Modifier.width(8.dp))
                        Text("Personalized AI Tip", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val tip = when {
                        data.proteinPercentage < 15 -> "Your protein intake is a bit low. Aim for more eggs, dal, or lean meats to support muscle recovery."
                        data.carbsPercentage > 60 -> "Carbs are high today. Try focusing on fiber-rich complex carbs like oats or brown rice for better satiety."
                        data.calorieStatus.tone == StatusTone.GOOD -> "Excellent consistency! Your nutrition is perfectly aligned with your health goals."
                        else -> "Consistency is key. You've logged ${data.daysLogged} days this week. Keeping up this habit is 80% of the journey!"
                    }
                    Text(tip, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
}
}



private fun getAiResponse(message: String): String {
    val msg = message.lowercase()
    return when {
        msg.contains("hello") || msg.contains("hi") -> "Hello! I'm your AI health assistant. I can help you with nutrition advice, meal planning, or progress tracking. What's on your mind?"
        msg.contains("weight") -> "To help with weight goals, I recommend focusing on high-fiber foods and consistent tracking. Would you like a personalized 1500-calorie suggestion?"
        msg.contains("protein") -> "Protein is essential! Good sources include lean meats, legumes, and Greek yogurt. Based on your profile, you should aim for about 1.2g per kg of body weight."
        msg.contains("log") -> "You can log meals by going to the 'Log Food' screen. From there you can search, manually enter, or scan food items."
        msg.contains("meal plan") -> "You can view and edit your meal plan on the 'Meal Plan' screen. You can also generate a new plan there."
        msg.contains("water") -> "Track your water intake on the home screen using the water widget. Each tap adds one glass (250ml)."
        else -> "That's an interesting question! While I'm still learning about specific medical conditions, I can definitely help with general nutrition tips or meal tracking. Could you tell me more?"
    }
}


