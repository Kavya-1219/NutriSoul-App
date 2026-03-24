package com.simats.nutrisoul

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.simats.nutrisoul.ui.FoodItemUi
import com.simats.nutrisoul.ui.CalorieProgressCard
import com.simats.nutrisoul.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogFoodScreen(
    navController: NavController,
    viewModel: LogFoodViewModel = hiltViewModel(),
    autoScan: Boolean = false
) {
    val todayTotals by viewModel.todayTotals.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedFood by remember { mutableStateOf<FoodItemUi?>(null) }
    var showManualSheet by remember { mutableStateOf(false) }
    var showScanResults by remember { mutableStateOf(false) }
    var isCameraOpen by remember { mutableStateOf(autoScan) }

    var targetCalories by remember { mutableStateOf(2000.0) }
    var lastScanUri by remember { mutableStateOf<Uri?>(null) }
    LaunchedEffect(Unit) {
        targetCalories = viewModel.getTargetCaloriesOrDefault(2000.0)
    }
    
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
        } else {
            Toast.makeText(context, "Camera permission is required to scan food.", Toast.LENGTH_SHORT).show()
            isCameraOpen = false
        }
    }

    LaunchedEffect(autoScan) {
        if (autoScan) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    
    val progressRaw = if (targetCalories > 0.0) (todayTotals.calories / targetCalories).toFloat() else 0f
    val progress = progressRaw.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(progress, label = "")

    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            lastScanUri = uri
            viewModel.onImageSelected(uri)
            showScanResults = true
        }
    }

    var tempUri by remember { mutableStateOf<Uri?>(null) }
    val takePicture = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = tempUri
        if (success && uri != null) {
            lastScanUri = uri
            viewModel.onImageSelected(uri)
            showScanResults = true
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    LogFoodHeader(
                        navController = navController,
                        onBack = { navController.popBackStack() }
                    )
                }

                item {
                    Box(Modifier.padding(horizontal = 16.dp)) {
                        TodayCaloriesCard(
                            calories = todayTotals.calories,
                            targetCalories = targetCalories,
                            protein = todayTotals.protein,
                            carbs = todayTotals.carbs,
                            fats = todayTotals.fats,
                            progress = animatedProgress
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        GradientActionCard(
                            modifier = Modifier.weight(1f),
                            title = "Scan Food",
                            subtitle = "AI Detection",
                            icon = Icons.Default.CameraAlt,
                            gradient = Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))),
                            onClick = {
                                when (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)) {
                                    PackageManager.PERMISSION_GRANTED -> {
                                        isCameraOpen = true
                                    }
                                    else -> {
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                }
                            },
                            height = 120.dp
                        )

                        GradientActionCard(
                            modifier = Modifier.weight(1f),
                            title = "Upload",
                            subtitle = "From Gallery",
                            icon = Icons.Default.Add,
                            gradient = Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669))),
                            onClick = { pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            height = 120.dp
                        )
                    }
                }

                item {
                    Box(Modifier.padding(horizontal = 16.dp)) {
                        GradientActionCard(
                            modifier = Modifier.fillMaxWidth(),
                            title = "Manual Entry",
                            subtitle = "Type and log food",
                            icon = Icons.Default.Edit,
                            gradient = Brush.horizontalGradient(
                                listOf(Color(0xFFFF5FA2), Color(0xFFFF2D55))
                            ),
                            onClick = { showManualSheet = true },
                            height = 125.dp
                        )
                    }
                }

                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        ScanTipsCard()
                    }
                }

                item {
                    Box(Modifier.padding(horizontal = 16.dp)) {
                        FoodSearchCard(
                            query = query,
                            onQueryChanged = viewModel::onQueryChanged,
                            results = searchResults,
                            localFoods = suggestedFoods,
                            onFoodPick = { selectedFood = it },
                            onQuickAddLocal = { item ->
                                viewModel.addFood(item, item.servingQuantity)
                            }
                        )
                    }
                }
            }

            selectedFood?.let { food ->
                FoodDetailsBottomSheet(
                    foodItem = food,
                    onDismiss = { selectedFood = null },
                    onLogFood = { item, qty ->
                        viewModel.addFood(item, qty)
                        selectedFood = null
                    }
                )
            }

            if (showManualSheet) {
                ManualEntryBottomSheet(
                    onDismiss = { showManualSheet = false },
                    onSave = { manual ->
                        viewModel.addManualFood(
                            name = manual.name,
                            quantity = manual.quantity,
                            calories = manual.calories,
                            protein = manual.protein,
                            carbs = manual.carbs,
                            fats = manual.fats
                        )
                        showManualSheet = false
                    }
                )
            }

            if (showScanResults) {
                ScanResultBottomSheet(
                    uiState = uiState,
                    navController = navController,
                    onDismiss = { showScanResults = false },
                    onLogFood = { item, qty ->
                        viewModel.addFood(item, qty)
                    },
                    onLogAll = { items ->
                        items.forEach { item ->
                            viewModel.addFood(item, item.servingQuantity)
                        }
                    },
                    onManualEntry = {
                        showScanResults = false
                        showManualSheet = true
                    },
                    onRetry = {
                        lastScanUri?.let { uri ->
                            viewModel.onImageSelected(uri)
                        }
                    }
                )
            }
        }
        
        if (isCameraOpen) {
            BackHandler {
                isCameraOpen = false
            }
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                var cameraController by remember { mutableStateOf<CameraController?>(null) }
                
                CameraView(
                    onControllerReady = { controller ->
                        cameraController = controller
                    }
                )
                
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(300.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color.Transparent)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(30.dp))
                        )
                    }
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(top = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { isCameraOpen = false },
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                    Text(
                        text = "Scan Food",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.size(48.dp))
                }

                Text(
                    text = "Center the food in the frame",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 110.dp)
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 64.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Button(
                        onClick = {
                            cameraController?.takePicture { bitmap ->
                                scope.launch(Dispatchers.IO) {
                                    val uri = saveBitmapToTempUri(context, bitmap)
                                    withContext(Dispatchers.Main) {
                                        isCameraOpen = false
                                        viewModel.onImageSelected(uri)
                                        showScanResults = true
                                    }
                                }
                            }
                        },
                        modifier = Modifier.size(80.dp),
                        shape = RoundedCornerShape(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(32.dp))
                                .background(Color.Transparent)
                                .background(Color(0xFF8B5CF6).copy(alpha = 0.2f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .align(Alignment.Center)
                                    .clip(RoundedCornerShape(27.dp))
                                    .background(Color(0xFF8B5CF6))
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun createTempImageUri(context: android.content.Context): Uri {
    val dir = context.externalCacheDir ?: context.cacheDir
    val file = File(dir, "scan_${System.currentTimeMillis()}.jpg").apply { 
        createNewFile()
    }
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

private fun saveBitmapToTempUri(context: android.content.Context, bitmap: android.graphics.Bitmap): Uri {
    val dir = context.externalCacheDir ?: context.cacheDir
    val file = File(dir, "scan_captured_${System.currentTimeMillis()}.jpg")
    file.outputStream().use { out ->
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
    }
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultBottomSheet(
    uiState: LogFoodUiState,
    navController: NavController,
    onDismiss: () -> Unit,
    onLogFood: (FoodItemUi, Double) -> Unit,
    onLogAll: (List<FoodItemUi>) -> Unit,
    onManualEntry: () -> Unit,
    onRetry: () -> Unit
) {
    var selectedIndex by remember(uiState.nutrition) { mutableStateOf(0) }
    val selected = uiState.nutrition.getOrNull(selectedIndex)

    if (uiState.isLoading) {
        ModalBottomSheet(onDismissRequest = onDismiss) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text(
                    text = uiState.scanMessage ?: "Analyzing your food...",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "We’re identifying foods and estimating nutrition.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(24.dp))
            }
        }
        return
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Scan Results",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = uiState.scanMessage ?: "Choose the best match below",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            if (uiState.nutrition.isEmpty()) {
                EmptyScanState(onManualEntry = onManualEntry, onDismiss = onDismiss)
                Spacer(Modifier.height(24.dp))
                return@ModalBottomSheet
            }

            Text(
                text = "Possible matches",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                uiState.nutrition.forEachIndexed { index, item ->
                    CandidateFoodCard(
                        item = item,
                        selected = index == selectedIndex,
                        onClick = { selectedIndex = index }
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            selected?.let { item ->
                ConfirmedFoodPanel(
                    item = item,
                    onLogFood = onLogFood,
                    onManualEntry = onManualEntry,
                    onRetryScan = onDismiss
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun FoodResultCard(
    item: FoodItemUi,
    navController: NavController,
    onLog: (Double) -> Unit,
    onAskAi: () -> Unit
) {
    var quantity by remember { mutableStateOf(1.0) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(2.dp, Brush.linearGradient(listOf(Color(0xFF10B981).copy(0.2f), Color(0xFF059669).copy(0.2f))))
    ) {
        Column(
            modifier = Modifier
                .background(Brush.verticalGradient(listOf(Color(0xFFF0FDF4).copy(0.5f), Color.White)))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
            
            val confidenceLabel = when {
                item.confidence >= 0.9 -> "Confidence High"
                item.confidence >= 0.7 -> "Confidence Moderate"
                else -> "Confidence Low"
            }
            Text(
                "100 ${item.servingUnit} • $confidenceLabel",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            Text(
                "Macronutrients",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MacroCardSmall("Calories", "${(item.calories * quantity).roundToInt()}", Color(0xFFEF4444), Modifier.weight(1f))
                MacroCardSmall("Protein", "${(item.protein * quantity).format(1)}g", Color(0xFF2196F3), Modifier.weight(1f))
                MacroCardSmall("Carbs", "${(item.carbs * quantity).format(1)}g", Color(0xFFFFC107), Modifier.weight(1f))
                MacroCardSmall("Fats", "${(item.fats * quantity).format(1)}g", Color(0xFFE91E63), Modifier.weight(1f))
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MacroMiniCard("Fiber", "${(item.fiber * quantity).format(1)}g", Color(0xFF4CAF50), Modifier.weight(1f))
                MacroMiniCard("Sugar", "${(item.sugar * quantity).format(1)}g", Color(0xFF9C27B0), Modifier.weight(1f))
                MacroMiniCard("Sat. Fat", "${(item.saturatedFat * quantity).format(1)}g", Color(0xFFFF9800), Modifier.weight(1f))
            }

            val hasVitamins = item.vitaminA > 0 || item.vitaminC > 0 || item.vitaminD > 0 || item.vitaminB12 > 0
            val hasMinerals = item.calcium > 0 || item.iron > 0 || item.magnesium > 0 || item.potassium > 0 || item.sodium > 0 || item.zinc > 0
            
            if (hasVitamins || hasMinerals) {
                Spacer(Modifier.height(20.dp))
                Text(
                    "Vitamins & Minerals",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color.LightGray.copy(0.3f))
                ) {
                    Column(Modifier.padding(12.dp)) {
                        FlowNutrientRow(item, quantity)
                    }
                }
            }

            if (item.proTip.isNotBlank()) {
                Spacer(Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                    border = BorderStroke(2.dp, Color(0xFFEF4444).copy(0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("✨", fontSize = 24.sp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("AI Recommendations for You", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(item.proTip, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                        }
                    }
                }
            }

            if (item.healthAlternative.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    border = BorderStroke(2.dp, Color(0xFFF59E0B).copy(0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("❤️", fontSize = 24.sp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Healthier Alternatives", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(item.healthAlternative, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Gray.copy(0.1f)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { if (quantity > 0.5) quantity -= 0.5 }) {
                    Icon(Icons.Default.Remove, contentDescription = null)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${quantity}x", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("Serving", style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = { quantity += 0.5 }) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFFEF4444))
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { onLog(quantity) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Log This Item", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MacroCardSmall(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(70.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text(value, fontWeight = FontWeight.Bold, color = color, fontSize = 18.sp)
        }
    }
}

@Composable
fun MacroMiniCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(0.1f))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, color = color, fontWeight = FontWeight.SemiBold)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun FlowNutrientRow(item: FoodItemUi, quantity: Double) {
    val nutrients = mutableListOf<Pair<String, String>>()
    if (item.vitaminA > 0) nutrients.add("Vitamin A" to "${(item.vitaminA * quantity).format(0)} mcg")
    if (item.vitaminC > 0) nutrients.add("Vitamin C" to "${(item.vitaminC * quantity).format(1)} mg")
    if (item.vitaminD > 0) nutrients.add("Vitamin D" to "${(item.vitaminD * quantity).format(1)} mcg")
    if (item.vitaminB12 > 0) nutrients.add("Vitamin B12" to "${(item.vitaminB12 * quantity).format(1)} mcg")
    if (item.calcium > 0) nutrients.add("Calcium" to "${(item.calcium * quantity).format(0)} mg")
    if (item.iron > 0) nutrients.add("Iron" to "${(item.iron * quantity).format(1)} mg")
    if (item.magnesium > 0) nutrients.add("Magnesium" to "${(item.magnesium * quantity).format(0)} mg")
    if (item.potassium > 0) nutrients.add("Potassium" to "${(item.potassium * quantity).format(0)} mg")
    if (item.sodium > 0) nutrients.add("Sodium" to "${(item.sodium * quantity).format(0)} mg")
    if (item.zinc > 0) nutrients.add("Zinc" to "${(item.zinc * quantity).format(1)} mg")

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        nutrients.chunked(2).forEach { pair ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEach { (name, value) ->
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(name, fontSize = 12.sp, color = Color.Gray)
                        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (pair.size < 2) Spacer(Modifier.weight(1f))
            }
        }
    }
}

private fun Double.format(digits: Int) = "%.${digits}f".format(this)

private val LogFoodGradientStart = Color(0xFFEF4444)
private val LogFoodGradientEnd = Color(0xFFDC2626)

@Composable
private fun LogFoodHeader(navController: NavController, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(
                brush = Brush.horizontalGradient(listOf(LogFoodGradientStart, LogFoodGradientEnd)),
                shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
            )
            .padding(top = 18.dp, start = 10.dp, end = 16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.2f), androidx.compose.foundation.shape.CircleShape)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text("🍴", fontSize = 22.sp)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Log Food",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Track your meals with AI",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 54.dp)
            )
        }
    }
}

@Composable
private fun TodayCaloriesCard(
    calories: Double,
    targetCalories: Double,
    protein: Double,
    carbs: Double,
    fats: Double,
    progress: Float
) {
    CalorieProgressCard(
        consumed = calories.roundToInt(),
        goal = targetCalories.roundToInt(),
        carbs = carbs.roundToInt(),
        protein = protein.roundToInt(),
        fat = fats.roundToInt()
    )
}

@Composable
private fun MacroMini(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun GradientActionCard(
    modifier: Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradient: Brush,
    onClick: () -> Unit,
    height: Dp = 120.dp
) {
    Card(
        modifier = modifier
            .height(height)
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun FoodSearchCard(
    query: String,
    onQueryChanged: (String) -> Unit,
    results: List<FoodItemUi>,
    localFoods: List<FoodItemUi>,
    onFoodPick: (FoodItemUi) -> Unit,
    onQuickAddLocal: (FoodItemUi) -> Unit
) {
    var showAllSuggested by remember { mutableStateOf(false) }
    val isDark = LocalDarkTheme.current

    val localMatches = remember(query) {
        localFoods.filter { it.name.contains(query, ignoreCase = true) }
    }
    val showOnline = results.isNotEmpty()
    val showLocalFallback = results.isEmpty() && localMatches.isNotEmpty()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Search Food",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChanged,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                placeholder = { Text("Search for food...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(Modifier.height(12.dp))

            if (query.length >= 2) {
                when {
                    showOnline -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            results.forEach { item ->
                                SearchResultRow(item, onClick = { onFoodPick(item) })
                            }
                        }
                    }

                    showLocalFallback -> {
                        Text("Showing local results", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            localMatches.forEach { item ->
                                SearchResultRow(
                                    item = item,
                                    isLocal = true,
                                    onClick = { onFoodPick(item) },
                                    onQuickAdd = { onQuickAddLocal(item) }
                                )
                            }
                        }
                    }

                    else -> {
                        Text("No foods found", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(8.dp))
                    }
                }
            } else {
                Text("Suggested Foods", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                
                val suggestedToShow = if (showAllSuggested) localFoods else localFoods.take(5)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    suggestedToShow.forEach { item ->
                        SearchResultRow(
                            item = item,
                            isLocal = true,
                            onClick = { onFoodPick(item) },
                            onQuickAdd = { onQuickAddLocal(item) }
                        )
                    }

                    if (!showAllSuggested && localFoods.size > 5) {
                        TextButton(
                            onClick = { showAllSuggested = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("See all suggested foods", color = if(isDark) Color(0xFF818CF8) else Color(0xFF4F46E5), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultRow(
    item: FoodItemUi,
    isLocal: Boolean = false,
    onClick: () -> Unit,
    onQuickAdd: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(item.name, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Text("${item.calories.roundToInt()} kcal • ${item.servingQuantity}${item.servingUnit}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (isLocal && onQuickAdd != null) {
            IconButton(onClick = onQuickAdd) {
                Icon(Icons.Default.Add, contentDescription = "Quick Add", tint = Color(0xFF10B981))
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CandidateFoodCard(
    item: FoodItemUi,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) Color(0xFF8B5CF6) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (selected) Color(0xFFF5F3FF) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (selected) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${item.calories.roundToInt()} kcal • ${item.protein.roundToInt()}P • ${item.carbs.roundToInt()}C • ${item.fats.roundToInt()}F",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                ConfidenceBadge(item.confidence)
            }

            if (item.source.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text(item.source) }
                )
            }
        }
    }
}

@Composable
private fun ConfidenceBadge(confidence: Double) {
    val label = when {
        confidence >= 0.80 -> "High confidence"
        confidence >= 0.55 -> "Medium confidence"
        else -> "Low confidence"
    }

    val color = when {
        confidence >= 0.80 -> Color(0xFF16A34A)
        confidence >= 0.55 -> Color(0xFFF59E0B)
        else -> Color(0xFFDC2626)
    }

    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = label,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfirmedFoodPanel(
    item: FoodItemUi,
    onLogFood: (FoodItemUi, Double) -> Unit,
    onManualEntry: () -> Unit,
    onRetryScan: (() -> Unit)? = null
) {
    var portions by remember(item.name) { mutableStateOf(1.0) }

    val isUncertain = item.name.equals("Uncertain result", ignoreCase = true) ||
            item.confidence <= 0.0 ||
            item.calories <= 0.0

    val displayName = if (isUncertain) "Couldn’t identify food" else item.name

    val confidenceLabel = when {
        item.confidence >= 0.80 -> "Confidence High"
        item.confidence >= 0.55 -> "Confidence Medium"
        else -> "Confidence Low"
    }

    val servingText = if (
        item.servingUnit.contains("apple", true) ||
        item.servingUnit.contains("banana", true) ||
        item.servingUnit.contains("burger", true) ||
        item.servingUnit.contains("sandwich", true) ||
        item.servingUnit.contains("slice", true) ||
        item.servingUnit.contains("idli", true) ||
        item.servingUnit.contains("dosa", true) ||
        item.servingUnit.contains("chapati", true)
    ) {
        "${portions}x ${item.servingUnit}"
    } else {
        "${portions}x ${item.servingQuantity.roundToInt()} ${item.servingUnit}"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(
            2.dp,
            Brush.linearGradient(
                listOf(
                    Color(0xFF10B981).copy(alpha = 0.25f),
                    Color(0xFF059669).copy(alpha = 0.25f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFF0FDF4).copy(alpha = 0.65f),
                            Color.White
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = if (isUncertain) {
                    "Retry or enter manually"
                } else {
                    "${item.servingQuantity.roundToInt()} ${item.servingUnit} • $confidenceLabel"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Macronutrients",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MacroCardSmall(
                    "Calories",
                    "${(item.calories * portions).roundToInt()}",
                    Color(0xFFEF4444),
                    Modifier.weight(1f)
                )
                MacroCardSmall(
                    "Protein",
                    "${(item.protein * portions).format(1)}g",
                    Color(0xFF2196F3),
                    Modifier.weight(1f)
                )
                MacroCardSmall(
                    "Carbs",
                    "${(item.carbs * portions).format(1)}g",
                    Color(0xFFFFC107),
                    Modifier.weight(1f)
                )
                MacroCardSmall(
                    "Fats",
                    "${(item.fats * portions).format(1)}g",
                    Color(0xFFE91E63),
                    Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MacroMiniCard(
                    "Fiber",
                    "${(item.fiber * portions).format(1)}g",
                    Color(0xFF4CAF50),
                    Modifier.weight(1f)
                )
                MacroMiniCard(
                    "Sugar",
                    "${(item.sugar * portions).format(1)}g",
                    Color(0xFF9C27B0),
                    Modifier.weight(1f)
                )
                MacroMiniCard(
                    "Sat. Fat",
                    "${(item.saturatedFat * portions).format(1)}g",
                    Color(0xFFFF9800),
                    Modifier.weight(1f)
                )
            }

            val hasVitamins = item.vitaminA > 0 || item.vitaminC > 0 || item.vitaminD > 0 || item.vitaminB12 > 0
            val hasMinerals = item.calcium > 0 || item.iron > 0 || item.magnesium > 0 || item.potassium > 0 || item.sodium > 0 || item.zinc > 0

            if (hasVitamins || hasMinerals) {
                Spacer(Modifier.height(20.dp))
                Text(
                    text = "Vitamins & Minerals",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                ) {
                    Column(Modifier.padding(12.dp)) {
                        FlowNutrientRow(item, portions)
                    }
                }
            }

            if (item.proTip.isNotBlank()) {
                Spacer(Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                    border = BorderStroke(2.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("✨", fontSize = 24.sp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "AI Recommendations for You",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = item.proTip,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            }

            if (item.healthAlternative.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    border = BorderStroke(2.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("❤️", fontSize = 24.sp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Healthier Alternatives",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = item.healthAlternative,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Gray.copy(alpha = 0.1f)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { if (!isUncertain && portions > 0.5) portions -= 0.5 },
                    enabled = !isUncertain
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = servingText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = if (isUncertain) Color.Gray else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Serving",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isUncertain) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { if (!isUncertain) portions += 0.5 },
                    enabled = !isUncertain
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = if (isUncertain) Color.Gray else Color(0xFFEF4444)
                    )
                }
            }

            if (isUncertain) {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "We couldn’t identify this food confidently. Please retry or enter the food manually.",
                    color = Color(0xFFB91C1C),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { onLogFood(item, portions) },
                enabled = !isUncertain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Text(
                    text = if (isUncertain) "Cannot Use Uncertain Result" else "Use This Result",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            if (isUncertain && onRetryScan != null) {
                OutlinedButton(
                    onClick = onRetryScan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Retry Scan")
                }

                Spacer(Modifier.height(12.dp))
            }

            OutlinedButton(
                onClick = onManualEntry,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Search / Enter Manually")
            }
        }
    }
}


@Composable
private fun EmptyScanState(
    onManualEntry: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "We couldn’t identify the food confidently",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Try a clearer image, better lighting, or choose the food manually.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onManualEntry, modifier = Modifier.fillMaxWidth()) {
            Text("Search / Enter Manually")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text("Close")
        }
    }
}

@Composable
private fun ScanTipsCard() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tips for best AI scan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text("• Capture one food item at a time")
            Text("• Use good lighting and close-up framing")
            Text("• Keep the food centered")
            Text("• Avoid blur, logos, and watermarks")
            Text("• For mixed meals, use Search / Enter Manual Entry")
        }
    }
}
