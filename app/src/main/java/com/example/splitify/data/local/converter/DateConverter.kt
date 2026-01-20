package com.example.splitify.data.local.converter

import androidx.room.TypeConverter
import java.time.LocalDate

class DateConverter {

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString() // yyyy-MM-dd
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

//    @TypeConverter
//    fun fromTimeStamp(value: Long?): LocalDate?{
//        return value?.let {
//            LocalDate.ofEpochDay(it)
//        }
//    }
//
//    @TypeConverter
//    fun dateToTimeStamp(date: LocalDate?): Long?{
//        return date?.toEpochDay()
//    }
}