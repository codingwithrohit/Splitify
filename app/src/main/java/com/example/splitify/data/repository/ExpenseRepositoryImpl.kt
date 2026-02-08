package com.example.splitify.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.example.splitify.data.local.AppDatabase
import com.example.splitify.data.local.SessionManager
import com.example.splitify.data.local.dao.ExpenseDao
import com.example.splitify.data.local.dao.ExpenseSplitDao
import com.example.splitify.data.local.entity.relations.ExpenseWithSplits
import com.example.splitify.data.local.toDomain
import com.example.splitify.data.local.toEntity
import com.example.splitify.data.local.toExpenseDomainModels
import com.example.splitify.data.remote.dto.ExpenseDto
import com.example.splitify.data.remote.dto.ExpenseSplitDto
import com.example.splitify.data.remote.toDto
import com.example.splitify.data.remote.toEntity
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
    private val sessionManager: SessionManager
): ExpenseRepository{

    override suspend fun addExpenseWithSplits(
        expense: Expense,
        splits: List<ExpenseSplit>
    ): Result<Unit> {
        return try {
            Log.d("ExpenseRepository", "════════════════════════════════")
            Log.d("ExpenseRepository", "💾 ADD EXPENSE WITH SPLITS")
            Log.d("ExpenseRepository", "  Expense ID: ${expense.id}")
            Log.d("ExpenseRepository", "  Description: ${expense.description}")
            Log.d("ExpenseRepository", "  Amount: ₹${expense.amount}")
            Log.d("ExpenseRepository", "  Paid by: ${expense.paidBy}")
            Log.d("ExpenseRepository", "  Trip ID: ${expense.tripId}")
            Log.d("ExpenseRepository", "  Is Group: ${expense.isGroupExpense}")
            Log.d("ExpenseRepository", "  Splits to save: ${splits.size}")

            // CRITICAL: Validate before saving
            if (splits.isEmpty()) {
                Log.e("ExpenseRepository", "❌ CRITICAL: Received 0 splits!")
                return Result.Error(Exception("Cannot create expense without splits"))
            }

            // Log all splits
            splits.forEachIndexed { index, split ->
                Log.d("ExpenseRepository", "  Split ${index + 1}:")
                Log.d("ExpenseRepository", "    ID: ${split.id}")
                Log.d("ExpenseRepository", "    Member: ${split.memberName} (${split.memberId})")
                Log.d("ExpenseRepository", "    Expense ID: ${split.expenseId}")
                Log.d("ExpenseRepository", "    Amount: ₹${split.amountOwed}")
            }

            // CRITICAL: Verify expense ID matches in all splits
            val mismatchedSplits = splits.filter { it.expenseId != expense.id }
            if (mismatchedSplits.isNotEmpty()) {
                Log.e("ExpenseRepository", "❌ CRITICAL: ${mismatchedSplits.size} splits have wrong expenseId!")
                mismatchedSplits.forEach {
                    Log.e("ExpenseRepository", "  Split ${it.id}: has expenseId=${it.expenseId}, expected=${expense.id}")
                }
                return Result.Error(Exception("Expense ID mismatch in splits"))
            }

            Log.d("ExpenseRepository", "🔄 Starting database transaction...")

            // 1. Insert expense and splits to Room (atomic transaction)
            database.withTransaction {
                Log.d("ExpenseRepository", "📥 Inserting expense entity...")

                val expenseEntity = expense.toEntity().copy(
                    isLocal = true,
                    isSynced = false,
                    lastModified = System.currentTimeMillis()
                )

                // Insert expense
                expenseDao.insertAnExpense(expenseEntity)
                Log.d("ExpenseRepository", "✅ Expense inserted to Room")

                // Insert splits
                Log.d("ExpenseRepository", "📥 Inserting ${splits.size} split entities...")
                val splitEntities = splits.map { split ->
                    val entity = split.toEntity()
                    Log.d("ExpenseRepository", "  Converting: ${split.memberName} → expenseId=${entity.expenseId}, memberId=${entity.memberId}")
                    entity
                }

                expenseSplitDao.insertSplits(splitEntities)
                Log.d("ExpenseRepository", "✅ ${splits.size} splits inserted to Room")

                // VERIFICATION
                val verifyCount = expenseSplitDao.getSplitsForExpenseSync(expense.id).size
                Log.d("ExpenseRepository", "🔍 Verification: Found ${verifyCount} splits in DB for expense ${expense.id}")

                if (verifyCount != splits.size) {
                    Log.e("ExpenseRepository", "❌ VERIFICATION FAILED!")
                    Log.e("ExpenseRepository", "  Expected: ${splits.size}")
                    Log.e("ExpenseRepository", "  Found: $verifyCount")
                    throw Exception("Split insertion failed - expected ${splits.size}, found $verifyCount")
                }

                Log.d("ExpenseRepository", "✅ Transaction completed successfully")
            }

            // 2. Try to sync to Supabase
            try {
                val session = supabase.auth.currentSessionOrNull()
                if (session == null) {
                    Log.w("ExpenseRepository", "⚠️ No session, skipping Supabase sync")
                    Log.d("ExpenseRepository", "════════════════════════════════")
                    return Result.Success(Unit)
                }

                Log.d("ExpenseRepository", "📤 Syncing to Supabase...")

                val expenseDto = expense.toEntity().toDto()
                val splitDtos = splits.map { it.toEntity().toDto() }

                // Upload expense
                supabase.from("expenses").insert(expenseDto)
                Log.d("ExpenseRepository", "✅ Expense synced to Supabase")

                // Upload splits
                supabase.from("expense_splits").insert(splitDtos)
                Log.d("ExpenseRepository", "✅ ${splitDtos.size} splits synced to Supabase")

                // Mark as synced
                database.withTransaction {
                    val syncedExpense = expense.toEntity().copy(
                        isSynced = true,
                        isLocal = false,
                        lastModified = System.currentTimeMillis()
                    )
                    expenseDao.updateExpense(syncedExpense)
                }
                Log.d("ExpenseRepository", "✅ Marked as synced in Room")

            } catch (e: Exception) {
                Log.e("ExpenseRepository", "⚠️ Supabase sync failed (expense saved locally): ${e.message}", e)
                // Don't return error - local save succeeded
            }

            Log.d("ExpenseRepository", "✅ ADD EXPENSE COMPLETE")
            Log.d("ExpenseRepository", "════════════════════════════════")
            Result.Success(Unit)

        } catch (e: Exception) {
            Log.e("ExpenseRepository", "❌ FAILED TO ADD EXPENSE", e)
            Log.e("ExpenseRepository", "  Error: ${e.message}")
            Log.e("ExpenseRepository", "  Stack trace: ${e.stackTraceToString()}")
            Log.d("ExpenseRepository", "════════════════════════════════")
            Result.Error(e, "Failed to save expense: ${e.message}")
        }
    }

//    override suspend fun addExpenseWithSplits(
//        expense: Expense,
//        splits: List<ExpenseSplit>
//    ): Result<Unit> {
//        return try {
//            Log.d("ExpenseRepository", "💾 Saving expense: ${expense.description}")
//            Log.d("ExpenseRepository", "💾 Saving expense with ${splits.size} splits")
//
//            //1. Insert expense to Room,
//            database.withTransaction {
//                val expenseEntity = expense.toEntity().copy(
//                    isLocal = true,
//                    isSynced = false,
//                    lastModified = System.currentTimeMillis()
//                )
//                //Save Expense
//                expenseDao.insertAnExpense(expenseEntity)
//                Log.d("ExpenseRepo", "✅ Expense saved to Room")
//
//                //Save Expense Splits
//                val splitEntities = splits.map { it.toEntity() }
//                expenseSplitDao.insertSplits(splitEntities)
//                Log.d("ExpenseRepo", "✅ ${splits.size} splits saved to Room")
//            }
//
//            //2. Try to sync to Supabase
//            try {
//                val session = supabase.auth.currentSessionOrNull()
//                if(session == null){
//                    Log.w("ExpenseRepository", "⚠️ No session, skipping sync")
//                    return Result.Success(Unit)
//                }
//                Log.d("ExpenseRepository", "📤 Syncing to Supabase...")
//
//                val expenseDto = expense.toEntity().toDto()
//                val splitDto = splits.map { it.toEntity().toDto() }
//
//                //Upload expense
//                supabase.from("expenses").insert(expenseDto)
//                Log.d("ExpenseRepository", "✅ Expense synced to Supabase")
//                //Upload splits
//                supabase.from("expense_splits").insert(splitDto)
//                Log.d("ExpenseRepository", "✅ Splits synced to Supabase")
//
//                //3. Mark as synced
//                database.withTransaction {
//                    val syncedExpense = expense.toEntity().copy(
//                        isSynced = true,
//                        lastModified = System.currentTimeMillis()
//                    )
//                    expenseDao.updateExpense(syncedExpense)
//                }
//                Log.d("ExpenseRepo", "✅ Marked as synced")
//            }catch (e: Exception){
//                Log.e("ExpenseRepository", "⚠️ Sync failed: ${e.message}")
//            }
//
//            Result.Success(Unit)
//        } catch (e: Exception) {
//            Log.e("ExpenseRepository", "❌ Error saving expense", e)
//            Result.Error(e, "Failed to save expense: ${e.message}")
//        }
//    }

    override suspend fun updateExpenseWithSplits(expense: Expense, splits: List<ExpenseSplit>): Result<Expense> {
        return try {
            Log.d("ExpenseRepository", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d("ExpenseRepository", "📝 UPDATING EXPENSE")
            Log.d("ExpenseRepository", "  ID: ${expense.id}")
            Log.d("ExpenseRepository", "  Description: ${expense.description}")
            Log.d("ExpenseRepository", "  Amount: ₹${expense.amount}")
            Log.d("ExpenseRepository", "  Paid by: ${expense.paidBy}")
            Log.d("ExpenseRepository", "  Is Group: ${expense.isGroupExpense}")
            Log.d("ExpenseRepository", "  Splits received: ${splits.size}")

            // CRITICAL: Validate input
            if (splits.isEmpty()) {
                Log.e("ExpenseRepository", "❌ CRITICAL ERROR: Received 0 splits!")
                return Result.Error(Exception("Cannot update expense without splits"))
            }

            // Log all splits BEFORE saving
            splits.forEachIndexed { index, split ->
                Log.d("ExpenseRepository", "  Split ${index + 1}:")
                Log.d("ExpenseRepository", "    ID: ${split.id}")
                Log.d("ExpenseRepository", "    Member: ${split.memberName} (${split.memberId})")
                Log.d("ExpenseRepository", "    Amount: ₹${split.amountOwed}")
                Log.d("ExpenseRepository", "    Expense ID: ${split.expenseId}")
            }

            // Atomic transaction
            database.withTransaction {
                Log.d("ExpenseRepository", "🔄 Starting transaction...")

                // Step 1: Update expense
                val expenseEntity = expense.toEntity().copy(
                    isSynced = false,
                    lastModified = System.currentTimeMillis()
                )
                val updateCount = expenseDao.updateExpense(expenseEntity)
                Log.d("ExpenseRepository", "✅ Expense updated (rows affected: $updateCount)")

                // Step 2: Delete old splits
                val deleteCount = expenseSplitDao.deleteSplitsForExpense(expense.id)
                Log.d("ExpenseRepository", "✅ Deleted $deleteCount old splits")

                // Step 3: Insert new splits
                val splitEntities = splits.map { split ->
                    split.toEntity().also {
                        Log.d("ExpenseRepository", "  Converting split: ${split.memberName} → Entity(expenseId=${it.expenseId}, memberId=${it.memberId})")
                    }
                }

                Log.d("ExpenseRepository", "📥 Inserting ${splitEntities.size} split entities...")
                expenseSplitDao.insertSplits(splitEntities)
                Log.d("ExpenseRepository", "✅ Splits inserted")

                // VERIFICATION
                val verifyCount = expenseSplitDao.getSplitsForExpenseSync(expense.id).size
                Log.d("ExpenseRepository", "🔍 Verification: ${verifyCount} splits in DB")

                if (verifyCount != splits.size) {
                    Log.e("ExpenseRepository", "❌ CRITICAL MISMATCH!")
                    Log.e("ExpenseRepository", "  Expected: ${splits.size}")
                    Log.e("ExpenseRepository", "  Found: $verifyCount")
                    throw Exception("Split insertion failed: expected ${splits.size}, found $verifyCount")
                }

                Log.d("ExpenseRepository", "✅ Transaction completed successfully")
            }

            // Supabase sync (optional)
            try {
                val session = supabase.auth.currentSessionOrNull()
                if (session != null) {
                    Log.d("ExpenseRepository", "📤 Syncing to Supabase...")

                    supabase.from("expenses").upsert(expense.toDto(), onConflict = "id")
                    supabase.from("expense_splits").delete { filter { eq("expense_id", expense.id) } }
                    supabase.from("expense_splits").insert(splits.map { it.toEntity().toDto() })

                    expenseDao.updateExpense(expense.toEntity().copy(isSynced = true, isLocal = false))
                    Log.d("ExpenseRepository", "✅ Synced to Supabase")
                }
            } catch (e: Exception) {
                Log.e("ExpenseRepository", "⚠️ Supabase sync failed: ${e.message}", e)
            }

            Log.d("ExpenseRepository", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Result.Success(expense)

        } catch (e: Exception) {
            Log.e("ExpenseRepository", "❌ Update failed", e)
            Result.Error(e, "Failed to update expense: ${e.message}")
        }
    }

//    override suspend fun updateExpenseWithSplits(expense: Expense, splits: List<ExpenseSplit>): Result<Expense> {
//        return try {
//            Log.d("ExpenseRepository", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
//            Log.d("ExpenseRepository", "📝 UPDATING EXPENSE")
//            Log.d("ExpenseRepository", "  Description: ${expense.description}")
//            Log.d("ExpenseRepository", "  Amount: ₹${expense.amount}")
//            Log.d("ExpenseRepository", "  Splits: ${splits.size}")
//
//            // Debug: Log all splits
//            splits.forEachIndexed { index, split ->
//                Log.d("ExpenseRepository", "    Split $index: ${split.memberId} → ₹${split.amountOwed}")
//            }
//
//            //1. Update in Room first (atomic transaction)
//            database.withTransaction {
//                // Step 1: Delete old splits
//                expenseSplitDao.deleteSplitsForExpense(expense.id)
//                Log.d("ExpenseRepository", "✅ Old splits deleted")
//
//                // Step 2: Insert new splits
//                if (splits.isNotEmpty()) {
//                    val splitEntities = splits.map { it.toEntity() }
//                    expenseSplitDao.insertSplits(splitEntities)
//                    Log.d("ExpenseRepository", "✅ ${splits.size} new splits saved")
//                } else {
//                    Log.w("ExpenseRepository", "⚠️ No splits to save!")
//                }
//
//                // Step 3: Update expense (marks transaction as complete)
//                val expenseEntity = expense.toEntity().copy(
//                    isSynced = false,
//                    lastModified = System.currentTimeMillis()
//                )
//                expenseDao.updateExpense(expenseEntity)
//                Log.d("ExpenseRepository", "✅ Expense updated in Room")
//            }
//
//            //2. Try to sync to Supabase
//            try {
//                val session = supabase.auth.currentSessionOrNull()
//                if (session == null) {
//                    Log.w("ExpenseRepository", "⚠️ No session, skipping sync")
//                    return Result.Success(expense)
//                }
//
//                Log.d("ExpenseRepository", "📤 Syncing to Supabase...")
//
//                // Update expense
//                supabase.from("expenses")
//                    .upsert(
//                        expense.toDto(),
//                        onConflict = "id"
//                    )
//                Log.d("ExpenseRepository", "✅ Expense synced to Supabase")
//
//                // Delete old splits
//                supabase.from("expense_splits").delete {
//                    filter { eq("expense_id", expense.id) }
//                }
//                Log.d("ExpenseRepository", "✅ Old splits deleted from Supabase")
//
//                // Insert new splits
//                if (splits.isNotEmpty()) {
//                    supabase.from("expense_splits")
//                        .insert(splits.map { it.toEntity().toDto() })
//                    Log.d("ExpenseRepository", "✅ ${splits.size} splits synced to Supabase")
//                }
//
//                // Mark as synced
//                database.withTransaction {
//                    expenseDao.updateExpense(
//                        expense.toEntity().copy(
//                            isSynced = true,
//                            isLocal = false
//                        )
//                    )
//                }
//                Log.d("ExpenseRepository", "✅ Marked as synced")
//                Log.d("ExpenseRepository", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
//
//            } catch (e: Exception) {
//                Log.e("ExpenseRepository", "⚠️ Sync failed: ${e.message}", e)
//            }
//
//            Result.Success(expense)
//
//        } catch (e: Exception) {
//            Log.e("ExpenseRepo", "❌ Failed to update expense", e)
//            e.asError()
//        }
//    }


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

            if (!sessionManager.hasValidSession()) {
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

    override suspend fun fetchAndSaveRemoteExpenses(tripId: String): Result<Unit> {
        return try {
            Log.d("ExpenseRepo", "📥 Pulling latest expenses from Supabase for $tripId")

            if (!sessionManager.hasValidSession()) {
                return Result.Success(Unit)
            }

            // 1. Fetch expenses
            val remoteExpenses = supabase.from("expenses")
                .select { filter { eq("trip_id", tripId) } }
                .decodeList<ExpenseDto>()

            Log.d("ExpenseRepo", "📊 Found ${remoteExpenses.size} remote expenses")

            // 2. Fetch all splits for these expenses
            val expenseIds = remoteExpenses.map { it.id }

            val remoteSplits = if (expenseIds.isNotEmpty()) {
                supabase.from("expense_splits")
                    .select {
                        filter {
                            // Use `in` filter for multiple IDs
                            isIn("expense_id", expenseIds)
                        }
                    }
                    .decodeList<ExpenseSplitDto>()
            } else {
                emptyList()
            }

            Log.d("ExpenseRepo", "📊 Found ${remoteSplits.size} remote splits")

            // 3. Save to Room (atomic transaction)
            database.withTransaction {
                remoteExpenses.forEach { dto ->
                    val entity = dto.toEntity().copy(
                        isSynced = true,
                        isLocal = false
                    )
                    expenseDao.upsertExpense(entity)  // Upsert to avoid duplicates
                }

                remoteSplits.forEach { dto ->
                    val entity = dto.toEntity()
                    expenseSplitDao.upsertSplit(entity)  // Upsert to avoid duplicates
                }
            }

            Log.d("ExpenseRepo", "✅ Successfully pulled ${remoteExpenses.size} expenses and ${remoteSplits.size} splits")
            Result.Success(Unit)

        } catch (e: Exception) {
            Log.e("ExpenseRepo", "❌ Failed to pull expenses", e)
            Result.Error(e)
        }
    }
}
