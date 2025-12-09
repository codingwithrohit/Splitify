package com.example.splitify.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.splitify.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface ExpenseDao {

    @Query("Select * from expenses where tripId = :tripId")
    fun getExpensesByTripId(tripId: String): Flow<List<ExpenseEntity>>

    @Query("Select * from expenses where id = :expenseId")
    suspend fun getExpenseById(expenseId: String): ExpenseEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnExpense(expense: ExpenseEntity)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("Delete from expenses where id = :expenseId ")
    suspend fun deleteExpenseById(expenseId: String)

    @Query("Delete from expenses where tripId = :tripId ")
    suspend fun deleteAllExpensesByTrip(tripId: String)

    @Query("Select SUM(amount) from expenses where tripId = :tripId ")
    suspend fun getTotalExpenses(tripId: String): Double?
}