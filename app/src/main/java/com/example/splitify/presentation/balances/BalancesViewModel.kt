// presentation/balances/BalancesViewModel.kt
package com.example.splitify.presentation.balances

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitify.domain.repository.AuthRepository
import com.example.splitify.domain.usecase.balance.CalculateTripBalancesUseCase
import com.example.splitify.presentation.balances.BalancesUiState.*
import com.example.splitify.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Balances screen
 *
 * Manages calculation and display of trip balances and simplified debts
 */
@HiltViewModel
class BalancesViewModel @Inject constructor(
    private val calculateTripBalancesUseCase: CalculateTripBalancesUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BalancesUiState>(BalancesUiState.Loading)
    val uiState: StateFlow<BalancesUiState> = _uiState.asStateFlow()

    /**
     * Load balances for a specific trip
     *
     * @param tripId The trip to load balances for
     * @param currentMemberId The current user's member ID in this trip (optional)
     */
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun loadBalances(tripId: String, currentMemberId: String? = null) {
        viewModelScope.launch {
            _uiState.value = BalancesUiState.Loading

            // Get current user ID if not provided
            val currentUserId = currentMemberId ?: authRepository.getCurrentUser().firstOrNull()?.id

            // Calculate balances
            when (val result = calculateTripBalancesUseCase(tripId)) {
                is Result.Success -> {
                    val tripBalance = result.data

                    _uiState.value = Success(
                        memberBalances = tripBalance.memberBalances,
                        simplifiedDebts = tripBalance.simplifiedDebts,
                        currentUserId = currentUserId
                    )
                }
                is Result.Error -> {
                    _uiState.value = Error(result.message)
                }

                Result.Loading -> TODO()
            }
        }
    }

    /**
     * Refresh balances (call after settlement or new expense)
     */
    fun refresh(tripId: String, currentMemberId: String? = null) {
        loadBalances(tripId, currentMemberId)
    }
}