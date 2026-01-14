package com.example.splitify.presentation.addmembers

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitify.domain.usecase.member.AddTripMemberUseCase
import com.example.splitify.domain.usecase.member.GetTripMemberUseCase
import com.example.splitify.domain.usecase.member.RemoveTripMemberUseCase
import com.example.splitify.domain.usecase.member.SearchUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.splitify.util.Result
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.debounce
import javax.inject.Inject
@HiltViewModel
class AddMembersViewModel @Inject constructor(
    private val getTripMembersUseCase: GetTripMemberUseCase,
    private val addTripMemberUseCase: AddTripMemberUseCase,
    private val removeTripMemberUseCase: RemoveTripMemberUseCase,
    private val searchUsersUseCase: SearchUsersUseCase,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    private val tripId: String = checkNotNull(savedStateHandle["tripId"])
    private val _uiState = MutableStateFlow<AddMembersUiState>(AddMembersUiState.Loading)
    val uiState: StateFlow<AddMembersUiState> = _uiState.asStateFlow()

    private val _toastMessage = MutableStateFlow("")
    val toastMessage: StateFlow<String> = _toastMessage.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")

    init {
        loadMembers()
        setUpSearchDebouncing()
    }

    private fun loadMembers() {
        viewModelScope.launch {
            getTripMembersUseCase(tripId).collect { result ->
                _uiState.value = when (result) {
                    is Result.Loading -> AddMembersUiState.Loading
                    is Result.Success -> AddMembersUiState.Success(
                        members = result.data,
                        searchResults = emptyList(),
                    )
                    is Result.Error -> AddMembersUiState.Error(result.message)
                }
            }
        }
    }

    fun addMemberByName(name: String){
        val trimmedName = name.trim()

        if (trimmedName.isBlank()) {
            viewModelScope.launch {
                _toastMessage.value = "Name cannot be blank"
            }
            return
        }

        if (trimmedName.length < 2) {
            viewModelScope.launch {
                _toastMessage.value = "Name must be at least 2 characters"
            }
            return
        }

        Log.d("AddMembersVM", "🔄 Adding member: '$trimmedName' to trip: $tripId")

        viewModelScope.launch {

            val currentResult = getTripMembersUseCase(tripId).first()

            if (currentResult is Result.Success) {
                val alreadyExists = currentResult.data.any {
                    it.displayName.equals(trimmedName, ignoreCase = true)
                }

                if (alreadyExists) {
                    _toastMessage.value = "$trimmedName is already in this trip"
                    return@launch
                }
            }

            when(val result = addTripMemberUseCase(
                tripId = tripId,
                displayName = trimmedName,
                userId = null
            )){
                is Result.Success -> {
                    Log.d("AddMembersVM", "✅ SUCCESS: Member added")
                    _toastMessage.value = "$trimmedName added to trip"
                }
                is Result.Error -> {
                    Log.e("AddMembersVM", "❌ ERROR: ${result.message}")
                    Log.e("AddMembersVM", "════════════════════════════════")
                    val userFriendlyMessage = when {
                        result.message.contains("FOREIGN KEY", ignoreCase = true) ->
                            "Trip not found. Please try again."
                        result.message.contains("UNIQUE", ignoreCase = true) ->
                            "$trimmedName is already in this trip"
                        else -> result.message
                    }
                    _toastMessage.value = userFriendlyMessage
                }
                else -> {
                    Log.w("AddMembersVM", "⚠️ Unexpected result: Loading state")
                }
            }
        }
    }

    fun removeMember(memberId: String, memberName: String) {
        Log.d("AddMembersVM", "🗑️ Attempting to remove member: $memberName (ID: $memberId)")

        viewModelScope.launch {
            when (val result = removeTripMemberUseCase(tripId, memberId)) {
                is Result.Success -> {
                    Log.d("AddMembersVM", "✅ Member removed successfully")
                    _toastMessage.value = "$memberName removed from trip"
                }
                is Result.Error -> {
                    Log.e("AddMembersVM", "❌ Failed to remove member: ${result.message}")

                    // User-friendly error messages
                    val message = when {
                        result.message.contains("admin", ignoreCase = true) ->
                            "Cannot remove trip admin"
                        result.message.contains("paid for expenses", ignoreCase = true) ->
                            "$memberName has paid for expenses and cannot be removed"
                        result.message.contains("pending settlements", ignoreCase = true) ->
                            "$memberName has pending settlements and cannot be removed"
                        else -> "Failed to remove $memberName: ${result.message}"
                    }

                    _toastMessage.value = message
                }
                else -> {}
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun setUpSearchDebouncing(){
        viewModelScope.launch {
            searchQueryFlow
                .debounce(500) // Wait 500ms after last keystroke
                .distinctUntilChanged()
                .collect { query ->
                    performSearch(query)
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQueryFlow.value = query

        // Update UI state immediately to show query
        val currentState = _uiState.value
        if (currentState is AddMembersUiState.Success) {
            _uiState.value = currentState.copy(
                searchQuery = query,
                isSearching = query.isNotBlank(), // Show loading if typing
                hasSearched = false
            )
        }
    }

    fun clearSearch() {
        searchQueryFlow.value = ""
        val currentState = _uiState.value
        if (currentState is AddMembersUiState.Success) {
            _uiState.value = currentState.copy(
                searchQuery = "",
                searchResults = emptyList(),
                isSearching = false,
                hasSearched = false
            )
        }
    }

    private suspend fun performSearch(query: String) {
        if (query.isBlank()) {
            // Clear results
            val currentState = _uiState.value
            if (currentState is AddMembersUiState.Success) {
                _uiState.value = currentState.copy(
                    searchQuery = "",
                    searchResults = emptyList(),
                    isSearching = false,
                    hasSearched = false
                )
            }
            return
        }

        Log.d("AddMembersVM", "🔍 Searching for: $query")

        val currentState = _uiState.value
        if (currentState !is AddMembersUiState.Success) return

        // Set searching state
        _uiState.value = currentState.copy(
            isSearching = true,
            hasSearched = false
        )

        // Perform search
        searchUsersUseCase(query).collect { result ->
            val state = _uiState.value
            if (state is AddMembersUiState.Success) {
                when (result) {
                    is Result.Success -> {
                        Log.d("AddMembersVM", "✅ Found ${result.data.size} users")

                        // ✨ Filter out users already in trip
                        val existingUserIds = state.members
                            .mapNotNull { it.userId }  // Get user IDs of existing members
                            .toSet()

                        val filteredResults = result.data.filter { user ->
                            user.id !in existingUserIds
                        }

                        _uiState.value = state.copy(
                            searchResults = filteredResults,
                            isSearching = false,
                            hasSearched = true // ✨ Mark as searched
                        )
                    }
                    is Result.Error -> {
                        Log.e("AddMembersVM", "❌ Search failed: ${result.message}")
                        _uiState.value = state.copy(
                            isSearching = false,
                            hasSearched = true
                        )
                        _toastMessage.value = "Search failed: ${result.message}"
                    }
                    is Result.Loading -> {
                        // Keep searching state
                    }
                }
            }
        }
    }

    fun clearToastMessage() {
        _toastMessage.value = ""
    }
}