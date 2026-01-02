package com.example.splitify.presentation.expense

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.model.TripMember
import com.example.splitify.domain.repository.AuthRepository
import com.example.splitify.domain.repository.ExpenseRepository
import com.example.splitify.domain.usecase.balance.CalculateTripBalancesUseCase
import com.example.splitify.domain.usecase.expense.DeleteExpenseUseCase
import com.example.splitify.domain.usecase.expense.GetExpensesUseCase
import com.example.splitify.domain.usecase.member.GetTripMemberUseCase
import com.example.splitify.presentation.balances.BalancesUiState
import com.example.splitify.presentation.balances.BalancesUiState.*
import com.example.splitify.util.ErrorMessages
import com.example.splitify.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.*
import kotlin.math.exp
@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val getExpensesUseCase: GetExpensesUseCase,
    private val getTripMembersUseCase: GetTripMemberUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val tripId: String = checkNotNull(savedStateHandle["tripId"])

    val uiState: StateFlow<ExpenseUiState> = combine(
        getExpensesUseCase(tripId),
        getTripMembersUseCase(tripId)
    ) { expensesResult, membersResult ->
        when {
            expensesResult is Result.Success && membersResult is Result.Success -> {
                Log.d("ExpensesVM", "✅ Loaded ${expensesResult.data.size} expenses")
                ExpenseUiState.Success(
                    expenses = expensesResult.data,
                    members = membersResult.data
                )
            }
            expensesResult is Result.Error -> {
                Log.e("ExpensesVM", "❌ Error: ${expensesResult.message}")
                ExpenseUiState.Error(expensesResult.message)
            }
            membersResult is Result.Error -> {
                Log.e("ExpensesVM", "❌ Error: ${membersResult.message}")
                ExpenseUiState.Error(membersResult.message)
            }
            else -> ExpenseUiState.Loading
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),  // ✅ Cache for 5 seconds
            initialValue = ExpenseUiState.Loading
        )
    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            Log.d("ExpensesVM", "🗑️ Deleting expense: $expenseId")

            when (val result = deleteExpenseUseCase(expenseId)) {
                is Result.Success -> {
                    Log.d("ExpensesVM", "✅ Expense deleted successfully")
                }
                is Result.Error -> {
                    Log.e("ExpensesVM", "❌ Failed to delete: ${result.message}")
                }
                Result.Loading -> {}
            }
        }
    }

}