package com.example.splitify.domain.usecase.member


import com.example.splitify.domain.repository.ExpenseRepository
import com.example.splitify.domain.repository.SettlementRepository
import com.example.splitify.util.Result
import kotlinx.coroutines.flow.first
import javax.inject.Inject


class CanRemoveMemberUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val settlementRepository: SettlementRepository
) {
    suspend operator fun invoke(
        tripId: String,
        memberId: String,
        isAdmin: Boolean
    ): Result<MemberRemovalValidation> {
        return try {
            // 1. Check if admin
            if (isAdmin) {
                return Result.Success(
                    MemberRemovalValidation(
                        canRemove = false,
                        reason = "Cannot remove trip admin"
                    )
                )
            }

            // 2. Check if member has paid for any expense
            val expensesResult = expenseRepository.getExpensesByTrip(tripId).first()
            val hasPaidExpenses = when (expensesResult) {
                is Result.Success -> {
                    expensesResult.data.any { it.paidBy == memberId }
                }
                else -> false
            }

            if (hasPaidExpenses) {
                return Result.Success(
                    MemberRemovalValidation(
                        canRemove = false,
                        reason = "Cannot remove member who has paid for expenses"
                    )
                )
            }

            // 3. Check for pending settlements
            val settlementsResult = settlementRepository.getSettlementsForTrip(tripId).first()
            val hasPendingSettlements = when (settlementsResult) {
                is Result.Success -> {
                    settlementsResult.data.any { settlement ->
                        settlement.status == com.example.splitify.domain.model.SettlementStatus.PENDING &&
                                (settlement.fromMemberId == memberId || settlement.toMemberId == memberId)
                    }
                }
                else -> false
            }

            if (hasPendingSettlements) {
                return Result.Success(
                    MemberRemovalValidation(
                        canRemove = false,
                        reason = "Cannot remove member with pending settlements"
                    )
                )
            }

            // 4. All checks passed - can remove
            Result.Success(
                MemberRemovalValidation(
                    canRemove = true,
                    reason = null
                )
            )
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
