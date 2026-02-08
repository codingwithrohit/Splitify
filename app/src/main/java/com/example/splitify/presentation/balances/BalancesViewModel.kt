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
        getSettlementsForTripUseCase(tripId),
        authRepository.getCurrentUser()
    ) { balanceResult, settlementsResult, user ->

        when {
            balanceResult is Result.Error -> {
                Log.e("BalancesVM", "❌ Balance calculation error: ${balanceResult.message}")
                BalancesUiState.Error(balanceResult.message)
            }

            settlementsResult is Result.Error -> {
                Log.e("BalancesVM", "❌ Settlements error: ${settlementsResult.message}")
                BalancesUiState.Error(settlementsResult.message)
            }

            balanceResult is Result.Success && settlementsResult is Result.Success -> {
                val tripBalance = balanceResult.data
                val settlements = settlementsResult.data

                Log.d("BalancesVM", "📊 Processing balances for trip: $tripId")
                Log.d("BalancesVM", "  Members: ${tripBalance.memberBalances.size}")
                Log.d("BalancesVM", "  Total expenses: ₹${tripBalance.totalExpenses}")

                // ✅ Filter out debts that have pending or confirmed settlements
                val settledDebts = settlements
                    .filter { it.status != SettlementStatus.DISPUTED }
                    .map { it.fromMemberId to it.toMemberId }
                    .toSet()

                val activeDebts = tripBalance.simplifiedDebts.filter { debt ->
                    (debt.fromMemberId to debt.toMemberId) !in settledDebts
                }

                Log.d("BalancesVM", "📊 Active debts: ${activeDebts.size} (filtered from ${tripBalance.simplifiedDebts.size})")

                // ✅ IMPROVED: Round balances to 2 decimal places before verification
                val roundedBalances = tripBalance.memberBalances.map { balance ->
                    balance.copy(
                        totalPaid = (balance.totalPaid * 100).roundToInt() / 100.0,
                        totalOwed = (balance.totalOwed * 100).roundToInt() / 100.0,
                        netBalance = (balance.netBalance * 100).roundToInt() / 100.0
                    )
                }

                val sumOfBalances = roundedBalances.sumOf { it.netBalance }

                Log.d("BalancesVM", "🔍 Balance verification:")
                Log.d("BalancesVM", "  Sum of balances: $sumOfBalances")
                Log.d("BalancesVM", "  Members: ${roundedBalances.size}")
                roundedBalances.forEach { balance ->
                    Log.d("BalancesVM", "  ${balance.member.displayName}: ₹${balance.netBalance}")
                }

                // ✅ FIXED: Use proper floating-point tolerance (0.10 rupees = 10 paisa)
                val tolerance = 0.10
                if (abs(sumOfBalances) > tolerance) {
                    Log.e("BalancesVM", "❌ Balance mismatch: sum = $sumOfBalances (tolerance: $tolerance)")
                    BalancesUiState.Error("Balance calculation error: Sum is ₹${"%.2f".format(sumOfBalances)}")
                } else {
                    Log.d("BalancesVM", "✅ Balances verified (sum=${"%.2f".format(sumOfBalances)})")
                    BalancesUiState.Success(
                        memberBalances = roundedBalances, // ✅ Use rounded balances
                        simplifiedDebts = activeDebts,
                        settlements = settlements,
                        currentUserId = user?.id
                    )
                }
            }

            else -> {
                Log.d("BalancesVM", "⏳ Loading balances...")
                BalancesUiState.Loading
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BalancesUiState.Loading
    )


    fun retry(){
        retryTrigger.value++
    }


}