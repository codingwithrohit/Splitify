package com.example.splitify.presentation.trips

import com.example.splitify.domain.model.Trip

sealed interface TripsUiState {

    data object Loading: TripsUiState

    data class Success(
        val trips: List<Trip>,
        val isRefreshing: Boolean = false  // for pull-to-refresh later
    ): TripsUiState

    data class Error(
        val message: String
    ): TripsUiState
}