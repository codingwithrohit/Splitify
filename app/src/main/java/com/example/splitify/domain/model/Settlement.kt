package com.example.splitify.domain.model

data class Settlement(
    val id: String,
    val tripId: String,
    val fromMemberId: String,
    val toMemberId: String,
    val amount: Double,
    val status: SettlementStatus,
    val notes: String? = null,
    val createdAt: Long,
    val settledAt: Long? = null
)

enum class SettlementStatus {
    PENDING,    // Payer marked as paid, waiting for receiver confirmation
    CONFIRMED,  // Receiver confirmed receipt
    DISPUTED    // Either party disputed the payment
}
