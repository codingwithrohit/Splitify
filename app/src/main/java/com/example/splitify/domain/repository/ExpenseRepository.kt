package com.example.splitify.domain.repository

import com.example.splitify.domain.model.Expense
import com.example.splitify.util.Result
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {

    fun getExpensesByTrip(tripId: String): Flow<List<Expense>>

    suspend fun getExpenseById(expenseId: String): Expense?

    suspend fun addExpense(expense: Expense): Result<Expense>

    suspend fun updateExpense(expense: Expense): Result<Unit>

    suspend fun deleteExpense(expenseId: String): Result<Unit>

    suspend fun getTotalExpenses(expenseId: String): Double
}