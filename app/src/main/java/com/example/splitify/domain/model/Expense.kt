package com.example.splitify.domain.model

import java.time.LocalDate

data class Expense(
    val id: String,
    val tripId: String,
    val description: String,
    val amount: Double,
    val category: Category,
    val expenseDate: LocalDate,
    val paidBy: String,
    val paidByName: String,
    val isGroupExpense: Boolean,
    val createdAt: Long = System.currentTimeMillis()
)
