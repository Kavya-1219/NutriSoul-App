package com.simats.nutrisoul.data

import androidx.room.TypeConverter
import com.simats.nutrisoul.SleepQuality
import java.time.LocalDate
import java.time.LocalTime

class SleepConverters {

    // LocalDate <-> String (yyyy-MM-dd)
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? =
        value?.let { LocalDate.parse(it) }

    // LocalTime <-> String (HH:mm:ss)
    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? = value?.toString()

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? =
        value?.let { LocalTime.parse(it) }

    // SleepQuality enum <-> String
    @TypeConverter
    fun fromSleepQuality(value: SleepQuality?): String? = value?.name

    @TypeConverter
    fun toSleepQuality(value: String?): SleepQuality? =
        value?.let { SleepQuality.valueOf(it) }
}
