package com.example.splitify.presentation.tripdetail

import androidx.compose.runtime.collectAsState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitify.domain.model.TripMember
import com.example.splitify.domain.repository.AuthRepository
import com.example.splitify.domain.repository.ExpenseRepository
import com.example.splitify.domain.repository.TripRepository
import com.example.splitify.domain.usecase.member.GetTripMemberUseCase
import com.example.splitify.presentation.navigation.Screen
import com.example.splitify.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripDetailViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val expenseRepository: ExpenseRepository,
    private val getTripMemberUseCase: GetTripMemberUseCase,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    //Get trip id from navigation arguments
    private val tripId: String = checkNotNull(
        savedStateHandle[Screen.TripDetail.ARG_TRIP_ID]
    )

    private val _uiState = MutableStateFlow<TripDetailUiState>(TripDetailUiState.Loading)
    val uiState: StateFlow<TripDetailUiState> = _uiState.asStateFlow()

    private val _currentMemberId = MutableStateFlow<String?>(null)
    val currentMemberId: StateFlow<String?> = _currentMemberId.asStateFlow()

    init {
        loadTripDetails()
        loadCurrentMemberId()
        loadExpenses()
        loadMembers()
    }

     fun loadCurrentMemberId() {
        viewModelScope.launch {
            val currentUserId = authRepository.getCurrentUser().firstOrNull()?.id
            if (currentUserId != null) {
                // Get current user's member ID in this trip
                getTripMemberUseCase(tripId).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            val currentMember = result.data.find { it.userId == currentUserId }
                            _currentMemberId.value = currentMember?.id
                        }
                        is Result.Error -> {
                            // Handle error if needed
                        }

                        Result.Loading -> TODO()
                    }
                }
            }
        }
    }

    private fun loadMembers() {
        viewModelScope.launch {
            getTripMemberUseCase(tripId).collect { result ->
                _uiState.update { currentState ->
                    if (currentState is TripDetailUiState.Success) {
                        when (result) {
                            is Result.Success -> {
                                currentState.copy(
                                    members = result.data,
                                    memberCount = result.data.size
                                )
                            }

                            is Result.Error -> {
                                TripDetailUiState.Error(result.message)
                            }

                            is Result.Loading -> {
                                TripDetailUiState.Loading
                            }
                        }
                    } else {
                        currentState
                    }
                }
            }
        }
    }


    private fun loadExpenses() {
        viewModelScope.launch {
            expenseRepository.getExpensesByTrip(tripId).collect { result ->
                when(result){
                    is Result.Success -> {
                        val expenses = result.data
                        _uiState.update { currentState ->
                            if (currentState is TripDetailUiState.Success) {
                                currentState.copy(
                                    expenses = expenses,
                                    totalExpenses = expenses.sumOf { it.amount }
                                )
                            } else {
                                currentState
                            }
                        }
                    }

                    is Result.Error -> {
                        _uiState.update {
                            TripDetailUiState.Error(result.message)
                        }
                    }
                    Result.Loading -> TODO()
                }

            }
        }
    }

    private fun loadTripDetails(){
        viewModelScope.launch {
            try {
                val trip = tripRepository.getTripById(tripId)
                if(trip != null){
                    _uiState.value = TripDetailUiState.Success(
                        trip = trip,
                        totalExpenses = 0.0,
                        memberCount = 1,
                        members = emptyList()
                    )
                }
                else{
                    _uiState.value = TripDetailUiState.Error(message = "Trip not found")
                }
            }
            catch (e: Exception){
                _uiState.value = TripDetailUiState.Error(message = e.message ?: "Failed to load trips")
            }
        }
    }

    fun selectTab(tab: TripDetailTab){
        _uiState.update { currentState ->
            if (currentState is TripDetailUiState.Success){
                currentState.copy(currentTab = tab)
            }
            else{
                currentState
            }
        }
    }

    fun refresh(){
        loadTripDetails()
    }


}