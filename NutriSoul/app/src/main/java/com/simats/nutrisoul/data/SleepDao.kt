package com.simats.nutrisoul.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepDao {
    @Query("SELECT * FROM sleep_logs ORDER BY date DESC")
    fun getAllSleepLogs(): Flow<List<SleepLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepLog(log: SleepLogEntity)

    @Query("SELECT * FROM sleep_schedule WHERE id = 1")
    fun getSleepSchedule(): Flow<SleepScheduleEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSleepSchedule(schedule: SleepScheduleEntity)
}
