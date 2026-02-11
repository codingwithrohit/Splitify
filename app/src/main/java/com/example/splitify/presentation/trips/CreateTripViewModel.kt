package com.example.splitify.presentation.trips

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitify.domain.model.NotificationTemplates
import com.example.splitify.domain.repository.AuthRepository
import com.example.splitify.domain.usecase.trip.CreateTripUseCase
import com.example.splitify.domain.usecase.trip.GetTripUseCase
import com.example.splitify.domain.usecase.trip.UpdateTripUseCase
import com.example.splitify.presentation.navigation.Screen
import com.example.splitify.util.NotificationManager
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
    private val notificationManager: NotificationManager,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    companion object {
        private const val TAG = "CreateTripVM"
    }

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
        Log.d(TAG, "🎬 ViewModel initialized")
        Log.d(TAG, "  Mode: ${if (mode is CreateTripFormMode.EditTrip) "EDIT" else "CREATE"}")
        Log.d(TAG, "  Trip ID: $tripId")

        viewModelScope.launch {
            authRepository.getCurrentUser().collect{user ->
                currentUserId = user?.id
                Log.d(TAG, "👤 Current user: ${user?.userName} (${user?.id})")
            }
        }
        if(mode is CreateTripFormMode.EditTrip){
            loadTripForEditing()
        }
    }

    private fun loadTripForEditing(){
        Log.d(TAG, "📥 Loading trip for editing: $tripId")
        viewModelScope.launch {
            getTripUseCase(tripId = tripId!!).collect { result ->
                when(result){
                    is Result.Error -> {
                        Log.e(TAG, "❌ Failed to load trip: ${result.message}")
                        _uiState.update {
                            it.copy(
                                nameError = result.message
                            )
                        }
                    }
                    Result.Loading -> {
                        Log.d(TAG, "⏳ Loading trip...")
                    }
                    is Result.Success -> {
                        val trip = result.data
                        Log.d(TAG, "✅ Trip loaded: ${trip.name}")
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

        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "💾 SAVE TRIP CALLED")
        Log.d(TAG, "  Name: ${state.name}")
        Log.d(TAG, "  Mode: ${if (mode is CreateTripFormMode.EditTrip) "EDIT" else "CREATE"}")

        // Validation
        if(state.name.isBlank()){
            Log.w(TAG, "⚠️ Validation failed: Name is blank")
            _uiState.update {
                it.copy(
                    nameError = "Trip is required"
                )
            }
            return
        }

        if(state.endDate != null && state.endDate.isBefore(state.startDate)){
            Log.w(TAG, "⚠️ Validation failed: End date before start date")
            _uiState.update {
                it.copy(
                    dateError = "End date cannot be before start date"
                )
            }
            return
        }

        Log.d(TAG, "✅ Validation passed")

        //Create Trip
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {

            if(currentUserId==null){
                Log.e(TAG, "❌ No user logged in!")
                _uiState.update { it.copy(
                    isLoading = false,
                    nameError = "You must be logged in to create a trip"
                ) }
                return@launch
            }

            Log.d(TAG, "🚀 Executing ${if (mode is CreateTripFormMode.EditTrip) "update" else "create"} use case...")

            val result = when(mode){
                is CreateTripFormMode.CreateTrip -> {
                    Log.d(TAG, "  Creating new trip...")
                    createTripUseCase(
                        name = state.name,
                        description = state.description,
                        startDate = state.startDate,
                        endDate = state.endDate,
                        inviteCode = state.inviteCode,
                    )
                }

                is CreateTripFormMode.EditTrip -> {
                    Log.d(TAG, "  Updating existing trip: $tripId")
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
                    Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    Log.d(TAG, "✅ SUCCESS!")
                    Log.d(TAG, "  Trip: ${result.data.name}")
                    Log.d(TAG, "  ID: ${result.data.id}")

                    val createdId = when (mode) {
                        is CreateTripFormMode.CreateTrip -> result.data.id
                        is CreateTripFormMode.EditTrip -> tripId!!
                    }

                    // ✅ NOTIFICATION LOGIC
                    try {
                        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                        Log.d(TAG, "📢 SENDING NOTIFICATION")
                        Log.d(TAG, "  Getting notification template...")

                        val notification = if (mode is CreateTripFormMode.CreateTrip) {
                            NotificationTemplates.tripCreated(result.data.name)
                        } else {
                            NotificationTemplates.tripUpdated(result.data.name)
                        }

                        Log.d(TAG, "  Notification created:")
                        Log.d(TAG, "    Title: ${notification.title}")
                        Log.d(TAG, "    Message: ${notification.message}")
                        Log.d(TAG, "    Type: ${notification.type}")

                        Log.d(TAG, "  Calling notificationManager.showNotification()...")
                        notificationManager.showNotification(notification)

                        Log.d(TAG, "✅ Notification sent successfully!")
                        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ NOTIFICATION FAILED!", e)
                        Log.e(TAG, "  Error: ${e.message}")
                        Log.e(TAG, "  Stack trace:")
                        e.printStackTrace()
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSaved = true,
                            createdTripId = createdId
                        )
                    }
                }
                is Result.Error -> {
                    Log.e(TAG, "❌ FAILED!")
                    Log.e(TAG, "  Error: ${result.message}")
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