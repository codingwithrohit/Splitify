package com.example.splitify.presentation.balances

/**
 * Represents the state of a settlement button for a debt
 */
sealed interface SettlementButtonState {
    /**
     * No settlement exists - show "Settle Up" button
     */
    object NoSettlement : SettlementButtonState

    /**
     * Settlement is pending confirmation
     */
    data class Pending(
        val settlementId: String,
        val amount: Double,
        val canConfirm: Boolean,    // Is current user the receiver?
        val canCancel: Boolean,      // Is current user the payer?
        val isGuest: Boolean = false // Is receiver a guest user?
    ) : SettlementButtonState

    /**
     * Settlement was confirmed - show as settled
     */
    data class Confirmed(
        val amount: Double,
        val date: Long
    ) : SettlementButtonState
}