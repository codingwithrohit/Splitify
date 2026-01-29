package com.example.splitify.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.splitify.data.local.converter.DateConverter
import com.example.splitify.data.local.dao.ExpenseDao
import com.example.splitify.data.local.dao.ExpenseSplitDao
import com.example.splitify.data.local.dao.SettlementDao
import com.example.splitify.data.local.dao.TripDao
import com.example.splitify.data.local.dao.TripMemberDao
import com.example.splitify.data.local.entity.ExpenseEntity
import com.example.splitify.data.local.entity.ExpenseSplitEntity
import com.example.splitify.data.local.entity.SettlementEntity
import com.example.splitify.data.local.entity.TripEntity
import com.example.splitify.data.local.entity.TripMemberEntity
import dagger.Provides

@Database(
    entities = [
        TripEntity::class,
        ExpenseEntity::class,
        TripMemberEntity::class,
        ExpenseSplitEntity::class,
        SettlementEntity::class
    ],
    version = 9,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase: RoomDatabase() {

    abstract fun tripDao(): TripDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun tripMemberDao(): TripMemberDao
    abstract fun expenseSplitDao(): ExpenseSplitDao
    abstract fun settlementDao(): SettlementDao
    companion object{
        const val DATABASE_NAME = "splitify_database"
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add created_by column (default to empty string for existing data)
                database.execSQL(
                    "ALTER TABLE trip_members ADD COLUMN invitation_status TEXT NOT NULL DEFAULT 'accepted'"
                )

            }
        }
    }
}