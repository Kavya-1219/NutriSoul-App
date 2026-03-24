package com.simats.nutrisoul

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.nutrisoul.ui.theme.LocalDarkTheme
import kotlinx.coroutines.launch

data class Recipe(
    val id: Int,
    val name: String,
    val category: String,
    val cookTime: String,
    val calories: Int,
    val servings: Int,
    val difficulty: String,
    val ingredients: List<String>,
    val instructions: List<String>,
    val protein: Int,
    val carbs: Int,
    val fats: Int,
    val fiber: Int,
    val image: String
)

private const val PREFS_NAME = "recipes_prefs"

private fun favoritesKeyFor(email: String): String {
    val safe = email.trim().lowercase().replace("[^a-z0-9@._-]".toRegex(), "_")
    return "favorite_recipe_ids__$safe"
}

private fun loadFavorites(context: Context, email: String): Set<Int> {
    if (email.isBlank()) return emptySet()
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val raw = prefs.getStringSet(favoritesKeyFor(email), emptySet()) ?: emptySet()
    return raw.mapNotNull { it.toIntOrNull() }.toSet()
}

private fun saveFavorites(context: Context, email: String, favorites: Set<Int>) {
    if (email.isBlank()) return
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit()
        .putStringSet(favoritesKeyFor(email), favorites.map { it.toString() }.toSet())
        .apply()
}

private enum class RecipeFilter(val label: String) {
    NONE("All"),
    HIGH_PROTEIN("High Protein"),
    LOW_CARB("Low Carb"),
    HIGH_FIBER("High Fiber"),
    LOW_CAL("Low Calories")
}

private val recipes = listOf(
    // -------------------- BREAKFAST --------------------
    Recipe(
        id = 1, name = "Masala Oats Upma", category = "breakfast", cookTime = "15 mins",
        calories = 180, servings = 2, difficulty = "Easy", protein = 6, carbs = 28, fats = 4, fiber = 5,
        image = "🥣",
        ingredients = listOf("1 cup rolled oats","1 onion, chopped","1 tomato, chopped","1 green chili, chopped","1/2 tsp mustard seeds","Curry leaves","1/2 tsp turmeric","Salt","2 cups water","Fresh coriander"),
        instructions = listOf("Dry roast oats 3-4 minutes.","Temper mustard + curry leaves.","Sauté onion + chili.","Add tomatoes + turmeric + salt.","Add water; boil.","Add oats; cook 3-4 minutes.","Garnish and serve.")
    ),
    Recipe(
        id = 2, name = "Moong Dal Cheela", category = "breakfast", cookTime = "20 mins",
        calories = 220, servings = 2, difficulty = "Easy", protein = 12, carbs = 30, fats = 5, fiber = 8,
        image = "🥞",
        ingredients = listOf("1 cup moong dal (soaked)","1 onion, chopped","1 green chili","1 inch ginger","1/2 tsp cumin","Salt","Coriander","Oil"),
        instructions = listOf("Grind dal with ginger + chili.","Add onion, cumin, salt, coriander.","Cook on pan both sides.","Serve with chutney/yogurt.")
    ),
    Recipe(
        id = 3, name = "Vegetable Poha", category = "breakfast", cookTime = "15 mins",
        calories = 200, servings = 2, difficulty = "Easy", protein = 4, carbs = 35, fats = 6, fiber = 3,
        image = "🍚",
        ingredients = listOf("2 cups poha","1 onion","1 potato","1/2 cup peas","1 green chili","mustard","turmeric","curry leaves","peanuts","lemon","coriander"),
        instructions = listOf("Rinse poha; drain.","Temper mustard + curry leaves + peanuts.","Cook onion + veggies.","Add turmeric + salt.","Mix poha gently.","Finish with lemon + coriander.")
    ),
    Recipe(
        id = 13, name = "Idli Sambar (Light & Protein)", category = "breakfast", cookTime = "25 mins",
        calories = 240, servings = 2, difficulty = "Medium", protein = 10, carbs = 40, fats = 3, fiber = 6,
        image = "🍘",
        ingredients = listOf("4 idlis","1 cup sambar","Mixed veggies","Mustard","Curry leaves","Salt"),
        instructions = listOf("Steam/prepare idlis.","Heat sambar and simmer.","Temper mustard + curry leaves.","Serve hot.")
    ),
    Recipe(
        id = 14, name = "Ragi Dosa with Veg Filling", category = "breakfast", cookTime = "20 mins",
        calories = 260, servings = 2, difficulty = "Medium", protein = 8, carbs = 42, fats = 6, fiber = 7,
        image = "🫓",
        ingredients = listOf("1 cup ragi flour","Salt","Water","Onion, carrot, capsicum","1 tsp oil"),
        instructions = listOf("Make thin batter.","Spread on pan.","Add veg filling; fold.","Serve with chutney.")
    ),

    // -------------------- LUNCH --------------------
    Recipe(
        id = 4, name = "Dal Tadka", category = "lunch", cookTime = "30 mins",
        calories = 180, servings = 4, difficulty = "Medium", protein = 10, carbs = 28, fats = 4, fiber = 12,
        image = "🍲",
        ingredients = listOf("1 cup toor dal","tomatoes","onion","chilies","cumin","garlic","turmeric","chili powder","garam masala","coriander","ghee"),
        instructions = listOf("Cook dal.","Make tadka.","Mix and simmer.","Garnish and serve.")
    ),
    Recipe(
        id = 6, name = "Rajma Masala", category = "lunch", cookTime = "40 mins",
        calories = 240, servings = 4, difficulty = "Medium", protein = 14, carbs = 38, fats = 4, fiber = 11,
        image = "🫘",
        ingredients = listOf("1 cup rajma","onion","tomato","ginger-garlic","cumin","spices","coriander"),
        instructions = listOf("Cook rajma.","Cook masala base.","Simmer with rajma 15-20 min.","Serve.")
    ),
    Recipe(
        id = 15, name = "Quinoa Veg Pulao", category = "lunch", cookTime = "25 mins",
        calories = 280, servings = 2, difficulty = "Easy", protein = 10, carbs = 45, fats = 6, fiber = 8,
        image = "🥘",
        ingredients = listOf("1 cup quinoa","mixed veggies","jeera","ginger","chili","salt","1 tsp oil"),
        instructions = listOf("Rinse quinoa.","Temper jeera; add ginger/chili.","Sauté veggies.","Cook quinoa 1:2 water.","Serve with curd.")
    ),
    Recipe(
        id = 16, name = "Chole (Healthy Chickpea Curry)", category = "lunch", cookTime = "35 mins",
        calories = 290, servings = 3, difficulty = "Medium", protein = 14, carbs = 42, fats = 7, fiber = 11,
        image = "🫛",
        ingredients = listOf("1 cup chickpeas","onion","tomato","ginger-garlic","chole masala","cumin","lemon","coriander"),
        instructions = listOf("Cook chickpeas.","Cook masala.","Simmer 10-12 min.","Finish with lemon + coriander.")
    ),

    // -------------------- SNACKS --------------------
    Recipe(
        id = 7, name = "Sprouts Chaat", category = "snack", cookTime = "10 mins",
        calories = 150, servings = 2, difficulty = "Easy", protein = 8, carbs = 22, fats = 3, fiber = 6,
        image = "🥗",
        ingredients = listOf("sprouts","onion","tomato","cucumber","chili","chaat masala","lemon","coriander"),
        instructions = listOf("Boil sprouts 5 minutes.","Mix everything.","Add lemon and serve.")
    ),
    Recipe(
        id = 8, name = "Masala Roasted Makhana", category = "snack", cookTime = "10 mins",
        calories = 120, servings = 2, difficulty = "Easy", protein = 4, carbs = 18, fats = 4, fiber = 2,
        image = "🍿",
        ingredients = listOf("makhana","1 tsp ghee","chaat masala","turmeric","chili powder","salt"),
        instructions = listOf("Roast 6-7 minutes.","Add spices.","Cool and eat.")
    ),
    Recipe(
        id = 17, name = "Greek Yogurt Fruit Bowl", category = "snack", cookTime = "5 mins",
        calories = 190, servings = 1, difficulty = "Easy", protein = 12, carbs = 22, fats = 5, fiber = 4,
        image = "🍓",
        ingredients = listOf("1 cup Greek yogurt","fruits","chia/flax seeds","honey (optional)"),
        instructions = listOf("Add yogurt.","Top with fruits + seeds.","Optional honey.")
    ),
    Recipe(
        id = 18, name = "Paneer & Cucumber Sandwich", category = "snack", cookTime = "10 mins",
        calories = 260, servings = 1, difficulty = "Easy", protein = 16, carbs = 26, fats = 10, fiber = 5,
        image = "🥪",
        ingredients = listOf("whole wheat bread","paneer","cucumber","mint chutney","salt/pepper"),
        instructions = listOf("Season paneer.","Layer cucumber + paneer.","Toast optional; serve.")
    ),

    // -------------------- DINNER --------------------
    Recipe(
        id = 10, name = "Palak Paneer", category = "dinner", cookTime = "30 mins",
        calories = 280, servings = 3, difficulty = "Medium", protein = 16, carbs = 10, fats = 20, fiber = 4,
        image = "🥬",
        ingredients = listOf("paneer","spinach","onion","tomato","ginger-garlic","cumin","garam masala","salt"),
        instructions = listOf("Blanch spinach; blend.","Cook masala.","Add spinach; simmer.","Add paneer; finish spices.")
    ),
    Recipe(
        id = 11, name = "Chicken Curry (Home Style)", category = "dinner", cookTime = "40 mins",
        calories = 320, servings = 4, difficulty = "Medium", protein = 35, carbs = 8, fats = 18, fiber = 2,
        image = "🍗",
        ingredients = listOf("chicken","onion","tomato","ginger-garlic","spices","curry leaves","coriander"),
        instructions = listOf("Cook onion masala.","Add chicken.","Simmer covered until tender.","Garnish and serve.")
    ),
    Recipe(
        id = 12, name = "Vegetable Khichdi", category = "dinner", cookTime = "25 mins",
        calories = 210, servings = 3, difficulty = "Easy", protein = 8, carbs = 38, fats = 4, fiber = 6,
        image = "🍛",
        ingredients = listOf("rice","moong dal","veggies","jeera","turmeric","ginger","salt","ghee"),
        instructions = listOf("Wash rice+dal.","Cook with veggies and spices.","Pressure cook soft.","Finish with ghee.")
    ),
    Recipe(
        id = 19, name = "Tandoori Fish (Pan/OTG)", category = "dinner", cookTime = "25 mins",
        calories = 260, servings = 2, difficulty = "Easy", protein = 30, carbs = 6, fats = 12, fiber = 1,
        image = "🐟",
        ingredients = listOf("fish fillet","curd","tandoori masala","lemon","salt","1 tsp oil"),
        instructions = listOf("Marinate 15 minutes.","Pan grill or bake.","Serve with salad.")
    ),
    Recipe(
        id = 20, name = "Mixed Veg Soup (Light Dinner)", category = "dinner", cookTime = "20 mins",
        calories = 140, servings = 2, difficulty = "Easy", protein = 6, carbs = 22, fats = 3, fiber = 5,
        image = "🥣",
        ingredients = listOf("carrot","beans","tomato","garlic","salt","pepper","water/stock"),
        instructions = listOf("Sauté garlic.","Add veggies + stock.","Boil 12-15 minutes.","Season and serve.")
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesScreen(navController: NavController, userEmail: String) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }
    var favorites by remember { mutableStateOf(setOf<Int>()) }
    var selectedFilter by remember { mutableStateOf(RecipeFilter.NONE) }

    LaunchedEffect(userEmail) {
        // Clear instantly so you never see old user’s favorites even for a moment
        favorites = emptySet()
        searchQuery = ""
        selectedCategory = "all"
        selectedRecipe = null
        selectedFilter = RecipeFilter.NONE

        favorites = loadFavorites(context, userEmail)
    }

    fun toggleFavorite(id: Int) {
        if (userEmail.isBlank()) return
        favorites = if (favorites.contains(id)) favorites - id else favorites + id
        saveFavorites(context, userEmail, favorites)
    }

    val categories = listOf(
        "all" to "🍽️ All",
        "favorites" to "❤️ Favorites",
        "breakfast" to "🌅 Breakfast",
        "lunch" to "☀️ Lunch",
        "snack" to "🍪 Snacks",
        "dinner" to "🌙 Dinner"
    )

    val filteredRecipes = recipes
        .asSequence()
        .filter { recipe ->
            when (selectedCategory) {
                "all" -> true
                "favorites" -> favorites.contains(recipe.id)
                else -> recipe.category == selectedCategory
            }
        }
        .filter { it.name.contains(searchQuery, ignoreCase = true) }
        .filter { recipe ->
            when (selectedFilter) {
                RecipeFilter.NONE -> true
                RecipeFilter.HIGH_PROTEIN -> recipe.protein >= 15
                RecipeFilter.LOW_CARB -> recipe.carbs <= 20
                RecipeFilter.HIGH_FIBER -> recipe.fiber >= 7
                RecipeFilter.LOW_CAL -> recipe.calories <= 200
            }
        }
        .toList()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    BackHandler(enabled = selectedRecipe != null) { selectedRecipe = null }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Header(searchQuery = searchQuery, onSearchQueryChange = { searchQuery = it })

            CategoryTabs(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            FiltersRow(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )

            if (filteredRecipes.isEmpty()) {
                EmptyState(
                    isFavorites = selectedCategory == "favorites"
                )
            } else {
                RecipesGrid(
                    recipes = filteredRecipes,
                    favorites = favorites,
                    onRecipeClick = { selectedRecipe = it },
                    onToggleFavorite = { toggleFavorite(it) }
                )
            }
        }
    }

    if (selectedRecipe != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedRecipe = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            RecipeDetailSheet(
                recipe = selectedRecipe!!,
                isFavorite = favorites.contains(selectedRecipe!!.id),
                onClose = { selectedRecipe = null },
                onToggleFavorite = { toggleFavorite(it) }
            )
        }
    }
}

@Composable
private fun Header(searchQuery: String, onSearchQueryChange: (String) -> Unit) {
    val isDark = LocalDarkTheme.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFF59E0B), Color(0xFFF97316))
                )
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Restaurant, contentDescription = "Recipes", tint = Color.White, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Recipes", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text("Healthy Indian recipes for every meal", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
        Spacer(modifier = Modifier.height(14.dp))

        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search recipes...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = if(isDark) Color(0xFF333333) else Color.White,
                unfocusedContainerColor = if(isDark) Color(0xFF333333) else Color.White,
                disabledContainerColor = if(isDark) Color(0xFF333333) else Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = if(isDark) Color.White else Color.Black,
                unfocusedTextColor = if(isDark) Color.White else Color.Black
            )
        )
    }
}

@Composable
private fun CategoryTabs(
    categories: List<Pair<String, String>>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val selectedTabIndex = categories.indexOfFirst { it.first == selectedCategory }.coerceAtLeast(0)

    ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        edgePadding = 12.dp,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                color = Color(0xFFFFB74D)
            )
        }
    ) {
        categories.forEachIndexed { index, (id, label) ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onCategorySelected(id) },
                text = {
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltersRow(
    selectedFilter: RecipeFilter,
    onFilterSelected: (RecipeFilter) -> Unit
) {
    val scroll = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .horizontalScroll(scroll),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        RecipeFilter.entries.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.label) },
                leadingIcon = if (selectedFilter == filter) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null
            )
        }
    }
}

@Composable
private fun ColumnScope.RecipesGrid(
    recipes: List<Recipe>,
    favorites: Set<Int>,
    onRecipeClick: (Recipe) -> Unit,
    onToggleFavorite: (Int) -> Unit
) {
    LazyVerticalGrid(
        modifier = Modifier.weight(1f),
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(recipes) { recipe ->
            RecipeCard(
                recipe = recipe,
                isFavorite = favorites.contains(recipe.id),
                onClick = { onRecipeClick(recipe) },
                onToggleFavorite = { onToggleFavorite(recipe.id) }
            )
        }
    }
}

@Composable
private fun RecipeCard(
    recipe: Recipe,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val isDark = LocalDarkTheme.current
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if(isDark) listOf(Color(0xFF422100), Color(0xFF5D3D00)) else {
                                when(recipe.category.lowercase()) {
                                    "breakfast" -> listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2))
                                    "lunch" -> listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9))
                                    "snack" -> listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))
                                    "dinner" -> listOf(Color(0xFFEDE7F6), Color(0xFFD1C4E9))
                                    else -> listOf(Color(0xFFF5F5F5), Color(0xFFEEEEEE))
                                }
                            }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = recipe.image, fontSize = 52.sp)

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if(isDark) Color(0xFF333333).copy(alpha=0.6f) else Color.White.copy(alpha=0.8f))
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color(0xFFE53935) else Color.Gray
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = recipe.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                MiniRow(Icons.Default.Schedule, recipe.cookTime)
                MiniRow(Icons.Default.Whatshot, "${recipe.calories} kcal")
                MiniRow(Icons.Default.Group, "${recipe.servings} servings")
                Spacer(modifier = Modifier.height(8.dp))
                DifficultyChip(recipe.difficulty)
            }
        }
    }
}

@Composable
private fun MiniRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DifficultyChip(difficulty: String) {
    val isDark = LocalDarkTheme.current
    val bg = if (difficulty == "Easy") {
        if(isDark) Color(0xFF1B5E20) else Color(0xFFC8E6C9)
    } else {
        if(isDark) Color(0xFFE65100) else Color(0xFFFFE0B2)
    }
    val fg = if (difficulty == "Easy") {
        if(isDark) Color(0xFFC8E6C9) else Color(0xFF2E7D32)
    } else {
        if(isDark) Color(0xFFFFE0B2) else Color(0xFFEF6C00)
    }

    Text(
        text = difficulty,
        color = fg,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

@Composable
private fun EmptyState(isFavorites: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            if (isFavorites) Icons.Default.FavoriteBorder else Icons.Default.SearchOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            if (isFavorites) "No favorites yet" else "No recipes found",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            if (isFavorites) "Tap ❤️ on any recipe to save it here." else "Try another keyword, category, or filter.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun RecipeDetailSheet(
    recipe: Recipe,
    isFavorite: Boolean,
    onClose: () -> Unit,
    onToggleFavorite: (Int) -> Unit
) {
    val isDark = LocalDarkTheme.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 18.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .background(
                    brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFF59E0B), Color(0xFFF97316))
                )
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = recipe.image, fontSize = 92.sp)

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f))
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(recipe.name, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoChip("Time", recipe.cookTime, Icons.Default.Schedule)
                InfoChip("Calories", "${recipe.calories} kcal", Icons.Default.Whatshot)
                InfoChip("Servings", "${recipe.servings}", Icons.Default.Group)
            }

            Spacer(modifier = Modifier.height(14.dp))
            NutritionCard(recipe)

            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle("Ingredients")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    recipe.ingredients.forEach {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FiberManualRecord, contentDescription = null, modifier = Modifier.size(8.dp), tint = Color(0xFFFFB74D))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(it, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle("Instructions")
            recipe.instructions.forEachIndexed { index, instruction ->
                Row(modifier = Modifier.padding(bottom = 10.dp), verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFB74D)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${index + 1}", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(instruction, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 3.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = { onToggleFavorite(recipe.id) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFavorite) Color(0xFFE53935) else Color(0xFFFFB74D)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isFavorite) "Remove from Favorites" else "Add to Favorites", color = Color.White)
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun InfoChip(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFFFFB74D), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun NutritionCard(recipe: Recipe) {
    val isDark = LocalDarkTheme.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if(isDark) Color(0xFF333333) else Color(0xFFFFF8F0)),
        border = BorderStroke(1.dp, Color(0xFFFFB74D).copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Macro("Protein", "${recipe.protein}g", Color(0xFF1D4ED8))
            Macro("Carbs", "${recipe.carbs}g", Color(0xFF0F766E))
            Macro("Fats", "${recipe.fats}g", Color(0xFF6D28D9))
            Macro("Fiber", "${recipe.fiber}g", Color(0xFF15803D))
        }
    }
}

@Composable
private fun Macro(label: String, value: String, color: Color) {
    val isDark = LocalDarkTheme.current
    val adjustedColor = if(isDark) {
        when(label) {
            "Protein" -> Color(0xFF60A5FA)
            "Carbs" -> Color(0xFF2DD4BF)
            "Fats" -> Color(0xFFA78BFA)
            "Fiber" -> Color(0xFF4ADE80)
            else -> color
        }
    } else color

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(adjustedColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = adjustedColor)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
    }
}


