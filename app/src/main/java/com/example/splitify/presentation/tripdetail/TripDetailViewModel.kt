package com.example.splitify.presentation.tripdetail

import androidx.compose.runtime.collectAsState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitify.domain.repository.ExpenseRepository
import com.example.splitify.domain.repository.TripRepository
import com.example.splitify.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripDetailViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val expenseRepository: ExpenseRepository,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    //Get trip id from navigation arguments
    private val tripId: String = checkNotNull(
        savedStateHandle[Screen.TripDetail.ARG_TRIP_ID]
    )

    private val _uiState = MutableStateFlow<TripDetailUiState>(TripDetailUiState.Loading)
    val uiState: StateFlow<TripDetailUiState> = _uiState.asStateFlow()

    init {
        loadTripDetails()
        loadExpenses()
    }

    private fun loadExpenses() {
        viewModelScope.launch {
            expenseRepository.getExpensesByTrip(tripId).collect { expenses ->
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
                        memberCount = 1
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