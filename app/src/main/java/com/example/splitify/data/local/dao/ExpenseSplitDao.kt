package com.example.splitify.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.splitify.data.local.entity.ExpenseEntity
import com.example.splitify.data.local.entity.ExpenseSplitEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface ExpenseSplitDao {

    @Query("SELECT * FROM expense_splits WHERE id = :splitId")
    suspend fun getSplitById(splitId: String): ExpenseSplitEntity?

    @Query("Select * from expense_splits where expense_id = :expenseId")
    fun getSplitsForExpenses(expenseId: String): Flow<List<ExpenseSplitEntity>>

    @Query("Select * from expense_splits where member_id = :memberId")
    fun getSplitsForMember(memberId: String): Flow<List<ExpenseSplitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSplit(split: ExpenseSplitEntity)

    @Upsert
    suspend fun upsertSplit(split: ExpenseSplitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSplits(splits: List<ExpenseSplitEntity>)

    @Query("Delete from expense_splits where expense_id = :expenseId")
    suspend fun deleteSplitsForExpense(expenseId: String): Int

    @Query("Delete from expense_splits where id = :splitId")
    suspend fun deleteSplit(splitId: String)

    // ADD THIS - Synchronous version for getExpensesWithSplits
    @Query("SELECT * FROM expense_splits WHERE expense_id = :expenseId")
    suspend fun getSplitsForExpenseSync(expenseId: String): List<ExpenseSplitEntity>

    @Transaction
    @Query("""
        SELECT e.*, es.*
        FROM expenses e
        LEFT JOIN expense_splits es ON e.id = es.expense_id
        WHERE e.trip_id = :tripId
        ORDER BY e.expense_date DESC
    """)
    fun getExpensesWithSplitsForTrip(tripId: String): Flow<Map<ExpenseEntity, List<ExpenseSplitEntity>>>

    @Query("""
    DELETE FROM expense_splits 
    WHERE expense_id IN (
        SELECT e.id FROM expenses e
        INNER JOIN trips t ON e.trip_id = t.id
        INNER JOIN trip_members tm ON t.id = tm.trip_id
        WHERE tm.user_id = :userId
    )
""")
    suspend fun deleteAllSplitsForUser(userId: String)


    @Query("DELETE FROM expense_splits WHERE expense_id IN (SELECT id FROM expenses WHERE trip_id = :tripId)")
    suspend fun deleteAllSplitsForTrip(tripId: String)

    @Query("DELETE FROM expense_splits WHERE member_id = :memberId")
    suspend fun deleteSplitsForMember(memberId: String)

    @Query("DELETE FROM expense_splits")
    fun deleteAllSplits()


}