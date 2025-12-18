package com.example.splitify.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.splitify.data.local.converter.DateConverter
import com.example.splitify.data.local.dao.ExpenseDao
import com.example.splitify.data.local.dao.ExpenseSplitDao
import com.example.splitify.data.local.dao.TripDao
import com.example.splitify.data.local.dao.TripMemberDao
import com.example.splitify.data.local.entity.ExpenseEntity
import com.example.splitify.data.local.entity.ExpenseSplitEntity
import com.example.splitify.data.local.entity.TripEntity
import com.example.splitify.data.local.entity.TripMemberEntity
import dagger.Provides

@Database(
    entities = [
        TripEntity::class,
        ExpenseEntity::class,
        TripMemberEntity::class,
        ExpenseSplitEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase: RoomDatabase() {

    abstract fun tripDao(): TripDao

    abstract fun expenseDao(): ExpenseDao


    abstract fun tripMemberDao(): TripMemberDao

    abstract fun expenseSplitDao(): ExpenseSplitDao

    companion object{
        const val DATABASE_NAME = "splitify_database"
    }
}