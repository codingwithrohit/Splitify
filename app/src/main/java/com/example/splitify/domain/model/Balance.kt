package com.example.splitify.domain.model

import kotlin.math.abs
import kotlin.math.roundToInt

data class Balance(
    val member: TripMember,
    val totalPaid: Double,
    val totalOwed: Double,
    val netBalance: Double
) {

    val isCreditor: Boolean get() = netBalance > 0.01  // Others owe them
    val isDebtor: Boolean get() = netBalance < -0.01   // They owe others
    val isSettled: Boolean get() = abs(netBalance) < 0.01  // All balanced

//    companion object {
//        fun create(
//            member: TripMember,
//            totalPaid: Double,
//            totalOwed: Double
//        ): Balance {
//            val net = totalPaid - totalOwed
//            return Balance(
//                member = member,
//                totalPaid = totalPaid,
//                totalOwed = totalOwed,
//                netBalance = net
//            )
//        }
//        private const val EPSILON = 0.01
//    }
    companion object {
        fun create(
            member: TripMember,
            totalPaid: Double,
            totalOwed: Double
        ): Balance {

            val roundedPaid = (totalPaid * 100).roundToInt() / 100.0
            val roundedOwed = (totalOwed * 100).roundToInt() / 100.0
            val netBalance = roundedPaid - roundedOwed

            return Balance(
                member = member,
                totalPaid = roundedPaid,
                totalOwed = roundedOwed,
                netBalance = netBalance
            )
        }
    }
}