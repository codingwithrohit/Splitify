package com.example.splitify.domain.repository

import com.example.splitify.data.local.entity.relations.ExpenseWithSplits
import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.model.ExpenseSplit
import com.example.splitify.domain.model.TripMember
import com.example.splitify.util.Result
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {

    fun getExpensesByTrip(tripId: String): Flow<Result<List<Expense>>>

    suspend fun addExpenseWithSplits(
        expense: Expense,
        splits: List<ExpenseSplit>
    ): Result<Unit>

    fun getExpenseWithSplitsById(expenseId: String): Flow<Result<ExpenseWithSplits>>

    suspend fun addExpense(expense: Expense): Result<Expense>

    suspend fun updateExpenseWithSplits(expense: Expense, splits: List<ExpenseSplit>): Result<Expense>

    suspend fun deleteExpense(expenseId: String): Result<Unit>

    suspend fun getTotalExpenses(expenseId: String): Double

    fun getExpensesWithSplits(tripId: String): Flow<Result<List<ExpenseWithSplits>>>

    suspend fun createExpenseWithSplits(expense: Expense, participatingMembers: List<TripMember>): Result<Unit>


}