package com.simats.nutrisoul.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomFoodDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(food: CustomFoodEntity)

    @Query("""
        SELECT * FROM custom_foods
        WHERE name LIKE '%' || :query || '%'
        ORDER BY name
    """)
    fun search(query: String): Flow<List<CustomFoodEntity>>
}
