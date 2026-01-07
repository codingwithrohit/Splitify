// presentation/balances/BalancesViewModel.kt
package com.example.splitify.presentation.balances

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitify.domain.model.SettlementStatus
import com.example.splitify.domain.repository.AuthRepository
import com.example.splitify.domain.usecase.balance.CalculateTripBalancesUseCase
import com.example.splitify.domain.usecase.settlement.GetSettlementsForTripUseCase
import com.example.splitify.presentation.balances.BalancesUiState.*
import com.example.splitify.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
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
    private val getSettlementsForTripUseCase: GetSettlementsForTripUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val retryTrigger = MutableStateFlow(0)
    private val tripId: String = checkNotNull(savedStateHandle["tripId"])

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    val uiState: StateFlow<BalancesUiState> = combine(
        calculateTripBalancesUseCase(tripId),
        getSettlementsForTripUseCase(tripId)
    ) { balanceResult, settlementsResult ->

        when {
            balanceResult is Result.Error -> {
                BalancesUiState.Error(balanceResult.message)
            }

            settlementsResult is Result.Error -> {
                BalancesUiState.Error(settlementsResult.message)
            }

            balanceResult is Result.Success && settlementsResult is Result.Success -> {
                val tripBalance = balanceResult.data
                val settlements = settlementsResult.data

                // ✅ Filter out debts that have pending or confirmed settlements
                val settledDebts = settlements
                    .filter { it.status != SettlementStatus.DISPUTED }
                    .map { it.fromMemberId to it.toMemberId }
                    .toSet()

                val activeDebts = tripBalance.simplifiedDebts.filter { debt ->
                    (debt.fromMemberId to debt.toMemberId) !in settledDebts
                }

                Log.d("BalancesVM", "📊 Active debts: ${activeDebts.size} (filtered from ${tripBalance.simplifiedDebts.size})")

                // Verify balance calculation
                val sumOfBalances = tripBalance.memberBalances.sumOf { it.netBalance }

                if (abs(sumOfBalances) > 0.01) {
                    BalancesUiState.Error("Balance calculation error")
                } else {
                    BalancesUiState.Success(
                        memberBalances = tripBalance.memberBalances,
                        simplifiedDebts = activeDebts, // ✅ Use filtered debts
                        settlements = settlements,      // ✅ Pass settlements for button state
                        currentUserId = authRepository
                            .getCurrentUser()
                            .firstOrNull()
                            ?.id
                    )
                }
            }

            else -> BalancesUiState.Loading
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BalancesUiState.Loading
    )

//    val uiState: StateFlow<BalancesUiState> =
//        calculateTripBalancesUseCase(tripId)
//            .map { result ->
//                when (result) {
//                    is Result.Success -> {
//                        val tripBalance = result.data
//
//                        val sumOfBalances =
//                            tripBalance.memberBalances.sumOf { it.netBalance }
//
//                        if (abs(sumOfBalances) > 0.01) {
//                            BalancesUiState.Error("Balance calculation error")
//                        } else {
//                            BalancesUiState.Success(
//                                memberBalances = tripBalance.memberBalances,
//                                simplifiedDebts = tripBalance.simplifiedDebts,
//                                currentUserId = authRepository
//                                    .getCurrentUser()
//                                    .firstOrNull()
//                                    ?.id
//                            )
//                        }
//                    }
//
//                    is Result.Error ->
//                        BalancesUiState.Error(result.message)
//
//                    Result.Loading ->
//                        BalancesUiState.Loading
//                }
//            }
//            .stateIn(
//                scope = viewModelScope,
//                started = SharingStarted.WhileSubscribed(5000),
//                initialValue = BalancesUiState.Loading
//            )

    fun retry(){
        retryTrigger.value++
    }


}