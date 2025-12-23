package com.example.splitify.presentation.expense

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.model.TripMember
import com.example.splitify.domain.repository.ExpenseRepository
import com.example.splitify.domain.usecase.expense.DeleteExpenseUseCase
import com.example.splitify.domain.usecase.expense.GetExpensesUseCase
import com.example.splitify.domain.usecase.member.GetTripMemberUseCase
import com.example.splitify.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.exp
@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val getExpensesUseCase: GetExpensesUseCase,
    private val getTripMemberUseCase: GetTripMemberUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase
): ViewModel() {

    private val _uiState = MutableStateFlow<ExpenseUiState>(ExpenseUiState.Loading)
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    fun loadExpenses(tripId: String) {
        viewModelScope.launch {
            _uiState.value = ExpenseUiState.Loading

            try {
                combine(
                    getExpensesUseCase(tripId),
                    getTripMemberUseCase(tripId)
                ){
                    expensesResult, membersResult ->
                    Pair(expensesResult, membersResult)
                }.collect { (expensesResult, membersResult) ->
                    val state = when{
                        expensesResult is Result.Success && membersResult is Result.Success -> {
                            ExpenseUiState.Success(
                                expensesResult.data,
                                membersResult.data
                            )
                        }
                        expensesResult is Result.Error -> {
                            Log.e("ExpensesVM", "❌ Error loading expenses: ${expensesResult.message}")
                            ExpenseUiState.Error(expensesResult.message)
                        }
                        membersResult is Result.Error -> {
                            Log.e("ExpensesVM", "❌ Error loading members: ${membersResult.message}")
                            ExpenseUiState.Error(membersResult.message)
                        }
                        else -> ExpenseUiState.Loading
                    }
                    _uiState.value = state
                }
            }
            catch (e: Exception){
                _uiState.value = ExpenseUiState.Error("Failed to load expenses, ${e.message}")
            }

        }
    }

    fun deleteExpense(expenseId: String){
        viewModelScope.launch {
            when(val result = deleteExpenseUseCase(expenseId)){
                is Result.Error -> {
                    Log.e("ExpensesVM", "❌ Failed to delete: ${result.message}")
                    _uiState.value = ExpenseUiState.Error(result.message)
                }
                is Result.Success -> {
                    Log.d("ExpensesVM", "✅ Expense deleted successfully")
                }
                Result.Loading -> TODO()
            }
        }
    }
}