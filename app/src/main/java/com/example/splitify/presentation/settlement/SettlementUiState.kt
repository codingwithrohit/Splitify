package com.example.splitify.presentation.settlement

import com.example.splitify.domain.model.Settlement
import com.example.splitify.domain.model.TripMember

sealed interface SettlementUiState{
    data object Initial: SettlementUiState
    data object Loading: SettlementUiState

    data class Success(
        val settlement: Settlement,
        val fromMember: TripMember,
        val toMember: TripMember
    ): SettlementUiState

    data class Error(val message: String): SettlementUiState
}

sealed interface SettlementListUiState{
    data object Loading: SettlementListUiState
    data class Success(
        val settlements: List<SettlementWithMember>
    ): SettlementListUiState
    data class Error(val message: String): SettlementListUiState

}

data class SettlementWithMember(
    val settlement: Settlement,
    val fromMember: TripMember,
    val toMember: TripMember
)