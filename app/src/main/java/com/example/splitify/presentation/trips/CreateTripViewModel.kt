package com.example.splitify.presentation.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.splitify.domain.model.Trip
import com.example.splitify.domain.repository.TripRepository
import com.example.splitify.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateTripViewModel @Inject constructor(
    private val tripRepository: TripRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(CreateTripUiState(
        inviteCode = generateInviteCode()
    ))

    val uiState: StateFlow<CreateTripUiState> = _uiState.asStateFlow()

    //Update Name
    fun onNameChange(name: String){
        _uiState.update { it.copy(
            name = name,
            nameError = null
        ) }
    }

    //Update Description
    fun onDescriptionChange(description: String){
        _uiState.update { it.copy(
            description = description
        ) }
    }

    //Update Start Date
    fun onStartDateChange(date: LocalDate?){
        _uiState.update { it.copy(
            startDate = date ?: it.startDate,
            dateError = null
        ) }
    }

    //Update End date
    fun onEndDateChange(date: LocalDate?){
        _uiState.update { it.copy(
            endDate = date,
            dateError = null
        ) }
    }

    //Regenerate Invite code
    fun regenerateInviteCode(){
        _uiState.update {
            it.copy(
                inviteCode = generateInviteCode()
            )
        }
    }

    fun resetSaveState(){
        _uiState.update { it.copy(isSaved = false)}
    }

    //Validate and Save Trip
    fun saveTrip(){
        val state = _uiState.value

        if(state.name.isBlank()){
            _uiState.update {
                it.copy(
                    nameError = "Trip is required"
                )
            }
            return
        }
        if(state.endDate != null && state.endDate.isBefore(state.startDate)){
            _uiState.update {
                it.copy(
                    dateError = "End date cannot be before start date"
                )
                return
            }
        }

        //Create Trip
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val trip = Trip(
                id = UUID.randomUUID().toString(),
                name = state.name,
                description = state.description,
                startDate = state.startDate,
                endDate = state.endDate,
                inviteCode = state.inviteCode,
                createdBy = "test-user-1",
                isLocal = true
            )
            when( val result = tripRepository.createTrip(trip)){
                is Result.Success ->{
                    _uiState.update { it.copy(
                        isLoading = false,
                        isSaved = true
                    ) }
                }
                is Result.Error ->{
                    _uiState.update { it.copy(
                        isLoading = false,
                        nameError = "Failed to create trip: ${result.message}"
                    ) }
                }
            }
        }

    }
    private fun generateInviteCode(): String{
        val chars: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..8)
            .map { chars.random() }
            .joinToString("")
    }
}

