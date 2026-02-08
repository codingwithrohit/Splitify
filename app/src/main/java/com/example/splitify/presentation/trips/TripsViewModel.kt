package com.example.splitify.presentation.trips

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitify.data.local.SessionManager
import com.example.splitify.data.local.dao.TripDao
import com.example.splitify.data.local.toDomainModels
import com.example.splitify.data.sync.RealtimeManager
import com.example.splitify.domain.repository.AuthRepository
import com.example.splitify.domain.repository.TripRepository
import com.example.splitify.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripsViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val sessionManager: SessionManager,
    private val tripDao: TripDao,
    private val authRepository: AuthRepository,
    private val realtimeManager: RealtimeManager
): ViewModel() {

    private val _uiState = MutableStateFlow<TripsUiState>(TripsUiState.InitialLoading)
    val uiState: StateFlow<TripsUiState> = _uiState.asStateFlow()

    private val _logoutEvent = Channel<Unit>()
    val logoutEvent = _logoutEvent.receiveAsFlow()

    private val currentUserId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = currentUserId.asStateFlow()


    init {

        loadCurrentUser()

        viewModelScope.launch {
            val userId = sessionManager.getCurrentUserId()
            if (userId != null) {
                realtimeManager.subscribeToGlobalTrips()
                Log.d("TripsVM", "✅ Subscribed to global trip deletes only")
            }
        }

    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val userId = sessionManager.getCurrentUserId()
                if (userId == null) {
                    Log.e("TripsVM", "❌ No user ID found!")
                    _uiState.value = TripsUiState.Error("Not logged in")
                    return@launch
                }

                Log.d("TripsVM", "✅ User ID: $userId")
                currentUserId.value = userId

                // Start observing trips
                observeTrips()

                // Optional: Sync in background (don't block UI)
                syncIfNeeded()

            } catch (e: Exception) {
                Log.e("TripsVM", "❌ Failed to load user", e)
                _uiState.value = TripsUiState.Error("Failed to load user")
            }
        }
    }


    private fun observeTrips() {
        viewModelScope.launch {
            val userId = currentUserId.value ?: return@launch

            tripDao.getTripsByUser(userId).collect { trips ->
                _uiState.update { current ->
                    if (trips.isEmpty()) {
                        TripsUiState.Empty(isSyncing = current.isSyncing())
                    } else {
                        Log.d("TripsVM", " UI Update: ${trips.size} trips (Syncing: ${current.isSyncing()})")
                        TripsUiState.Content(
                            trips = trips.toDomainModels(),
                            isSyncing = current.isSyncing()
                        )
                    }
                }
            }
        }
    }

    private fun syncIfNeeded() {
        viewModelScope.launch {
            val userId = currentUserId.value ?: return@launch

            if (!sessionManager.hasValidSession()) {
                Log.d("TripsVM", "⚠️ No valid session, skipping sync")
                return@launch
            }

            Log.d("TripsVM", "🔄 Starting background sync...")
            _uiState.update { it.withSyncing(true) }

            when (val result = tripRepository.syncTrips()) {
                is Result.Success -> {
                    Log.d("TripsVM", "✅ Sync completed")
                    _uiState.update { it.withSyncing(false) }
                }
                is Result.Error -> {
                    Log.e("TripsVM", "❌ Sync failed: ${result.message}")
                    _uiState.update {
                        TripsUiState.Error(
                            message = "Failed to sync trips",
                            trips = tripDao
                                .getTripsByUser(userId)
                                .first()
                                .toDomainModels()
                        )
                    }
                }
                Result.Loading -> Unit
            }
        }
    }


    fun refresh() {
        Log.d("TripsVM", "Manual refresh requested")
        syncIfNeeded()
    }

    fun deleteTrip(tripId: String) {
        viewModelScope.launch {
            Log.d("TripsVM", "🗑️ Deleting trip: $tripId")

            when (val result = tripRepository.deleteTrip(tripId)) {
                is Result.Success -> {
                    Log.d("TripsVM", "✅ Trip deleted")
                }
                is Result.Error -> {
                    Log.e("TripsVM", "❌ Delete failed: ${result.message}")
                    _uiState.value = TripsUiState.Error(result.message)
                }
                Result.Loading -> Unit
            }
        }
    }


    fun logout() {
        viewModelScope.launch {
            Log.d("TripsVM", "🚪 Logging out...")
            realtimeManager.unsubscribeAll()
            val result = authRepository.signOut()
            if (result is Result.Success) {
                _logoutEvent.send(Unit)
            }else {
                _uiState.value = TripsUiState.Error("Failed to logout")
                Log.d("TripsVM", "❌ Logout failed: ${result.isError}")
                Log.d("TripsVM", "❌ Logout failed: $result")
            }

            Log.d("TripsVM", "✅ Logout complete")
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Only unsubscribe from global channel, not individual trips
        Log.d("TripsVM", "🔕 TripsViewModel cleared")
    }

    private fun TripsUiState.isSyncing(): Boolean = when (this) {
        is TripsUiState.Content -> isSyncing
        is TripsUiState.Empty -> isSyncing
        else -> false
    }

    private fun TripsUiState.withSyncing(syncing: Boolean): TripsUiState = when (this) {
        is TripsUiState.Content -> copy(isSyncing = syncing)
        is TripsUiState.Empty -> copy(isSyncing = syncing)
        else -> this
    }
}