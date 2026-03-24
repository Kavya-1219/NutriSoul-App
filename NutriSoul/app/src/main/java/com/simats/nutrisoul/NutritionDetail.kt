package com.simats.nutrisoul

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun NutritionDetail(name: String, value: String) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.padding(horizontal = 2.dp)

    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(name, fontSize = 12.sp, color = Color.Gray)
            Text(
                value,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (name == "Calories") Color(0xFFFF6D00) else Color.Black
            )
        }
    }
}