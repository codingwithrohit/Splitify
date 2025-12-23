package com.example.splitify.domain.usecase.expense

import com.example.splitify.domain.repository.ExpenseRepository
import com.example.splitify.util.Result
import javax.inject.Inject


class DeleteExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
){
    suspend operator fun invoke(
        expenseId: String
    ): Result<Unit> {
        return expenseRepository.deleteExpense(expenseId)
    }
}