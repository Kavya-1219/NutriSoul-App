package com.simats.nutrisoul.data.meal

import com.simats.nutrisoul.data.meal.model.Meal
import com.simats.nutrisoul.data.meal.model.MealItem
import com.simats.nutrisoul.data.meal.model.MealPlan
import java.time.LocalDate
import kotlin.random.Random

class MealPlanGenerator {

    // Starter catalog (you can add more anytime; app will still work perfectly now)

    private val breakfastPool = listOf(
        meal("breakfast", "Oats with Banana & Nuts", 380, 14, 58, 10,
            listOf(
                item("Oats", "60g", 228, 7, 39, 4),
                item("Banana", "1 medium", 105, 1, 27, 0),
                item("Milk", "200ml", 47, 3, 5, 2)
            )
        ),
        meal("breakfast", "Vegetable Poha", 420, 12, 72, 10,
            listOf(
                item("Poha", "80g", 290, 6, 60, 4),
                item("Veggies", "100g", 50, 2, 10, 0),
                item("Peanuts", "10g", 80, 4, 2, 6)
            )
        ),
        meal("breakfast", "Paneer Sandwich with Veggies", 323, 16, 31, 16,
            listOf(
                item("Whole Wheat Bread", "2 slices", 140, 6, 26, 2),
                item("Paneer", "50g", 132, 9, 2, 10),
                item("Vegetables (tomato, cucumber)", "50g", 15, 1, 3, 0),
                item("Butter", "5g", 36, 0, 0, 4)
            )
        )
    )

    private val lunchPool = listOf(
        meal("lunch", "Dal + Brown Rice + Salad", 540, 22, 86, 10,
            listOf(
                item("Dal", "200g", 240, 16, 34, 6),
                item("Brown Rice", "180g cooked", 210, 5, 44, 2),
                item("Salad", "150g", 90, 1, 8, 2)
            )
        ),
        meal("lunch", "Quinoa Paneer Bowl", 560, 28, 62, 20,
            listOf(
                item("Quinoa", "180g cooked", 220, 8, 39, 4),
                item("Paneer", "80g", 210, 14, 4, 16),
                item("Veggies", "150g", 130, 6, 19, 0)
            )
        ),
        meal("lunch", "Paneer Curry with Quinoa", 457, 25, 42, 21,
            listOf(
                item("Quinoa", "150g cooked", 167, 6, 29, 3),
                item("Paneer Curry", "150g", 265, 18, 8, 18),
                item("Salad", "100g", 25, 1, 5, 0)
            )
        )
    )

    private val dinnerPool = listOf(
        meal("dinner", "Roti + Palak Paneer", 520, 26, 58, 18,
            listOf(
                item("Roti", "2 medium", 142, 6, 30, 1),
                item("Palak Paneer", "200g", 318, 18, 22, 17),
                item("Curd", "100g", 60, 4, 6, 2)
            )
        ),
        meal("dinner", "Veg Khichdi + Curd", 480, 18, 72, 12,
            listOf(
                item("Khichdi", "300g", 380, 14, 62, 10),
                item("Curd", "150g", 100, 4, 10, 2)
            )
        ),
        meal("dinner", "Roti with Palak Paneer", 382, 22, 43, 16,
            listOf(
                item("Roti", "2 medium", 142, 6, 30, 1),
                item("Palak Paneer", "150g", 180, 12, 8, 12),
                item("Curd", "100g", 60, 4, 6, 2)
            )
        )
    )

    private val snackPool = listOf(
        meal("snack", "Sprouts Salad", 180, 14, 22, 4,
            listOf(
                item("Mixed Sprouts", "150g", 135, 12, 22, 2),
                item("Lemon & Spices", "10g", 10, 0, 2, 0),
                item("Cucumber", "100g", 35, 2, 8, 0)
            )
        ),
        meal("snack", "Greek Yogurt Bowl", 200, 16, 22, 5,
            listOf(
                item("Greek Yogurt", "200g", 150, 15, 12, 4),
                item("Berries", "80g", 50, 1, 10, 1)
            )
        ),
        meal("snack", "Sprouts Salad (Light)", 95, 8, 16, 1,
            listOf(
                item("Mixed Sprouts", "100g", 90, 8, 15, 1),
                item("Lemon & Spices", "10g", 5, 0, 1, 0)
            )
        )
    )

    fun generate(profile: UserNutritionProfile, seed: String): MealPlan {
        val rng = Random(seed.hashCode())

        val b = pickStable(filterPool(breakfastPool, profile), rng)
        val l = pickStable(filterPool(lunchPool, profile), rng)
        val d = pickStable(filterPool(dinnerPool, profile), rng)
        val s = pickStable(filterPool(snackPool, profile), rng)

        return MealPlan(
            targetCalories = profile.targetCalories,
            meals = listOf(b, l, d, s)
        )
    }

    fun alternativesFor(mealType: String, profile: UserNutritionProfile): List<Meal> {
        val pool = when (mealType.lowercase()) {
            "breakfast" -> breakfastPool
            "lunch" -> lunchPool
            "dinner" -> dinnerPool
            "snack" -> snackPool
            else -> emptyList()
        }
        return filterPool(pool, profile).take(5)
    }

    fun todaySeed(userKey: String): String = "$userKey-${LocalDate.now()}"

    private fun filterPool(pool: List<Meal>, profile: UserNutritionProfile): List<Meal> {
        val allergies = profile.allergies.map { it.lowercase() }
        return pool.filter { meal ->
            val text = (meal.title + " " + meal.items.joinToString(" ") { it.name }).lowercase()
            allergies.none { a -> a.isNotBlank() && text.contains(a) }
        }
    }

    private fun pickStable(pool: List<Meal>, rng: Random): Meal {
        if (pool.isEmpty()) {
            return meal("snack", "Custom Meal", 300, 10, 40, 10, emptyList())
        }
        return pool[rng.nextInt(pool.size)]
    }

    private fun meal(
        type: String,
        title: String,
        c: Int,
        p: Int,
        cb: Int,
        f: Int,
        items: List<MealItem>
    ) = Meal(id = 0, mealType = type, title = title, calories = c, protein = p, carbs = cb, fats = f, items = items)

    private fun item(
        name: String,
        qty: String,
        c: Int,
        p: Int,
        cb: Int,
        f: Int
    ) = MealItem(name, qty, c, p, cb, f)
}
