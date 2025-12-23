package com.example.splitify.domain.model

import kotlin.math.abs

data class Balance(
    val member: TripMember,         // Full member object (contains id, displayName, etc.)
    val totalPaid: Double,          // Total amount this person paid
    val totalOwed: Double,          // Total amount this person owes
    val netBalance: Double
) {

    val isCreditor: Boolean get() = netBalance > 0.01  // Others owe them
    val isDebtor: Boolean get() = netBalance < -0.01   // They owe others
    val isSettled: Boolean get() = abs(netBalance) < 0.01  // All balanced

    companion object {
        fun create(
            member: TripMember,
            totalPaid: Double,
            totalOwed: Double
        ): Balance {
            val net = totalPaid - totalOwed
            return Balance(
                member = member,
                totalPaid = totalPaid,
                totalOwed = totalOwed,
                netBalance = net
            )
        }
        private const val EPSILON = 0.01
    }
}