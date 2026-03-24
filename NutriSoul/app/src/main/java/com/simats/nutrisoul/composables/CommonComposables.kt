package com.simats.nutrisoul.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun UnitSelector(
    units: List<String>,
    selectedUnit: String,
    onUnitSelected: (String) -> Unit,
    activeColor: Color
) {
    Row(
        modifier = Modifier
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
    ) {
        units.forEach { unit ->
            Text(
                text = unit,
                modifier = Modifier
                    .background(if (unit == selectedUnit) activeColor else Color.Transparent)
                    .clickable { onUnitSelected(unit) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                color = if (unit == selectedUnit) Color.White else Color.Black,
                fontWeight = if (unit == selectedUnit) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }
    }
}
