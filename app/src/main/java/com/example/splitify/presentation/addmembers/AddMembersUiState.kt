package com.example.splitify.presentation.addmembers

import com.example.splitify.domain.model.TripMember

sealed interface AddMembersUiState {
    data object Loading: AddMembersUiState

    data class Success(
        val members: List<TripMember>,
        val searchResults: List<TripMember>,
        val isSearching: Boolean = false,
    ): AddMembersUiState

    data class Error(val message: String): AddMembersUiState
}