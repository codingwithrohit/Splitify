package com.example.splitify.presentation.expense

import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.model.TripMember

sealed interface ExpenseUiState {

    data object Loading: ExpenseUiState

    data class Success(
        val expenses: List<Expense>,
        val members: List<TripMember>
    ): ExpenseUiState

    data class Error(val message: String): ExpenseUiState
}