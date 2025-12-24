package com.example.splitify.domain.usecase.expense
import android.util.Log
import com.example.splitify.domain.model.Category
import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.model.ExpenseSplit
import com.example.splitify.domain.repository.ExpenseRepository
import com.example.splitify.domain.repository.TripMemberRepository
import com.example.splitify.util.Result
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class AddExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val memberRepository: TripMemberRepository
) {
    suspend operator fun invoke(
        tripId: String,
        amount: Double,
        description: String,
        category: Category,
        date: LocalDate,
        paidBy: String,
        createdBy: String,
        isGroupExpense: Boolean,
        participatingMemberIds: List<String>
    ): Result<Unit> {
        // Validation
        if (amount <= 0) {
            return Result.Error(Exception("Invalid Amount"), "Amount must be greater than 0")
        }

        if (description.isBlank()) {
            return Result.Error(Exception("Description cannot be empty"))
        }

        if (participatingMemberIds.isEmpty()) {
            return Result.Error(Exception("At least one member must participate"))
        }

        return try {
            Log.d("AddExpenseUseCase", "🔍 Getting members for trip: $tripId")
            // Get participating members
            val allMembersResult = memberRepository.getMembersForTrip(tripId).first()
            Log.d("AddExpenseUseCase", "📦 Members result type: ${allMembersResult::class.simpleName}")
            val allMembers = when (allMembersResult) {
                is Result.Success -> {
                    Log.d("AddExpenseUseCase", "✅ Got ${allMembersResult.data.size} members")
                    allMembersResult.data.forEach { member ->
                        Log.d("AddExpenseUseCase", "  - Member: ${member.displayName} (${member.id})")
                    }
                    allMembersResult.data
                }
                is Result.Error ->{
                    Log.e("AddExpenseUseCase", "❌ Error loading members: ${allMembersResult.message}")
                    return Result.Error(
                        allMembersResult.exception,
                        "Failed to load members: ${allMembersResult.message}"
                    )
                }

                Result.Loading -> {
                    Log.e("AddExpenseUseCase", "⏳ Still loading members")
                    return Result.Error(
                        Exception("Still loading"),
                        "Members are still loading"
                    )
                }
            }
            Log.d("AddExpenseUseCase", "🔍 Filtering participating members from IDs: $participatingMemberIds")
            val participatingMembers = allMembers.filter { it.id in participatingMemberIds }
            Log.d("AddExpenseUseCase", "👥 Participating members: ${participatingMembers.size}")
            participatingMembers.forEach { member ->
                Log.d("AddExpenseUseCase", "  - ${member.displayName} (${member.id})")
            }

            if (participatingMembers.isEmpty()) {
                Log.e("AddExpenseUseCase", "❌ No valid members found after filtering")
                Log.e("AddExpenseUseCase", "All member IDs in trip: ${allMembers.map { it.id }}")
                Log.e("AddExpenseUseCase", "Requested IDs: $participatingMemberIds")

                return Result.Error(
                    Exception("Member ID mismatch"),
                    "Selected members not found in trip. This is likely a bug - member IDs don't match."
                )
            }

            // Get payer info
            val payer = allMembers.find { it.userId == paidBy || it.id == paidBy }
                ?: return Result.Error(Exception("Payer not found in trip members"))

            // Create expense
            val expense = Expense(
                id = UUID.randomUUID().toString(),
                tripId = tripId,
                amount = amount,
                description = description.trim(),
                category = category,
                expenseDate = date,
                paidBy = paidBy,
                createdBy = createdBy,
                paidByName = payer.displayName,
                isGroupExpense = isGroupExpense,
                createdAt = System.currentTimeMillis()
            )

            // Create splits
            val splits = if (isGroupExpense) {
                // Equal split among all participating members
                val splitAmount = amount / participatingMembers.size
                participatingMembers.map { member ->
                    ExpenseSplit(
                        id = UUID.randomUUID().toString(),
                        expenseId = expense.id,
                        memberId = member.id,
                        memberName = member.displayName,
                        amountOwed = splitAmount,
                        createdAt = System.currentTimeMillis()
                    )
                }
            } else {
                // Personal expense - only payer owes
                listOf(
                    ExpenseSplit(
                        id = UUID.randomUUID().toString(),
                        expenseId = expense.id,
                        memberId = payer.id,
                        memberName = payer.displayName,
                        amountOwed = amount,
                        createdAt = System.currentTimeMillis()
                    )
                )
            }

            // Save to repository
            val result = expenseRepository.addExpenseWithSplits(expense, splits)
            when (result) {
                is Result.Success -> {
                    Log.d("AddExpenseUseCase", "✅ Expense saved successfully!")
                    Result.Success(Unit)
                }
                is Result.Error -> {
                    Log.e("AddExpenseUseCase", "❌ Failed to save: ${result.message}")
                    result
                }
                Result.Loading -> {
                    Log.e("AddExpenseUseCase", "⏳ Still loading after save?")
                    Result.Error(Exception("Unexpected loading state"), "Unexpected state")
                }
            }

        } catch (e: Exception) {
            Log.e("AddExpenseUseCase", "💥 Exception in AddExpenseUseCase", e)
            Result.Error(e, "Failed to add expense: ${e.message}")
        }
    }
}