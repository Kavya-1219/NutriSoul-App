package com.simats.nutrisoul

object FoodParser {
    fun extractFoods(text: String): List<String> {
        val t = text.lowercase()
        val candidates = listOf(
            "cake", "pizza", "burger", "sandwich", "biryani", "noodles", "pasta", "fries",
            "rice", "chapati", "roti", "milk", "egg", "banana", "apple", "chicken", "paneer", 
            "dal", "curd", "bread", "oats", "idli", "dosa", "vada", "samosa", "coffee", "tea"
        )
        return candidates.filter { t.contains(it) }
    }
}
