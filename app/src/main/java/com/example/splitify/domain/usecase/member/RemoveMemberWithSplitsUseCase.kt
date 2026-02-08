package com.example.splitify.domain.usecase.member

import android.util.Log
import com.example.splitify.domain.repository.ExpenseRepository
import com.example.splitify.domain.repository.TripMemberRepository
import javax.inject.Inject
import com.example.splitify.util.Result
import kotlinx.coroutines.flow.first

class RemoveMemberWithSplitsUpdateUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val tripMemberRepository: TripMemberRepository
) {
    suspend operator fun invoke(
        tripId: String,
        memberId: String
    ): Result<Unit> {
        return try {
            Log.d("RemoveMember", "════════════════════════════════")
            Log.d("RemoveMember", "🗑️ Starting member removal")
            Log.d("RemoveMember", "  Trip ID: $tripId")
            Log.d("RemoveMember", "  Member ID: $memberId")

            // 1. Get all expenses with splits
            val expensesResult = expenseRepository.getExpensesWithSplits(tripId).first()
            if (expensesResult !is Result.Success) {
                Log.e("RemoveMember", "❌ Failed to get expenses")
                return Result.Error(Exception("Failed to get expenses"))
            }

            // STEP 2: Find and update affected expenses
            val expensesWithSplits = expensesResult.data

            val affectedExpenses = expensesWithSplits.filter { expenseWithSplits ->
                expenseWithSplits.splits.any { it.memberId == memberId }
            }

            Log.d("RemoveMember", "  Affected expenses: ${affectedExpenses.size}")

            // Update each affected expense SEQUENTIALLY and AWAIT each one
            for (expenseWithSplits in affectedExpenses) {
                val expense = expenseWithSplits.expense
                val currentSplits = expenseWithSplits.splits

                Log.d("RemoveMember", "  Processing expense: ${expense.id}")
                Log.d("RemoveMember", "    Description: ${expense.description}")
                Log.d("RemoveMember", "    Amount: ₹${expense.amount}")
                Log.d("RemoveMember", "    Current participants: ${currentSplits.size}")

                // Remove the member's split
                val updatedSplits = currentSplits.filter { it.memberId != memberId }

                if (updatedSplits.isEmpty()) {
                    Log.w("RemoveMember", "    ⚠️ No participants left - deleting expense")
                    // Delete expense if no participants remain
                    expenseRepository.deleteExpense(expense.id)
                    continue
                }

                // Recalculate split amounts
                val newSplitAmount = expense.amount / updatedSplits.size
                val recalculatedSplits = updatedSplits.map { split ->
                    split.copy(amountOwed = newSplitAmount)
                }

                Log.d("RemoveMember", "    New participants: ${recalculatedSplits.size}")
                Log.d("RemoveMember", "    New split amount: ₹$newSplitAmount each")

                // ⚠️ CRITICAL FIX: AWAIT the update result
                val updateResult = expenseRepository.updateExpenseWithSplits(
                    expense,
                    recalculatedSplits
                )

                when (updateResult) {
                    is Result.Success -> {
                        Log.d("RemoveMember", "    ✅ Splits updated successfully")
                    }
                    is Result.Error -> {
                        Log.e("RemoveMember", "    ❌ Failed to update splits: ${updateResult.message}")
                        return Result.Error(Exception("Failed to update expense splits: ${updateResult.message}"))
                    }
                    else -> {}
                }
            }

            Log.d("RemoveMember", "✅ All expense splits updated")

            // 3. Finally, remove the member
            val removeResult = tripMemberRepository.removeMember(tripId, memberId)

            when (removeResult) {
                is Result.Success -> {
                    Log.d("RemoveMember", "✅ Member removed successfully")
                    Log.d("RemoveMember", "════════════════════════════════")
                }
                is Result.Error -> {
                    Log.e("RemoveMember", "❌ Failed to remove member: ${removeResult.message}")
                    Log.e("RemoveMember", "════════════════════════════════")
                }
                else -> {}
            }

            removeResult
        } catch (e: Exception) {
            Log.e("RemoveMember", "❌ Error: ${e.message}")
            Log.e("RemoveMember", "════════════════════════════════")
            Result.Error(e)
        }
    }
}

data class MemberRemovalValidation(
    val canRemove: Boolean,
    val reason: String?
)