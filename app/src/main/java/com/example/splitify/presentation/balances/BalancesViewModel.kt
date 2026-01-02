// presentation/balances/BalancesViewModel.kt
package com.example.splitify.presentation.balances

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitify.domain.repository.AuthRepository
import com.example.splitify.domain.usecase.balance.CalculateTripBalancesUseCase
import com.example.splitify.presentation.balances.BalancesUiState.*
import com.example.splitify.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.*

/**
 * ViewModel for the Balances screen
 *
 * Manages calculation and display of trip balances and simplified debts
 */
@HiltViewModel
class BalancesViewModel @Inject constructor(
    private val calculateTripBalancesUseCase: CalculateTripBalancesUseCase,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {


    private val tripId: String = checkNotNull(savedStateHandle["tripId"])

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    val uiState: StateFlow<BalancesUiState> = flow {
        val currentUserId = authRepository.getCurrentUser().firstOrNull()?.id

        when (val result = calculateTripBalancesUseCase(tripId = tripId)) {
            is Result.Success -> {
                val tripBalance = result.data

                // Verify balances
                val sumOfBalances = tripBalance.memberBalances.sumOf { it.netBalance }
                if (abs(sumOfBalances) > 0.01) {
                    Log.e("BalancesVM", "❌ BALANCE ERROR: Sum is ₹$sumOfBalances")
                    emit(Error("Balance calculation error"))
                } else {
                    Log.d("BalancesVM", "✅ Balances verified. Sum = ₹$sumOfBalances")
                    emit(
                        Success(
                            memberBalances = tripBalance.memberBalances,
                            simplifiedDebts = tripBalance.simplifiedDebts,
                            currentUserId = currentUserId
                        )
                    )
                }
            }
            is Result.Error -> {
                emit(Error(result.message))
            }

            Result.Loading -> Unit
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),  // ✅ Cache for 5 seconds
            initialValue = BalancesUiState.Loading
        )


}