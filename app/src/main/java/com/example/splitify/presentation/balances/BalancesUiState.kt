package com.example.splitify.presentation.balances

import com.example.splitify.domain.model.Balance
import com.example.splitify.domain.model.Settlement
import com.example.splitify.domain.model.SimplifiedDebt
import com.example.splitify.domain.model.TripBalance

sealed interface BalancesUiState{
    object Loading: BalancesUiState
    data class Success(
        val memberBalances: List<Balance>,
        val simplifiedDebts: List<SimplifiedDebt>,
        val settlements: List<Settlement>,
        val currentUserId: String?
    ) : BalancesUiState
    data class Error(val message: String): BalancesUiState
}