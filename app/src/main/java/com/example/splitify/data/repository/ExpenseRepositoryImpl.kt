package com.example.splitify.data.repository

import com.example.splitify.data.local.dao.ExpenseDao
import com.example.splitify.data.local.toDomainModel
import com.example.splitify.data.local.toEntity
import com.example.splitify.data.local.toExpenseDomainModels
import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.repository.ExpenseRepository
import com.example.splitify.util.Result
import com.example.splitify.util.asError
import com.example.splitify.util.asSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.math.exp

class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao
): ExpenseRepository{

    override fun getExpensesByTrip(tripId: String): Flow<List<Expense>> {
        return expenseDao.getExpensesByTripId(tripId)
            .map { entities ->
                entities.toExpenseDomainModels()
            }
    }

    override suspend fun getExpenseById(expenseId: String): Expense? {
        return try {
            expenseDao.getExpenseById(expenseId).toDomainModel()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun addExpense(expense: Expense): Result<Expense> {
        return try {
            println("Creating expense in Room: ${expense.description}")
            val entity = expense.toEntity()
            expenseDao.insertAnExpense(entity)

            //TODO: Sync to supabase
            expense.asSuccess()
        }
        catch (e: Exception){
            println("Failed to add expense to room: ${e.message}")
            e.printStackTrace()
            e.asError()
        }
    }

    override suspend fun updateExpense(expense: Expense): Result<Unit> {
        return try {
            val entity = expense.toEntity()
            expenseDao.updateExpense(entity)
            Unit.asSuccess()
        }
        catch (e: Exception){
            e.asError()
        }
    }

    override suspend fun deleteExpense(expenseId: String): Result<Unit> {
        return try {
            expenseDao.deleteExpenseById(expenseId)
            Unit.asSuccess()
        }
        catch (e: Exception){
            e.asError()
        }
    }

    override suspend fun getTotalExpenses(expenseId: String): Double {
        return try {
            expenseDao.getTotalExpenses(expenseId) ?: 0.0
        }
        catch (e: Exception){
            0.0
        }
    }

}