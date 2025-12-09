package com.example.splitify.presentation.expense

import com.example.splitify.domain.model.Category
import java.sql.Date
import java.time.LocalDate

data class AddExpenseUiState(
    val amount: String = "",
    val description: String = "",
    val category: Category = Category.OTHER,
    val expenseDate: LocalDate = LocalDate.now(),
    val isGroupExpense: Boolean = true,

    //Validation errors
    val amountError: String? = null,
    val descriptionError: String? = null,

    //Loading State
    val isLoading: Boolean = false,
    val isSaved: Boolean = false
)