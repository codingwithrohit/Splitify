package com.example.splitify.presentation.addmembers

import com.example.splitify.domain.model.TripMember


sealed interface MembersUiState {
    data object Loading : MembersUiState
    data class Success(val members: List<TripMember>) : MembersUiState
    data class Error(val message: String) : MembersUiState
}