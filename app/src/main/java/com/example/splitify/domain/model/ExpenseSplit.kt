package com.example.splitify.domain.model

data class ExpenseSplit(
    val id: String,
    val expenseId: String,
    val memberId: String,
    val memberName: String,
    val amountOwed: Double,
    val createdAt: Long = System.currentTimeMillis()
)
