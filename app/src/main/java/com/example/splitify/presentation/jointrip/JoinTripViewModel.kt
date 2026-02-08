package com.example.splitify.presentation.jointrip

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitify.domain.usecase.trip.JoinTripUsingInviteCode
import com.example.splitify.domain.usecase.trip.ValidateInviteCodeUseCase
import com.example.splitify.presentation.jointrip.JoinTripUiState.Error
import com.example.splitify.presentation.jointrip.JoinTripUiState.TripFound
import com.example.splitify.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JoinTripViewModel @Inject constructor(
    private val validateInviteCodeUseCase: ValidateInviteCodeUseCase,
    private val joinTripUsingInviteCode: JoinTripUsingInviteCode,
): ViewModel() {

    private val _uiState = MutableStateFlow<JoinTripUiState>(JoinTripUiState.Idle)
    val uiState: StateFlow<JoinTripUiState> = _uiState.asStateFlow()

    private val _inviteCode = MutableStateFlow("")
    val inviteCode: StateFlow<String> = _inviteCode.asStateFlow()

    fun onInviteCodeChange(code: String) {
        _inviteCode.value = code.uppercase().take(8)
        if (_uiState.value is JoinTripUiState.Error) {
            _uiState.value = JoinTripUiState.Idle
        }
    }

    fun validateCode() {
        val code = _inviteCode.value.trim()
        if (code.isEmpty()) {
            _uiState.value = JoinTripUiState.Error("Please enter an invite code")
            return
        }

        viewModelScope.launch {
            _uiState.value = JoinTripUiState.Loading

            when (val result = validateInviteCodeUseCase(code)) {
                is Result.Success -> {
                    Log.d("JoinTripVM", "Trip found: ${result.data.name}")
                    _uiState.value = TripFound(result.data)
                }
                is Result.Error -> {
                    Log.e("JoinTripVM", "Validation failed: ${result.message}")
                    _uiState.value = Error(result.message)
                }
                Result.Loading -> Unit
            }
        }
    }

//    fun joinTrip() {
//        val state = _uiState.value
//        if (state !is JoinTripUiState.TripFound) return
//
//        viewModelScope.launch {
//            _uiState.value = JoinTripUiState.Joining
//
//            val userId = sessionManager.getCurrentUserId()
//            if (userId == null) {
//                _uiState.value = JoinTripUiState.Error("User not logged in")
//                return@launch
//            }
//
//            val displayName = sessionManager.getCurrentUserFlow().first()?.userName ?: "Unknown User"
//
//            when (val result = joinTripUseCase(state.trip.id, userId, displayName)) {
//                is Result.Success -> {
//                    Log.d("JoinTripVM", "Successfully joined trip")
//                    _uiState.value = JoinTripUiState.Success(state.trip.id)
//                }
//                is Result.Error -> {
//                    Log.e("JoinTripVM", "Failed to join: ${result.message}")
//                    _uiState.value = JoinTripUiState.Error(result.message)
//                }
//                Result.Loading -> Unit
//            }
//        }
//    }
//fun joinTrip() {
//    val state = _uiState.value
//    if (state !is JoinTripUiState.TripFound) return
//
//    viewModelScope.launch {
//        _uiState.value = JoinTripUiState.Joining
//
//        val code = _inviteCode.value.trim()
//
//        when (val result = joinTripUsingInviteCode(code)) {
//            is Result.Success -> {
//                Log.d("JoinTripVM", "Successfully joined trip and synced all data")
//
//                _uiState.value = JoinTripUiState.Success(result.data.id)
//            }
//            is Result.Error -> {
//                Log.e("JoinTripVM", "Failed to join: ${result.message}")
//                _uiState.value = JoinTripUiState.Error(result.message)
//            }
//            Result.Loading -> Unit
//        }
//    }
//}

    fun joinTrip() {
        val state = _uiState.value
        if (state !is JoinTripUiState.TripFound) return

        viewModelScope.launch {
            _uiState.value = JoinTripUiState.Joining

            val code = _inviteCode.value.trim()

            // ADD THIS DEBUG LOG
            Log.d("JoinTripVM", "🔍 Joining trip:")
            Log.d("JoinTripVM", "  Invite code: $code")
            Log.d("JoinTripVM", "  Trip from validation: ${state.trip.id}")
            Log.d("JoinTripVM", "  Trip name: ${state.trip.name}")

            when (val result = joinTripUsingInviteCode(code)) {
                is Result.Success -> {
                    Log.d("JoinTripVM", "✅ Joined trip: ${result.data.id}")

                    // ADD THIS CHECK
                    if (result.data.id != state.trip.id) {
                        Log.e("JoinTripVM", "❌ TRIP ID MISMATCH!")
                        Log.e("JoinTripVM", "  Expected: ${state.trip.id}")
                        Log.e("JoinTripVM", "  Got: ${result.data.id}")
                    }

                    _uiState.value = JoinTripUiState.Success(result.data.id)
                }
                is Result.Error -> {
                    Log.e("JoinTripVM", "Failed to join: ${result.message}")
                    val message = result.message
                    val userFriendlyMessage = when {
                        message.contains("unable to resolve host") ->
                            "No internet connection. Please check your network."
                        else -> "Something went wrong. Please try again."
                    }

                    _uiState.value = JoinTripUiState.Error(userFriendlyMessage)
                }
                Result.Loading -> Unit
            }
        }
    }

    fun resetState() {
        _uiState.value = JoinTripUiState.Idle
        _inviteCode.value = ""
    }





}