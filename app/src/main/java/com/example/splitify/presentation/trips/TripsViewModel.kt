package com.example.splitify.presentation.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitify.domain.model.Trip
import com.example.splitify.domain.repository.AuthRepository
import com.example.splitify.domain.repository.TripRepository
import com.example.splitify.presentation.trips.TripsUiState.*
import com.example.splitify.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate

import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for Trips List screen
 *
 * Responsibilities:
 * - Load trips from repository
 * - Manage UI state
 * - Handle user actions (create trip, delete trip, etc.)
 * - Survive configuration changes (rotation)
 *
 * @HiltViewModel - Hilt creates this automatically
 * @Inject constructor - Hilt provides the repository
 */
@HiltViewModel
class TripsViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val authRepository: AuthRepository
): ViewModel() {

    private val _uiState = MutableStateFlow<TripsUiState>(TripsUiState.Loading)
    val uiState: StateFlow<TripsUiState> = _uiState.asStateFlow()
    private var currentUserId: String? = null
    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser(){
        viewModelScope.launch {
            authRepository.getCurrentUser().collect {user ->
                currentUserId = user?.id
            }
            if(currentUserId != null){
                loadTripsForUser(currentUserId!!)
            }
            else{
                _uiState.value = Error("Not logged in")
            }
        }
    }

    private fun loadTripsForUser(userId: String){
        viewModelScope.launch {
            tripRepository.getTripsByUser(userId)
                .map<List<Trip>, TripsUiState> {trips ->
                    TripsUiState.Success(trips = trips)
                }
                .catch { exception ->
                    emit(TripsUiState.Error(exception.message ?: "Failed to load trips"))
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    private fun loadTrips(){
        viewModelScope.launch {
            tripRepository.getAllTrips()
                .map<List<Trip>, TripsUiState> { trips ->
                    TripsUiState.Success(trips)
                }
                .catch { exception ->
                    emit(TripsUiState.Error(exception.message ?: "Failed to load trips"))
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }



     fun deleteTrip(tripId: String){
        viewModelScope.launch {
            val result = tripRepository.deleteTrip(tripId)
            when(result){
                is Result.Success -> {
                    println("✅ Trip deleted: $tripId")
                }
                is Result.Error -> {
                    _uiState.value = Error(result.message)
                }

                Result.Loading -> TODO()
            }
        }

    }

    fun logout(){
        viewModelScope.launch {
            // Clear local trips before logout
            tripRepository.clearLocalTrips()
            authRepository.signOut()
        }
    }



    fun refresh() {
        loadTrips()
    }

    private fun generateInviteCode(): String{
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789"
        return (1..8)
            .map { chars.random() }
            .joinToString("")
    }
}