package com.simats.nutrisoul.data

import androidx.room.TypeConverter
import com.google.firebase.Timestamp
import java.util.Date

class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromTimestamp(value: Timestamp?): Long? = value?.seconds

    @TypeConverter
    fun toTimestamp(value: Long?): Timestamp? = value?.let { Timestamp(it, 0) }
}
