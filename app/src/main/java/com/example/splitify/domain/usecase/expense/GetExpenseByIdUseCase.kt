package com.example.splitify.domain.usecase.expense

import com.example.splitify.data.local.entity.relations.ExpenseWithSplits
import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.model.ExpenseSplit
import com.example.splitify.domain.repository.ExpenseRepository
import com.example.splitify.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetExpenseByIdUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(expenseId: String): Flow<Result<ExpenseWithSplits>> {
        return expenseRepository.getExpenseWithSplitsById(expenseId)
    }
}