package com.example.splitify.presentation.trips

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitify.domain.repository.AuthRepository
import com.example.splitify.domain.usecase.trip.CreateTripUseCase
import com.example.splitify.domain.usecase.trip.GetTripUseCase
import com.example.splitify.domain.usecase.trip.UpdateTripUseCase
import com.example.splitify.presentation.navigation.Screen
import com.example.splitify.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class CreateTripViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val createTripUseCase: CreateTripUseCase,
    private val updateTripUseCase: UpdateTripUseCase,
    private val getTripUseCase: GetTripUseCase,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    private val _uiState = MutableStateFlow(CreateTripUiState(
        inviteCode = generateInviteCode()
    ))

    val uiState: StateFlow<CreateTripUiState> = _uiState.asStateFlow()
    private val tripId: String? = savedStateHandle[Screen.EditTrip.ARG_TRIP_ID]
    private var currentUserId: String? = null

    val mode: CreateTripFormMode = if(tripId != null){
        CreateTripFormMode.EditTrip(tripId = tripId)
    }else{
        CreateTripFormMode.CreateTrip
    }

    init {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect{user ->
                currentUserId = user?.id
            }
        }
        if(mode is CreateTripFormMode.EditTrip){
            loadTripForEditing()
        }
    }

    private fun loadTripForEditing(){
        viewModelScope.launch {
            getTripUseCase(tripId = tripId!!).collect { result ->
                when(result){
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                nameError = result.message
                            )
                        }
                    }
                    Result.Loading -> {}
                    is Result.Success -> {
                        val trip = result.data
                        _uiState.update {
                            it.copy(
                                name = trip.name,
                                description = trip.description.orEmpty(),
                                startDate = trip.startDate,
                                endDate = trip.endDate,
                                inviteCode = trip.inviteCode
                            )
                        }
                    }
                }
            }
        }
    }

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

            if(currentUserId==null){
                _uiState.update { it.copy(
                    isLoading = false,
                    nameError = "You must be logged in to create a trip"
                ) }
                return@launch
            }

            val result = when(mode){
                is CreateTripFormMode.CreateTrip -> {
                    createTripUseCase(
                        name = state.name,
                        description = state.description,
                        startDate = state.startDate,
                        endDate = state.endDate,
                        inviteCode = state.inviteCode,
                    )
                }

                is CreateTripFormMode.EditTrip -> {
                    updateTripUseCase(
                        name = state.name,
                        description = state.description,
                        startDate = state.startDate,
                        endDate = state.endDate,
                        inviteCode = state.inviteCode,
                        tripId = tripId!!
                    )
                }
            }
            when(result) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSaved = true) }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, nameError = result.message)
                    }
                }
                else -> Unit
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

