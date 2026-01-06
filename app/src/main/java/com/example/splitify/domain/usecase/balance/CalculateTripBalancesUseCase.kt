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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import kotlin.math.exp

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
                return@combine Error(Exception(membersResult.message))
            }

            if (expensesResult is Error) {
                return@combine Error(Exception(expensesResult.message))
            }

            if (membersResult !is Success || expensesResult !is Success) {
                return@combine Loading
            }

            val members = membersResult.data
            val expensesWithSplits = expensesResult.data

            val totalExpenses = expensesWithSplits.sumOf { it.expense.amount }

            val balances = members.map { member ->
                val totalPaid = expensesWithSplits
                    .filter { it.expense.paidBy == member.id }
                    .sumOf { it.expense.amount }

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