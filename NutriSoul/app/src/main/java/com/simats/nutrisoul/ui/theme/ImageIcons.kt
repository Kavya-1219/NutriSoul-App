package com.simats.nutrisoul.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.simats.nutrisoul.R

object ImageIcons {
    val Home @Composable get() = painterResource(id = R.drawable.home)
    val MindCare @Composable get() = painterResource(id = R.drawable.stress_and_sleep)
    val Recipes @Composable get() = painterResource(id = R.drawable.recipes)
    val Insights @Composable get() = painterResource(id = R.drawable.insights)
    val Settings @Composable get() = painterResource(id = R.drawable.settings)
}
