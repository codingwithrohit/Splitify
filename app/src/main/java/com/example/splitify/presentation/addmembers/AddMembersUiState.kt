package com.example.splitify.presentation.addmembers

import com.example.splitify.domain.model.TripMember
import com.example.splitify.domain.model.User

sealed interface AddMembersUiState {
    data object Loading: AddMembersUiState

    data class Success(
        val members: List<TripMember>,
        val searchResults: List<User> = emptyList(),
        val isSearching: Boolean = false,
        val searchQuery: String = "",  //  Track query
        val hasSearched: Boolean = false  //  Track if search was performed
    ): AddMembersUiState

    data class Error(val message: String): AddMembersUiState
}