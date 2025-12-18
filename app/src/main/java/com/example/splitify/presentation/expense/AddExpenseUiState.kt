package com.example.splitify.presentation.expense

import com.example.splitify.domain.model.Category
import com.example.splitify.domain.model.TripMember
import java.sql.Date
import java.time.LocalDate

data class AddExpenseUiState(
    val amount: String = "",
    val description: String = "",
    val category: Category = Category.FOOD,
    val expenseDate: LocalDate = LocalDate.now(),
    val isGroupExpense: Boolean = false,
    val members: List<TripMember> = emptyList(),
    val paidByMemberId: String? = null,
    val amountError: String? = null,
    val descriptionError: String? = null,

    val isLoading: Boolean = false,
    val isSaved: Boolean = false
)