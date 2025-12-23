package com.example.splitify.presentation.expense



sealed class ExpenseFormMode {

    data object Add: ExpenseFormMode()

    data class Edit(val expenseId: String): ExpenseFormMode()
}