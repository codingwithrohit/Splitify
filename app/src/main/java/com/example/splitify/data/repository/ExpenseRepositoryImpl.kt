package com.example.splitify.data.repository

import android.util.Log
import com.example.splitify.data.local.dao.ExpenseDao
import com.example.splitify.data.local.dao.ExpenseSplitDao
import com.example.splitify.data.local.entity.relations.ExpenseWithSplits
import com.example.splitify.data.local.toDomain
import com.example.splitify.data.local.toEntity
import com.example.splitify.data.local.toExpenseDomainModels
import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.model.ExpenseSplit
import com.example.splitify.domain.model.TripMember
import com.example.splitify.domain.repository.ExpenseRepository
import com.example.splitify.util.Result
import com.example.splitify.util.asError
import com.example.splitify.util.asSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val expenseSplitDao: ExpenseSplitDao
): ExpenseRepository{

    override suspend fun addExpenseWithSplits(
        expense: Expense,
        splits: List<ExpenseSplit>
    ): Result<Unit> {
        return try {
            Log.d("ExpenseRepository", "💾 Saving expense: ${expense.description}")

            // Insert expense
            expenseDao.insertAnExpense(expense.toEntity())
            Log.d("ExpenseRepository", "✅ Expense saved")

            // Insert splits
            expenseSplitDao.insertSplits(splits.map { it.toEntity() })
            Log.d("ExpenseRepository", "✅ ${splits.size} splits saved")

            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("ExpenseRepository", "❌ Error saving expense", e)
            Result.Error(e, "Failed to save expense: ${e.message}")
        }
    }

    override fun getExpenseWithSplitsById(expenseId: String): Flow<Result<ExpenseWithSplits>> {
        return expenseDao.getExpenseWithSplitsById(expenseId)
            .map { expenseWithSplits ->
                Result.Success(expenseWithSplits.toDomain())
            }
            .catch { e ->
                Result.Error(Exception("Failed to load expense with splits", e))
            }
    }


    override fun getExpensesByTrip(tripId: String): Flow<Result<List<Expense>>> {
        return expenseDao.getExpensesByTripId(tripId)
            .map { entities ->
                Result.Success(entities.toExpenseDomainModels())
            }
    }

    suspend fun getExpenseById(expenseId: String): Expense? {
        return try {
            expenseDao.getExpenseById(expenseId).toDomain()
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

    override suspend fun updateExpenseWithSplits(expense: Expense, splits: List<ExpenseSplit>): Result<Expense> {
        return try {
            expenseDao.updateExpense(expense.toEntity())
            expenseSplitDao.deleteSplitsForExpense(expense.id)

            splits.forEach { split ->
                expenseSplitDao.insertSplit(split.toEntity())
            }
            Result.Success(expense)
        }
        catch (e: Exception){
            e.asError()
        }
    }

    override suspend fun deleteExpense(expenseId: String): Result<Unit> {
        return try {
            expenseSplitDao.deleteSplitsForExpense(expenseId)
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

    override fun getExpensesWithSplits(tripId: String): Flow<Result<List<ExpenseWithSplits>>> {
        return expenseDao.getExpensesByTripId(tripId)
            .map { expenseEntities ->
                Log.d("ExpenseRepository", "📦 Got ${expenseEntities.size} expenses for trip $tripId")

                val expensesWithSplits = expenseEntities.map { expenseEntity ->
                    // Get splits for this expense
                    val splitsResult = expenseSplitDao.getSplitsForExpenseSync(expenseEntity.id)

                    Log.d("ExpenseRepository", "  - ${expenseEntity.description}: ${splitsResult.size} splits")

                    ExpenseWithSplits(
                        expense = expenseEntity.toDomain(),
                        splits = splitsResult.map { it.toDomain() }
                    )
                }

                Result.Success(expensesWithSplits) as Result<List<ExpenseWithSplits>>
            }
            .catch { e ->
                Log.e("ExpenseRepository", "❌ Error getting expenses with splits", e)
                emit(Result.Error(e, "Failed to load expenses: ${e.message}"))
            }
    }

    override suspend fun createExpenseWithSplits(
        expense: Expense,
        participatingMembers: List<TripMember>
    ): Result<Unit> {
        TODO("Not yet implemented")
    }


}