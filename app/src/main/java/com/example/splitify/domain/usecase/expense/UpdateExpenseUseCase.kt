package com.example.splitify.domain.usecase.expense

import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.model.ExpenseSplit
import com.example.splitify.domain.repository.ExpenseRepository
import javax.inject.Inject
import com.example.splitify.util.Result
import kotlin.math.abs
import kotlin.math.exp

class UpdateExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
){
    suspend operator fun invoke(
        expense: Expense,
        splits: List<ExpenseSplit>
    ): Result<Expense> {
        if(expense.amount <= 0){
            return Result.Error(Exception("Amount must be greater than zero"))
        }
        if (expense.description.isBlank()) {
            return Result.Error(Exception("Description cannot be empty"))
        }

        if (splits.isEmpty()) {
            return Result.Error(Exception("Expense must have at least one split"))
        }
        val totalSplitAmount = splits.sumOf { it.amountOwed }
        val difference = abs(expense.amount - totalSplitAmount)
        if(difference > 0.01){
            return Result.Error(Exception("Split amounts do not add up to the expense amount"))
        }

        return expenseRepository.updateExpenseWithSplits(expense, splits)
    }
}