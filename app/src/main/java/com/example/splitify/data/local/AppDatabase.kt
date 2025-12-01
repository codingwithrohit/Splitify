package com.example.splitify.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.splitify.data.local.converter.DateConverter
import com.example.splitify.data.local.dao.TripDao
import com.example.splitify.data.local.entity.TripEntity

@Database(
    entities = [TripEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase: RoomDatabase() {

    abstract fun tripDao(): TripDao

    companion object{
        const val DATABASE_NAME = "splitify_database"
    }
}