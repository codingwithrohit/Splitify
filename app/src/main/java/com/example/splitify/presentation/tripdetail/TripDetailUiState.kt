package com.example.splitify.presentation.tripdetail

import co.touchlab.kermit.Message
import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.model.Trip

sealed interface TripDetailUiState {

    data object Loading: TripDetailUiState

    data class Success(
        val trip: Trip,
        val expenses: List<Expense> = emptyList(),
        val totalExpenses: Double = 0.0,
        val memberCount: Int = 1,
        val currentTab: TripDetailTab = TripDetailTab.OVERVIEW
    ): TripDetailUiState

    data class Error(val message: String): TripDetailUiState
}

enum class TripDetailTab{
    OVERVIEW, EXPENSES, MEMBERS, BALANCES
}