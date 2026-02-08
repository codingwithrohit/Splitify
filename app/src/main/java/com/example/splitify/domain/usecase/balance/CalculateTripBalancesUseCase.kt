package com.example.splitify.domain.usecase.balance

import android.os.Build
import android.util.Log
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import kotlin.math.exp
import kotlin.math.roundToInt

class CalculateTripBalancesUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val tripMemberRepository: TripMemberRepository,
    private val simplifyDebtsUseCase: SimplifyDebtsUseCase
) {
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    operator fun invoke(tripId: String): Flow<Result<TripBalance>> =
        combine(
            tripMemberRepository.getMembersForTrip(tripId),
            expenseRepository.getExpensesWithSplits(tripId)
        ) { membersResult, expensesResult ->

            if (membersResult is Error) {
                return@combine Result.Error(Exception(membersResult.message))
            }

            if (expensesResult is Error) {
                return@combine Result.Error(Exception(expensesResult.message))
            }

            if (membersResult !is Success || expensesResult !is Success) {
                return@combine Result.Loading
            }

            val members = membersResult.data
            val expensesWithSplits = expensesResult.data

            Log.d("CalculateBalances", "════════════════════════════════")
            Log.d("CalculateBalances", "📊 Calculating balances")
            Log.d("CalculateBalances", "  Trip ID: $tripId")
            Log.d("CalculateBalances", "  Members: ${members.size}")
            Log.d("CalculateBalances", "  Expenses: ${expensesWithSplits.size}")

            if (members.isEmpty()) {
                return@combine Result.Success(
                    TripBalance(
                        tripId = tripId,
                        memberBalances = emptyList(),
                        simplifiedDebts = emptyList(),
                        totalExpenses = 0.0
                    )
                )
            }

            val totalExpenses = expensesWithSplits.sumOf { it.expense.amount }

            val balances = members.map { member ->
                val totalPaid = expensesWithSplits
                    .filter { it.expense.paidBy == member.id }
                    .sumOf { it.expense.amount }

                val totalOwed = expensesWithSplits
                    .flatMap { it.splits }
                    .filter { it.memberId == member.id }
                    .sumOf { it.amountOwed }

                // ✅ Round values to avoid floating-point errors
                val roundedPaid = (totalPaid * 100).roundToInt() / 100.0
                val roundedOwed = (totalOwed * 100).roundToInt() / 100.0

                Log.d("CalculateBalances", "  ${member.displayName}:")
                Log.d("CalculateBalances", "    Paid: ₹$roundedPaid")
                Log.d("CalculateBalances", "    Owes: ₹$roundedOwed")
                Log.d("CalculateBalances", "    Balance: ₹${roundedPaid - roundedOwed}")

                Balance.create(
                    member = member,
                    totalPaid = roundedPaid,
                    totalOwed = roundedOwed
                )
            }

            expensesWithSplits.forEach { expenseWithSplits ->
                Log.d("CalculateBalances", "  📝 Expense: ${expenseWithSplits.expense.description}")
                Log.d("CalculateBalances", "     Amount: ₹${expenseWithSplits.expense.amount}")
                Log.d("CalculateBalances", "     Paid by: ${expenseWithSplits.expense.paidBy}")
                Log.d("CalculateBalances", "     Splits: ${expenseWithSplits.splits.size}") // ← Shows split count
                expenseWithSplits.splits.forEach { split ->
                    Log.d("CalculateBalances", "       → ${split.memberId}: ₹${split.amountOwed}")
                }
            }

            val simplifiedDebts = simplifyDebtsUseCase(balances)

            Success(
                TripBalance(
                    tripId = tripId,
                    memberBalances = balances,
                    simplifiedDebts = simplifiedDebts,
                    totalExpenses = totalExpenses
                )
            )
        }


}