package com.simats.nutrisoul.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.simats.nutrisoul.data.models.DailyTotals
import kotlinx.coroutines.flow.Flow

@Dao
interface IntakeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: IntakeEntity)

    @Query("""
        SELECT * FROM daily_intake
        WHERE date = :date AND LOWER(userEmail) = LOWER(:email)
        ORDER BY id DESC
    """)
    fun getForDate(date: String, email: String): Flow<List<IntakeEntity>>

    @Query("""
        SELECT * FROM daily_intake
        WHERE LOWER(userEmail) = LOWER(:email) 
          AND date BETWEEN :startDate AND :endDate
        ORDER BY date DESC, timestamp DESC
    """)
    fun getLogsBetween(email: String, startDate: String, endDate: String): Flow<List<IntakeEntity>>

    @Query("""
        SELECT
            IFNULL(SUM(calories), 0) as calories,
            IFNULL(SUM(protein), 0) as protein,
            IFNULL(SUM(carbs), 0) as carbs,
            IFNULL(SUM(fats), 0) as fats
        FROM daily_intake
        WHERE date = :date AND LOWER(userEmail) = LOWER(:email)
    """)
    fun observeTotalsForDate(date: String, email: String): Flow<DailyTotals>

    @Query("""
        DELETE FROM daily_intake 
        WHERE date = :date AND LOWER(userEmail) = LOWER(:email) 
          AND name = :name AND mealType = :mealType
    """)
    suspend fun deleteForMeal(date: String, email: String, name: String, mealType: String)
}
