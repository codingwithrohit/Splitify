
package com.example.splitify.domain.usecase.balance

import com.example.splitify.domain.model.Balance
import com.example.splitify.domain.model.SimplifiedDebt
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.min

/**
 * Simplify debts using a greedy matching algorithm
 *
 * Reduces the number of transactions needed to settle all balances
 * by matching largest creditors with largest debtors
 */
class SimplifyDebtsUseCase @Inject constructor() {

    operator fun invoke(balances: List<Balance>): List<SimplifiedDebt> {
        // Separate into creditors and debtors
        val creditors = balances.filter { it.isCreditor }.toMutableList()
        val debtors = balances.filter { it.isDebtor }.toMutableList()

        val simplifiedDebts = mutableListOf<SimplifiedDebt>()

        // Sort by absolute value (largest first)
        creditors.sortByDescending { it.netBalance }
        debtors.sortBy { it.netBalance }  // Most negative first

        var creditorIndex = 0
        var debtorIndex = 0

        while (creditorIndex < creditors.size && debtorIndex < debtors.size) {
            val creditor = creditors[creditorIndex]
            val debtor = debtors[debtorIndex]

            // Amount to settle is the minimum of what creditor is owed and debtor owes
            val amountToSettle = min(
                creditor.netBalance,
                abs(debtor.netBalance)
            )

            if (amountToSettle > 0.01) {  // Only add if amount is significant
                simplifiedDebts.add(
                    SimplifiedDebt(
                        fromMemberId = debtor.member.id,
                        toMemberId = creditor.member.id,
                        amount = amountToSettle
                    )
                )
            }

            // Update balances
            creditors[creditorIndex] = creditor.copy(
                netBalance = creditor.netBalance - amountToSettle
            )
            debtors[debtorIndex] = debtor.copy(
                netBalance = debtor.netBalance + amountToSettle
            )

            // Move to next creditor/debtor if fully settled
            if (abs(creditors[creditorIndex].netBalance) < 0.01) {
                creditorIndex++
            }
            if (abs(debtors[debtorIndex].netBalance) < 0.01) {
                debtorIndex++
            }
        }

        return simplifiedDebts
    }
}