package com.example.splitify.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.splitify.data.local.entity.ExpenseEntity
import com.example.splitify.data.local.entity.ExpenseSplitEntity
import com.example.splitify.data.local.entity.relations.ExpenseWithSplits
import com.example.splitify.data.local.entity.relations.ExpenseWithSplitsRelation
import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.model.ExpenseSplit
import com.example.splitify.util.Result
import kotlinx.coroutines.flow.Flow
@Dao
interface ExpenseDao {

    @Query("Select * from expenses where trip_id = :tripId")
    fun getExpensesByTripId(tripId: String): Flow<List<ExpenseEntity>>

    @Query("Select * from expenses where id = :expenseId")
    suspend fun getExpenseById(expenseId: String): ExpenseEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnExpense(expense: ExpenseEntity)

    @Transaction
    @Query("Select * from expenses where id = :expenseId")
    fun getExpenseWithSplitsById(expenseId: String): Flow<ExpenseWithSplitsRelation>

    @Update
    suspend fun updateExpenseWithSplits(expense: ExpenseEntity, splits: List<ExpenseSplitEntity>)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("Delete from expenses where id = :expenseId ")
    suspend fun deleteExpenseById(expenseId: String)

    @Query("Delete from expenses where trip_id = :tripId ")
    suspend fun deleteAllExpensesByTrip(tripId: String)

    @Query("Select SUM(amount) from expenses where trip_id = :tripId ")
    suspend fun getTotalExpenses(tripId: String): Double?

    @Query("SELECT * FROM expenses WHERE trip_id = :tripId AND is_synced = 0")
    suspend fun getUnsyncedExpensesForTrip(tripId: String): List<ExpenseEntity>

    @Query("""
    DELETE FROM expenses 
    WHERE trip_id IN (
        SELECT trip_id FROM trip_members WHERE user_id = :userId
    )
""")
    suspend fun deleteAllExpensesForUser(userId: String)
}