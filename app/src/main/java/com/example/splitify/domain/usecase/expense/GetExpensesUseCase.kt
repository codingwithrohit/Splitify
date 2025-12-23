package com.example.splitify.domain.usecase.expense

import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.repository.ExpenseRepository
import com.example.splitify.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetExpensesUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(tripId: String): Flow<Result<List<Expense>>>{
        return expenseRepository.getExpensesByTrip(tripId)
    }
}