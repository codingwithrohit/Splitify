package com.example.splitify.presentation.expense

import androidx.compose.runtime.getValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.splitify.domain.model.Category
import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.repository.AuthRepository
import com.example.splitify.domain.repository.ExpenseRepository
import com.example.splitify.presentation.navigation.Screen
import com.example.splitify.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    private val tripId: String = checkNotNull(
        savedStateHandle[Screen.AddExpense.ARG_TRIP_ID]
    )
    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    private var currentUser: Pair<String, String>? = null //(id,name)

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser(){
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { user ->
                if(user != null){
                    currentUser = user.id to (user.fullName ?: user.userName)
                    println("👤 Current user: ${user.userName}")
                }
            }
        }
    }

    fun onAmountChange(amount: String){                       //"^\\d*\\.?\\d{0,2}$"
        if(amount.isEmpty() || amount.matches(Regex("^[0-9]+(\\.[0-9]{1,2})?$"))){
            _uiState.update {
                it.copy(
                    amount = amount,
                    amountError = null
                )
            }
        }
    }

    fun onDescriptionChange(description: String){
        _uiState.update {
            it.copy(
                description = description,
                descriptionError = null
            )
        }
    }

    fun onCategoryChange(category: Category){
        _uiState.update {
            it.copy(
                category = category
            )
        }
    }

    fun onDateChange(date: LocalDate) {
        _uiState.update {
            it.copy(
                expenseDate = date
            )
        }
    }

    fun onSplitTypeChange(isGroup: Boolean) {
        _uiState.update { it.copy(isGroupExpense = isGroup) }
    }

    fun saveExpense(){
        val state = uiState.value
        //Validations
        val amountValue = state.amount.toDoubleOrNull()
        if(amountValue == null || amountValue <= 0){
            _uiState.update {
                it.copy(
                    amountError = "Enter a valid amount"
                )
            }
            return
        }

        if(state.description.isBlank()){
            _uiState.update {
                it.copy(
                    descriptionError = "Description is required"
                )
            }
            return
        }

        val user = currentUser
        if (user == null) {
            _uiState.update { it.copy(amountError = "You must be logged in") }
            return
        }

        _uiState.update { it.copy( isLoading = true ) }
        viewModelScope.launch {
            val expense = Expense(
                id = UUID.randomUUID().toString(),
                tripId = tripId,
                amount = amountValue,
                description = state.description.trim(),
                category = state.category,
                expenseDate = state.expenseDate,
                paidBy = user.first,
                paidByName = user.second,
                isGroupExpense = state.isGroupExpense
            )

            when( val result = expenseRepository.addExpense(expense) ){
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            amountError = result.message,
                            isLoading = false
                        )
                    }
                }
                is Result.Success<*> -> {
                    _uiState.update {
                        it.copy(
                            isSaved = true,
                            isLoading = false
                        )
                    }
                }
            }

        }

    }
}