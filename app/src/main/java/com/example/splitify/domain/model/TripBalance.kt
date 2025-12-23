// domain/model/TripBalance.kt
package com.example.splitify.domain.model

/**
 * Represents the complete balance state for a trip
 *
 * Contains:
 * - Individual member balances
 * - Simplified debts (minimum transactions to settle)
 * - Total expenses
 * - Calculation timestamp
 */
data class TripBalance(
    val tripId: String,
    val memberBalances: List<Balance>,
    val simplifiedDebts: List<SimplifiedDebt>,
    val totalExpenses: Double,
    val calculatedAt: Long = System.currentTimeMillis()
) {

    /**
     * Check if all members are settled (no one owes anyone)
     */
    val isAllSettled: Boolean
        get() = memberBalances.all { it.isSettled }

    /**
     * Get balance for a specific member
     *
     * @param memberId The member ID to search for
     * @return Balance for the member, or null if not found
     */
    fun getBalanceForMember(memberId: String): Balance? {
        return memberBalances.find { it.member.id == memberId }  // ✅ FIXED: it.member.id
    }

    /**
     * Get debts where this member owes someone
     *
     * @param memberId The member ID
     * @return List of debts this member needs to pay
     */
    fun getDebtsOwedByMember(memberId: String): List<SimplifiedDebt> {
        return simplifiedDebts.filter { it.fromMemberId == memberId }
    }

    /**
     * Get debts where someone owes this member
     *
     * @param memberId The member ID
     * @return List of debts owed to this member
     */
    fun getDebtsOwedToMember(memberId: String): List<SimplifiedDebt> {
        return simplifiedDebts.filter { it.toMemberId == memberId }
    }

    /**
     * Get total amount this member owes to others
     */
    fun getTotalOwedByMember(memberId: String): Double {
        return getDebtsOwedByMember(memberId).sumOf { it.amount }
    }

    /**
     * Get total amount owed to this member by others
     */
    fun getTotalOwedToMember(memberId: String): Double {
        return getDebtsOwedToMember(memberId).sumOf { it.amount }
    }
}