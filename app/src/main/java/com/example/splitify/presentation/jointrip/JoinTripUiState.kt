package com.example.splitify.presentation.jointrip

import com.example.splitify.domain.model.Trip

sealed interface JoinTripUiState{

    data object Idle : JoinTripUiState
    data object Loading : JoinTripUiState
    data class TripFound(val trip: Trip) : JoinTripUiState
    data object Joining : JoinTripUiState
    data class Success(val tripId: String) : JoinTripUiState
    data class Error(val message: String) : JoinTripUiState
}