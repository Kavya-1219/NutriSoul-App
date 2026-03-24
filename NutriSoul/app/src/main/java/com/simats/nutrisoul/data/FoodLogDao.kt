package com.simats.nutrisoul.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: FoodLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<FoodLogEntity>)

    @Query("""
        SELECT * FROM food_logs
        WHERE userEmail = :email
          AND timestampMillis BETWEEN :startMillis AND :endMillis
        ORDER BY timestampMillis DESC
    """)
    fun observeLogsBetween(
        email: String,
        startMillis: Long,
        endMillis: Long
    ): Flow<List<FoodLogEntity>>
}
