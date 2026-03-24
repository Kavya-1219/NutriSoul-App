package com.simats.nutrisoul.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface StepsDao {
    @Upsert
    suspend fun upsert(steps: StepsEntity)

    @Query("SELECT * FROM daily_steps WHERE date = :date")
    fun getStepsForDate(date: LocalDate): Flow<StepsEntity?>

    @Query("SELECT * FROM daily_steps WHERE date >= :startDate ORDER BY date DESC")
    fun getStepsFrom(startDate: LocalDate): Flow<List<StepsEntity>>
}
