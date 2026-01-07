package com.example.splitify.domain.model

import java.time.LocalDate

data class TripInsights(
    val tripId: String,
    val tripName: String,
    val startDate: LocalDate,
    val endDate: LocalDate?,

    // Overview Stats
    val totalSpending: Double,
    val totalExpenses: Int,
    val totalMembers: Int,
    val averagePerPerson: Double,
    val tripDurationDays: Int,

    // Category Breakdown
    val categorySpending: Map<Category, Double>,
    val categoryPercentages: Map<Category, Float>,

    // Member Breakdown
    val memberSpending: List<MemberSpending>,
    val topSpender: MemberSpending?,

    // Daily Trend
    val dailySpending: Map<LocalDate, Double>,
    val highestSpendingDay: Pair<LocalDate, Double>?,

    // Expense Type Split
    val personalExpensesTotal: Double,
    val groupExpensesTotal: Double,
    val personalExpenseCount: Int,
    val groupExpenseCount: Int
)

data class MemberSpending(
    val member: TripMember,
    val totalPaid: Double,
    val percentage: Float,
    val expenseCount: Int
)
