package com.example.splitify.presentation.addmembers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitify.domain.usecase.member.GetTripMemberUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import com.example.splitify.util.Result
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MembersViewModel @Inject constructor(
    private val getTripMembersUseCase: GetTripMemberUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tripId: String = checkNotNull(savedStateHandle["tripId"])

    val uiState: StateFlow<MembersUiState> = getTripMembersUseCase(tripId)
        .map { result ->
            when (result) {
                is Result.Success -> MembersUiState.Success(result.data)
                is Result.Error -> MembersUiState.Error(result.message)
                Result.Loading -> MembersUiState.Loading
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MembersUiState.Loading
        )
}