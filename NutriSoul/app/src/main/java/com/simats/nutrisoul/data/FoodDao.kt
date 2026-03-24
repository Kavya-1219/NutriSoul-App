package com.simats.nutrisoul.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface FoodDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFoodItem(foodItem: FoodItemEntity)

    @Query("SELECT * FROM food_items WHERE name LIKE :query || '%'")
    fun searchFood(query: String): Flow<List<FoodItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun logFood(loggedFood: LoggedFood)

    @Query("SELECT * FROM logged_foods WHERE timestamp >= :since")
    fun getLoggedFoodsAfter(since: Date): Flow<List<LoggedFood>>
}
