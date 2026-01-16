package com.example.splitify.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.example.splitify.data.local.AppDatabase
import com.example.splitify.data.local.dao.ExpenseDao
import com.example.splitify.data.local.dao.ExpenseSplitDao
import com.example.splitify.data.local.entity.ExpenseEntity
import com.example.splitify.data.local.entity.relations.ExpenseWithSplits
import com.example.splitify.data.local.toDomain
import com.example.splitify.data.local.toEntity
import com.example.splitify.data.local.toExpenseDomainModels
import com.example.splitify.data.remote.toDto
import com.example.splitify.data.remote.toUpdateDto
import com.example.splitify.data.sync.SyncManager
import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.model.ExpenseSplit
import com.example.splitify.domain.model.TripMember
import com.example.splitify.domain.repository.ExpenseRepository
import com.example.splitify.util.Result
import com.example.splitify.util.asError
import com.example.splitify.util.asSuccess
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val expenseSplitDao: ExpenseSplitDao,
    private val supabase: SupabaseClient,
    private val database: AppDatabase,
    private val syncManager: SyncManager
): ExpenseRepository{

    override suspend fun addExpenseWithSplits(
        expense: Expense,
        splits: List<ExpenseSplit>
    ): Result<Unit> {
        return try {
            Log.d("ExpenseRepository", "💾 Saving expense: ${expense.description}")
            Log.d("ExpenseRepository", "💾 Saving expense with ${splits.size} splits")

            //1. Insert expense to Room,
            database.withTransaction {
                val expenseEntity = expense.toEntity().copy(
                    isLocal = true,
                    isSynced = false,
                    lastModified = System.currentTimeMillis()
                )
                //Save Expense
                expenseDao.insertAnExpense(expenseEntity)
                Log.d("ExpenseRepo", "✅ Expense saved to Room")

                //Save Expense Splits
                val splitEntities = splits.map { it.toEntity() }
                expenseSplitDao.insertSplits(splitEntities)
                Log.d("ExpenseRepo", "✅ ${splits.size} splits saved to Room")
            }

            //2. Try to sync to Supabase
            try {
                val session = supabase.auth.currentSessionOrNull()
                if(session == null){
                    Log.w("ExpenseRepository", "⚠️ No session, skipping sync")
                    return Result.Success(Unit)
                }
                Log.d("ExpenseRepository", "📤 Syncing to Supabase...")

                val expenseDto = expense.toEntity().toDto()
                val splitDto = splits.map { it.toEntity().toDto() }

                //Upload expense
                supabase.from("expenses").insert(expenseDto)
                Log.d("ExpenseRepository", "✅ Expense synced to Supabase")
                //Upload splits
                supabase.from("expense_splits").insert(splitDto)
                Log.d("ExpenseRepository", "✅ Splits synced to Supabase")

                //3. Mark as synced
                database.withTransaction {
                    val syncedExpense = expense.toEntity().copy(
                        isSynced = true,
                        lastModified = System.currentTimeMillis()
                    )
                    expenseDao.updateExpense(syncedExpense)
                }
                Log.d("ExpenseRepo", "✅ Marked as synced")
            }catch (e: Exception){
                Log.e("ExpenseRepository", "⚠️ Sync failed: ${e.message}")
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("ExpenseRepository", "❌ Error saving expense", e)
            Result.Error(e, "Failed to save expense: ${e.message}")
        }
    }

    override suspend fun updateExpenseWithSplits(expense: Expense, splits: List<ExpenseSplit>): Result<Expense> {
        return try {
            //1. Update expense to room
            val expenseEntity = expense.toEntity().copy(
                isSynced = false,
                lastModified = System.currentTimeMillis()
            )
            database.withTransaction {
                Log.d("ExpenseRepository", "💾 Updating expense: ${expense.description}")
                Log.d("ExpenseRepository", "💾 Updating expense with ${splits.size} splits")
                expenseDao.updateExpense(expenseEntity)
                Log.d("ExpenseRepository", "✅ Expense updated in Room")

                Log.d("ExpenseRepository", "💾 Deleting old splits for expense: ${expense.description}")
                expenseSplitDao.deleteSplitsForExpense(expense.id)
                Log.d("ExpenseRepository", "✅ Old splits deleted")

                Log.d("ExpenseRepository", "💾 Saving new splits for expense: ${expense.description}")
                splits.forEach { split ->
                    expenseSplitDao.insertSplit(split.toEntity())
                }
                Log.d("ExpenseRepository", "✅ New splits saved")
            }

            //2. Try to sync to Supabase
            try {
                val session = supabase.auth.currentSessionOrNull()
                if(session == null){
                    Log.w("ExpenseRepository", "⚠️ No session, skipping sync")
                    return Result.Success(expense)
                }

                Log.d("ExpenseRepository", "📤 Syncing to Supabase...")

                //Update expense
                supabase.from("expenses")
                    .upsert(
                        expense.toDto(),
                        onConflict = "id"
                    )

                //Delete old splits
                supabase.from("expense_splits").delete {
                    filter { eq("expense_id", expense.id) }
                }

                //Insert new splits
                supabase.from("expense_splits")
                    .insert(splits.map {
                        it.toEntity().toDto()
                    })

                //3. Mark as synced
                database.withTransaction {
                    expenseDao.updateExpense(expense.toEntity().copy(isSynced = true, isLocal = false))
                }
                Log.d("ExpenseRepository", "✅ Synced to Supabase")
            }catch (e: Exception){
                Log.e("ExpenseRepository", "⚠️ Sync failed: ${e.message}")
            }

            Result.Success(expense)
        }
        catch (e: Exception){
            Log.e("ExpenseRepo", "❌ Failed to update expense", e)
            e.asError()
        }
    }


    override suspend fun deleteExpense(expenseId: String): Result<Unit> {
        return try {
            //1. Delete from room
            Log.d("ExpenseRepository", "💾 Deleting expense: $expenseId")
            database.withTransaction {
                expenseSplitDao.deleteSplitsForExpense(expenseId)
                expenseDao.deleteExpenseById(expenseId)
            }
            Log.d("ExpenseRepository", "✅ Deleted from Room")

            //2. Delete from supabase
            try {
                val session = supabase.auth.currentSessionOrNull()
                if(session == null){
                    Log.w("ExpenseRepo", "⚠️ No session, skipping sync")
                    return Unit.asSuccess()
                }

                Log.d("ExpenseRepository", "📤 Syncing to Supabase...")
                supabase.from("expense_splits").delete {
                    filter { eq("expense_id", expenseId) }
                }
                supabase.from("expenses").delete {
                    filter { eq("id", expenseId) }
                }
                Log.d("ExpenseRepository", "✅ Deleted from Supabase")
            }catch (e: Exception){
                Log.e("ExpenseRepository", "⚠️ Sync failed: ${e.message}")
            }

            Unit.asSuccess()
        }
        catch (e: Exception){
            e.asError()
        }
    }

    override fun getExpenseWithSplitsById(expenseId: String): Flow<Result<ExpenseWithSplits>> = flow {
        try {
            emit(Result.Loading)
            expenseDao.getExpenseWithSplitsById(expenseId).collect { expenseWithSplits ->
                emit(Result.Success(expenseWithSplits.toDomain()))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            emit(Result.Error(e, "Failed to load expenses with Splits"))
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

    override suspend fun getTotalExpenses(expenseId: String): Double {
        return try {
            expenseDao.getTotalExpenses(expenseId) ?: 0.0
        }
        catch (e: Exception){
            0.0
        }
    }

    override fun getExpensesWithSplits(tripId: String): Flow<Result<List<ExpenseWithSplits>>> {
        return expenseSplitDao.getExpensesWithSplitsForTrip(tripId)
            .map { map ->
                try {
                    val domainList = map.map { (expenseEntity, splitEntities) ->
                        ExpenseWithSplits(
                            expense = expenseEntity.toDomain(),
                            splits = splitEntities.map { it.toDomain() }
                        )
                    }
                    // Wrap in Success
                    Result.Success(domainList)
                } catch (e: Exception) {
                    // Return Error - Both Success and Error are subtypes of Result
                    Result.Error(e, "Mapping failed")
                }
            }
            .catch { e ->
                // This handles DB errors (like connection or query issues)
                Log.e("ExpenseRepository", "❌ DB Error", e)
                emit(Result.Error(e, "Failed to load expenses"))
            }
    }

    suspend fun syncUnsyncedExpenses(tripId: String): Result<Unit> {
        return try {
            Log.d("ExpenseRepository", "📤 Syncing unsynced expenses")

            val session = supabase.auth.currentSessionOrNull()
            if (session == null) {
                Log.d("ExpenseRepository", "⚠️ No session, skipping sync")
                return Result.Success(Unit)
            }

            val unsyncedExpenses = expenseDao.getUnsyncedExpensesForTrip(tripId)

            unsyncedExpenses.forEach { expenseEntity ->
                try {
                    Log.d("ExpenseRepo", "🔄 Syncing expense ${expenseEntity.id}")

                    // 1️⃣ UPSERT expense (safe)
                    supabase
                        .from("expenses")
                        .upsert(
                            expenseEntity.toDto(),
                            onConflict = "id"
                        )

                    // 2️⃣ DELETE old splits (CRITICAL FIX)
                    supabase
                        .from("expense_splits")
                        .delete {
                            filter { eq("expense_id", expenseEntity.id) }
                        }

                    // 3️⃣ INSERT fresh splits
                    val splits = expenseSplitDao
                        .getSplitsForExpenseSync(expenseEntity.id)
                        .distinctBy { it.memberId }
                    Log.d("ExpenseRepo", "📊 Re-inserting ${splits.size} splits")

                    splits.forEach {
                        Log.d(
                            "ExpenseRepo",
                            "➡️ split member=${it.memberId} amount=${it.amountOwed}"
                        )
                    }


                    if (splits.isNotEmpty()) {
                        supabase
                            .from("expense_splits")
                            .upsert(
                                splits.map { it.toDto() },
                                onConflict = "expense_id, member_id")
                    }

                    // 4️⃣ Mark expense as synced ONLY after success
                    expenseDao.updateExpense(
                        expenseEntity.copy(isSynced = true)
                    )

                    Log.d("ExpenseRepo", "✅ Synced expense: ${expenseEntity.description}")

                } catch (e: Exception) {
                    Log.e(
                        "ExpenseRepo",
                        "❌ Failed to sync expense ${expenseEntity.id}",
                        e
                    )
                    // Do NOT mark as synced
                }
            }

            Result.Success(Unit)

        } catch (e: Exception) {
            Log.e("ExpenseRepo", "❌ Sync failed", e)
            Result.Error(e)
        }
    }


    override suspend fun createExpenseWithSplits(
        expense: Expense,
        participatingMembers: List<TripMember>
    ): Result<Unit> {
        TODO("Not yet implemented")
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
}
