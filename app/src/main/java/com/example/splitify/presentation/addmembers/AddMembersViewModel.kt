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
import kotlinx.coroutines.launch
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

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    init {
        loadMembers()
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
                _toastMessage.emit("Name cannot be empty")
            }
            return
        }

        if (trimmedName.length < 2) {
            viewModelScope.launch {
                _toastMessage.emit("Name must be at least 2 characters")
            }
            return
        }

        Log.d("AddMembersVM", "════════════════════════════════")
        Log.d("AddMembersVM", "🔄 ADDING MEMBER")
        Log.d("AddMembersVM", "  Trip ID: $tripId")
        Log.d("AddMembersVM", "  Member Name: '$trimmedName'")
        Log.d("AddMembersVM", "  User ID: null (guest)")
        Log.d("AddMembersVM", "════════════════════════════════")

        viewModelScope.launch {
            when(val result = addTripMemberUseCase(
                tripId = tripId,
                displayName = trimmedName,
                userId = null
            )){
                is Result.Success -> {
                    Log.d("AddMembersVM", "✅ SUCCESS: Member added")
                    _toastMessage.emit("${name.trim()} added to trip")
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
                    _toastMessage.emit(userFriendlyMessage)
                }
                else -> {
                    Log.w("AddMembersVM", "⚠️ Unexpected result: Loading state")
                }
            }
        }
    }

    fun removeMember(memberId: String, memberName: String) {
        viewModelScope.launch {
            when (val result = removeTripMemberUseCase(tripId, memberId)) {
                is Result.Success -> {
                    _toastMessage.emit("$memberName removed")
                }
                is Result.Error -> {
                    _toastMessage.emit(result.message)
                }
                else -> {}
            }
        }
    }

    fun searchUsers(query: String) {
        if (query.isBlank()) {
            // Reset search results
            val currentState = _uiState.value
            if (currentState is AddMembersUiState.Success) {
                _uiState.value = currentState.copy(
                    searchResults = emptyList(),
                    isSearching = false
                )
            }
            return
        }

        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is AddMembersUiState.Success) {
                _uiState.value = currentState.copy(isSearching = true)
            }

            searchUsersUseCase(query).collect { result ->
                val state = _uiState.value
                if (state is AddMembersUiState.Success) {
                    when (result) {
                        is Result.Success -> {
                            _uiState.value = state.copy(
                                searchResults = result.data,
                                isSearching = false
                            )
                        }
                        is Result.Error -> {
                            _uiState.value = state.copy(isSearching = false)
                            _toastMessage.emit("Search failed: ${result.message}")
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}