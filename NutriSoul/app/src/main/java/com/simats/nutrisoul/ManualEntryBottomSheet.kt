package com.simats.nutrisoul

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

data class ManualFoodInput(
    val name: String,
    val quantity: Double,
    val unit: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualEntryBottomSheet(
    onDismiss: () -> Unit,
    onSave: (ManualFoodInput) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fats by remember { mutableStateOf("") }

    // Unit dropdown
    val unitOptions = listOf("g", "ml", "piece", "cup", "tbsp")
    var unitExpanded by remember { mutableStateOf(false) }
    var unit by remember { mutableStateOf("g") }

    val qtyVal = qty.toDoubleOrNull()
    val calVal = calories.toDoubleOrNull()

    val canSave =
        name.trim().isNotBlank() &&
        qtyVal != null && qtyVal > 0.0 &&
        calVal != null && calVal >= 0.0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .windowInsetsPadding(WindowInsets.ime) // ✅ avoids keyboard overlap
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Manual Food Entry", fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Food name") },
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = qty,
                    onValueChange = { qty = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Quantity") },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    )
                )

                ExposedDropdownMenuBox(
                    expanded = unitExpanded,
                    onExpandedChange = { unitExpanded = !unitExpanded },
                    modifier = Modifier.width(130.dp)
                ) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                        modifier = Modifier.menuAnchor(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    ExposedDropdownMenu(
                        expanded = unitExpanded,
                        onDismissRequest = { unitExpanded = false }
                    ) {
                        unitOptions.forEach { u ->
                            DropdownMenuItem(
                                text = { Text(u) },
                                onClick = {
                                    unit = u
                                    unitExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = calories,
                onValueChange = { calories = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Calories (total for this quantity)") },
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = protein,
                    onValueChange = { protein = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Protein (g)") },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
                )
                OutlinedTextField(
                    value = carbs,
                    onValueChange = { carbs = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Carbs (g)") },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
                )
                OutlinedTextField(
                    value = fats,
                    onValueChange = { fats = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Fats (g)") },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done)
                )
            }

            Spacer(Modifier.height(14.dp))

            Button(
                onClick = {
                    val safeQty = qty.toDoubleOrNull() ?: return@Button
                    val safeCalories = calories.toDoubleOrNull() ?: return@Button

                    onSave(
                        ManualFoodInput(
                            name = name.trim(),
                            quantity = safeQty,
                            unit = unit,
                            calories = safeCalories,
                            protein = (protein.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0),
                            carbs = (carbs.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0),
                            fats = (fats.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
                        )
                    )
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669))),
                            RoundedCornerShape(18.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Save & Add to Today", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}


