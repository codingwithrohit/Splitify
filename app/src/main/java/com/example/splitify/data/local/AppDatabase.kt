package com.example.splitify.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.splitify.data.local.converter.DateConverter
import com.example.splitify.data.local.dao.ExpenseDao
import com.example.splitify.data.local.dao.TripDao
import com.example.splitify.data.local.entity.ExpenseEntity
import com.example.splitify.data.local.entity.TripEntity

@Database(
    entities = [
        TripEntity::class,
        ExpenseEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase: RoomDatabase() {

    abstract fun tripDao(): TripDao

    abstract fun expenseDao(): ExpenseDao

    companion object{
        const val DATABASE_NAME = "splitify_database"
    }
}