package com.example.splitify.presentation.trips

import com.example.splitify.domain.model.Trip

sealed interface TripsUiState {

    data object InitialLoading : TripsUiState

    data class Content(
        val trips: List<Trip>,
        val isSyncing: Boolean
    ) : TripsUiState

    data class Empty(
        val isSyncing: Boolean
    ) : TripsUiState

    data class Error(
        val message: String,
        val trips: List<Trip> = emptyList()
    ) : TripsUiState
}