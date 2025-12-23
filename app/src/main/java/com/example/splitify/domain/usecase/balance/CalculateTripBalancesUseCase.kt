package com.example.splitify.domain.usecase.balance

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.splitify.data.local.entity.relations.ExpenseWithSplits
import com.example.splitify.domain.model.Balance
import com.example.splitify.domain.model.TripBalance
import com.example.splitify.domain.model.TripMember
import com.example.splitify.domain.repository.ExpenseRepository
import com.example.splitify.domain.repository.TripMemberRepository
import kotlinx.coroutines.flow.first
import com.example.splitify.util.Result
import com.example.splitify.util.Result.*
import javax.inject.Inject
import kotlin.math.exp

class CalculateTripBalancesUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val tripMemberRepository: TripMemberRepository,
    private val simplifyDebtsUseCase: SimplifyDebtsUseCase
) {
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    suspend operator fun invoke(tripId: String): Result<TripBalance> {
        return try {
            // Get all members
            val membersResult = tripMemberRepository.getMembersForTrip(tripId).first()
            val members = when (membersResult) {
                is Success -> membersResult.data
                is Error -> return Error(Exception(membersResult.message))
                Loading -> TODO()
            }

            // Get all expenses with splits
            val expensesResult = expenseRepository.getExpensesWithSplits(tripId).first()
            val expensesWithSplits = when (expensesResult) {
                is Success -> expensesResult.data
                is Error -> return Error(Exception(expensesResult.message))
                Loading -> TODO()
            }

            // Calculate total expenses
            val totalExpenses = expensesWithSplits.sumOf { it.expense.amount }

            // Calculate balances for each member
            val balances = members.map { member ->
                // Total paid: Sum of all expenses where this member is the payer
                val totalPaid = expensesWithSplits
                    .filter { it.expense.paidBy == member.id }
                    .sumOf { it.expense.amount }

                // Total owed: Sum of all splits where this member owes
                val totalOwed = expensesWithSplits
                    .flatMap { it.splits }
                    .filter { it.memberId == member.id }
                    .sumOf { it.amountOwed }

                Balance.create(
                    member = member,
                    totalPaid = totalPaid,
                    totalOwed = totalOwed
                )
            }

            // Simplify debts
            val simplifiedDebts = simplifyDebtsUseCase(balances)

            Success(
                TripBalance(
                    tripId = tripId,
                    memberBalances = balances,
                    simplifiedDebts = simplifiedDebts,
                    totalExpenses = totalExpenses
                )
            )
        } catch (e: Exception) {
            Result.Error(Exception("Failed to calculate balances: ${e.message}"))
        }
    }

}