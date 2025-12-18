package com.example.splitify.data.local.entity.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.splitify.data.local.entity.ExpenseEntity
import com.example.splitify.data.local.entity.ExpenseSplitEntity
import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.model.ExpenseSplit

data class ExpenseWithSplitsRelation(
    @Embedded
    val expense: ExpenseEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "expense_id"
    )
    val splits: List<ExpenseSplitEntity>

)

data class ExpenseWithSplits(
    val expense: Expense,
    val splits: List<ExpenseSplit>
)
{
    /**
     * Verify that splits sum to expense amount
     * Used for validation and debugging
     */
    fun isValid(): Boolean {
        val splitTotal = splits.sumOf { it.amountOwed }
        return kotlin.math.abs(splitTotal - expense.amount) < 0.01
    }

    /**
     * Get split for a specific member
     */
    fun getSplitForMember(memberId: String): ExpenseSplit? {
        return splits.find { it.memberId == memberId }
    }

    /**
     * Check if a member participated in this expense
     */
    fun hasMember(memberId: String): Boolean {
        return splits.any { it.memberId == memberId }
    }
}