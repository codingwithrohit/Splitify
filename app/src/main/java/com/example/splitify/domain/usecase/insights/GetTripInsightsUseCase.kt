package com.example.splitify.domain.usecase.insights

import android.util.Log
import com.example.splitify.domain.model.MemberSpending
import com.example.splitify.domain.model.Trip
import com.example.splitify.domain.model.TripInsights
import com.example.splitify.domain.repository.ExpenseRepository
import com.example.splitify.domain.repository.TripMemberRepository
import com.example.splitify.domain.repository.TripRepository
import com.example.splitify.util.Result
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.exp


class GetTripInsightsUseCase @Inject constructor(
    private val tripRepository: TripRepository,
    private val expenseRepository: ExpenseRepository,
    private val tripMemberRepository: TripMemberRepository
){
    suspend operator fun invoke(tripId: String): Result<TripInsights> {
        return try {
            Log.d("InsightsUseCase", "════════════════════════════════")
            Log.d("InsightsUseCase", "📊 Calculating insights for trip: $tripId")

            //1. Get trip details
            val tripResult = tripRepository.observeTripById(tripId).first()
            if(tripResult !is Result.Success){
                return Result.Error(Exception("Failed to load trip"))
            }
            val trip = tripResult.data

            //2. Get all expenses
            val expenseResult = expenseRepository.getExpensesByTrip(tripId).first()
            if(expenseResult !is Result.Success){
                return Result.Error(Exception("Failed to load expenses"))
            }
            val expenses = expenseResult.data


            //3. Get all members
            val memberResult = tripMemberRepository.getMembersForTrip(tripId).first()
            if(memberResult !is Result.Success){
                return Result.Error(Exception("Failed to load members"))
            }
            val members = memberResult.data

            Log.d("InsightsUseCase", "  Expenses: ${expenses.size}")
            Log.d("InsightsUseCase", "  Members: ${members.size}")

            if(expenses.isEmpty()) {
                Log.d("InsightsUseCase", "⚠️ No expenses - returning empty insights")
                return Result.Success(createEmptyInsights(trip, members.size))
            }

            val totalSpending = expenses.sumOf { it.amount }
            val totalExpenses = expenses.size
            val totalMembers = members.size
            val averagePerPerson = if (totalMembers > 0) totalSpending / totalMembers else 0.0

            val tripDurationDays = if (trip.endDate != null) {
                ChronoUnit.DAYS.between(trip.startDate, trip.endDate).toInt() + 1
            } else {
                ChronoUnit.DAYS.between(trip.startDate, LocalDate.now()).toInt() + 1
            }

            val categorySpending = expenses
                .groupBy { it.category }
                .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }

            val categoryPercentages = categorySpending
                .mapValues { (_, amount) ->
                    ((amount / totalSpending) * 100).toFloat()
                }

            val memberSpending = members.map { member ->
                val totalPaid = expenses
                    .filter { it.paidBy == member.id }
                    .sumOf { it.amount }

                val expenseCount = expenses.count { it.paidBy == member.id }

                val percentage = if (totalSpending > 0) {
                    ((totalPaid / totalSpending) * 100).toFloat()
                } else {
                    0f
                }

                MemberSpending(
                    member = member,
                    totalPaid = totalPaid,
                    percentage = percentage,
                    expenseCount = expenseCount
                )
            }.sortedByDescending { it.totalPaid }

            val topSpender = memberSpending.firstOrNull()

            val dailySpending = expenses
                .groupBy { it.expenseDate }
                .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
                .toSortedMap()

            val highestSpendingDay = dailySpending.maxByOrNull { it.value }
                ?.let { it.key to it.value }

            val personalExpenses = expenses.filter { !it.isGroupExpense }
            val groupExpenses = expenses.filter { it.isGroupExpense }

            val personalExpensesTotal = personalExpenses.sumOf { it.amount }
            val groupExpensesTotal = groupExpenses.sumOf { it.amount }
            val personalExpenseCount = personalExpenses.size
            val groupExpenseCount = groupExpenses.size
            Log.d("InsightsUseCase", "  Personal: $personalExpenseCount expenses (₹$personalExpensesTotal)")
            Log.d("InsightsUseCase", "  Group: $groupExpenseCount expenses (₹$groupExpensesTotal)")
            Log.d("InsightsUseCase", "✅ Insights calculated successfully")
            Log.d("InsightsUseCase", "════════════════════════════════")

            Result.Success(
                TripInsights(
                    tripId = tripId,
                    tripName = trip.name,
                    startDate = trip.startDate,
                    endDate = trip.endDate,
                    totalSpending = totalSpending,
                    totalExpenses = totalExpenses,
                    totalMembers = totalMembers,
                    averagePerPerson = averagePerPerson,
                    tripDurationDays = tripDurationDays,
                    categorySpending = categorySpending,
                    categoryPercentages = categoryPercentages,
                    memberSpending = memberSpending,
                    topSpender = topSpender,
                    dailySpending = dailySpending,
                    highestSpendingDay = highestSpendingDay,
                    personalExpensesTotal = personalExpensesTotal,
                    groupExpensesTotal = groupExpensesTotal,
                    personalExpenseCount = personalExpenseCount,
                    groupExpenseCount = groupExpenseCount
                )
            )

        }
        catch (e: Exception) {
            Log.e("InsightsUseCase", "❌ Error calculating insights: ${e.message}")
            Result.Error(e)
        }
    }

    private fun createEmptyInsights(
        trip: Trip,
        memberCount: Int
    ): TripInsights {
        return TripInsights(
            tripId = trip.id,
            tripName = trip.name,
            startDate = trip.startDate,
            endDate = trip.endDate,
            totalSpending = 0.0,
            totalExpenses = 0,
            totalMembers = memberCount,
            averagePerPerson = 0.0,
            tripDurationDays = 1,
            categorySpending = emptyMap(),
            categoryPercentages = emptyMap(),
            memberSpending = emptyList(),
            topSpender = null,
            dailySpending = emptyMap(),
            highestSpendingDay = null,
            personalExpensesTotal = 0.0,
            groupExpensesTotal = 0.0,
            personalExpenseCount = 0,
            groupExpenseCount = 0
        )
    }
}