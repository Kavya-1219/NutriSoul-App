package com.simats.nutrisoul

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.nutrisoul.ui.FoodItemUi
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailsBottomSheet(
    foodItem: FoodItemUi,
    onDismiss: () -> Unit,
    onLogFood: (FoodItemUi, Double) -> Unit
) {
    var qty by remember { mutableStateOf(foodItem.servingQuantity) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color.White
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(foodItem.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(Modifier.height(12.dp))

            val isPieceBased = foodItem.servingUnit.lowercase() in listOf("piece", "serving", "unit", "cup", "bowl", "plate")
            val serving = if (foodItem.servingQuantity <= 0) 100.0 else foodItem.servingQuantity
            
            val (calories, protein, carbs, fats) = if (isPieceBased) {
                // If it's a piece (e.g. 1 Apple = 52 kcal), simply multiply by quantity
                listOf(foodItem.calories * qty, foodItem.protein * qty, foodItem.carbs * qty, foodItem.fats * qty)
            } else {
                // If it's weight-based (e.g. 100g = 364 kcal), use the ratio
                listOf(
                    foodItem.calories * qty / serving,
                    foodItem.protein * qty / serving,
                    foodItem.carbs * qty / serving,
                    foodItem.fats * qty / serving
                )
            }
            
            NutritionGrid(
                calories = calories,
                protein = protein,
                carbs = carbs,
                fats = fats
            )

            Spacer(Modifier.height(16.dp))

            // Qty selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFF9FAFB))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val step = if (isPieceBased) 1.0 else if (foodItem.servingUnit.lowercase() in listOf("g", "ml")) (serving / 2).coerceAtLeast(50.0) else 1.0
                val minQty = if (isPieceBased) 1.0 else if (foodItem.servingUnit.lowercase() in listOf("g", "ml")) (serving / 10).coerceAtLeast(10.0) else 1.0

                IconButton(onClick = { qty = (qty - step).coerceAtLeast(minQty) }) {
                    Icon(Icons.Default.Remove, contentDescription = "Minus")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val displayValue = if (isPieceBased) qty.roundToInt().toString() else "%.1f".format(qty)
                    Text("$displayValue${foodItem.servingUnit}", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Quantity", fontSize = 12.sp, color = Color(0xFF6B7280))
                }
                IconButton(onClick = { qty += step }) {
                    Icon(Icons.Default.Add, contentDescription = "Plus")
                }
            }

            Spacer(Modifier.height(14.dp))

            Button(
                onClick = {
                    if (qty <= 0) return@Button
                    onLogFood(foodItem, qty)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFFFF7A18), Color(0xFFFF3D3D))),
                            RoundedCornerShape(18.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Log Food", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun NutritionGrid(calories: Double, protein: Double, carbs: Double, fats: Double) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        NutrBox("Calories", calories.roundToInt().toString(), Color(0xFFFF5722), Modifier.weight(1f))
        NutrBox("Protein", "${protein.roundToInt()}g", Color(0xFF111827), Modifier.weight(1f))
        NutrBox("Carbs", "${carbs.roundToInt()}g", Color(0xFF111827), Modifier.weight(1f))
        NutrBox("Fats", "${fats.roundToInt()}g", Color(0xFF111827), Modifier.weight(1f))
    }
}

@Composable
private fun NutrBox(title: String, value: String, valueColor: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontSize = 12.sp, color = Color(0xFF6B7280))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}


