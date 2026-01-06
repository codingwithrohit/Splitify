package com.example.splitify.presentation.expense

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.splitify.domain.model.Category
import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.model.ExpenseSplit
import com.example.splitify.domain.repository.AuthRepository
import com.example.splitify.domain.repository.ExpenseRepository
import com.example.splitify.domain.usecase.expense.AddExpenseUseCase
import com.example.splitify.domain.usecase.expense.DeleteExpenseUseCase
import com.example.splitify.domain.usecase.expense.GetExpenseByIdUseCase
import com.example.splitify.domain.usecase.expense.UpdateExpenseUseCase
import com.example.splitify.domain.usecase.member.GetTripMemberUseCase
import com.example.splitify.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import com.example.splitify.util.Result
import com.example.splitify.util.ValidationResult
import com.example.splitify.util.ValidationUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.ZoneId

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val getTripMembersUseCase: GetTripMemberUseCase,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val updateExpenseUseCase: UpdateExpenseUseCase,
    private val getExpenseByIdUseCase: GetExpenseByIdUseCase,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    private val tripId: String = checkNotNull(
        savedStateHandle[Screen.AddExpense.ARG_TRIP_ID]
    )

    private val expenseId: String? =
        savedStateHandle[Screen.EditExpense.ARG_EXPENSE_ID]


    val mode: ExpenseFormMode = if (expenseId != null) {
        ExpenseFormMode.Edit(expenseId)
    } else {
        ExpenseFormMode.Add
    }

    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    private val _isGroupExpense = MutableStateFlow(false)
    val isGroupExpense: StateFlow<Boolean> = _isGroupExpense.asStateFlow()

    private val _selectedMemberIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedMemberIds: StateFlow<Set<String>> = _selectedMemberIds.asStateFlow()

    private var currentUserMemberId: String? = null

    private val currentUserFlow = authRepository.getCurrentUser()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    init {
        observeUserAndMembers()
        if (mode is ExpenseFormMode.Edit) {
            loadExpenseForEditing(mode.expenseId)
        }
    }

    private fun loadExpenseForEditing(expenseId: String){
        viewModelScope.launch {
            getExpenseByIdUseCase(expenseId).collect { result ->
                when(result){
                    is Result.Error -> {
                        Log.e("AddExpenseVM", "❌ Failed to load expense: ${result.message}")
                        _uiState.update {
                            it.copy(
                                amountError = result.message,
                                isLoading = false
                            )
                        }
                    }
                    Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Result.Success -> {
                        val (expense, splits) = result.data
                        Log.d("AddExpenseVM", "✅ Loaded expense: ${expense.description}")
                        Log.d("AddExpenseVM", "  Amount: ₹${expense.amount}")
                        Log.d("AddExpenseVM", "  Paid by: ${expense.paidBy}")
                        Log.d("AddExpenseVM", "  Is group: ${expense.isGroupExpense}")
                        Log.d("AddExpenseVM", "  Splits: ${splits.size}")
                        _uiState.update {
                            it.copy(
                                amount = expense.amount.toString(),
                                description = expense.description,
                                category = expense.category,
                                expenseDate = expense.expenseDate,
                                paidByMemberId = expense.paidBy,
                                isLoading = false
                            )
                        }

                        _isGroupExpense.value = expense.isGroupExpense
                        val participantIds = splits.map { it.memberId }.toSet()
                        _selectedMemberIds.value = participantIds
                        Log.d("AddExpenseVM", "✅ Form pre-filled with existing data")
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeUserAndMembers() {
        viewModelScope.launch {
            currentUserFlow
                .filterNotNull() // ⬅ guarantees user exists
                .flatMapLatest { user ->
                    getTripMembersUseCase(tripId)
                        .map { result -> user to result }
                }
                .collect { (user, result) ->
                    when (result) {

                        is Result.Success -> {
                            val members = result.data

                            Log.d("AddExpenseVM", "════════════════════════════════")
                            Log.d("AddExpenseVM", "📦 MEMBERS LOADED")
                            Log.d("AddExpenseVM", "  Trip ID: $tripId")
                            Log.d("AddExpenseVM", "  Total Members: ${members.size}")

                            if (members.isEmpty()) {
                                Log.e("AddExpenseVM", "❌ NO MEMBERS FOUND!")
                                Log.e("AddExpenseVM", "  This trip has no members!")
                                Log.e("AddExpenseVM", "════════════════════════════════")
                                _uiState.update {
                                    it.copy(
                                        amountError = "No members in this trip",
                                        isLoading = false
                                    )
                                }
                                return@collect
                            }

                            val currentMember = members.find {
                                it.userId == user.id
                            }

                            currentUserMemberId = currentMember?.id

                            val defaultPayer = currentMember?.id ?: members.firstOrNull()?.id

                            Log.d("AddExpenseVM", "📦 Loaded ${members.size} members")
                            Log.d("AddExpenseVM", "👤 Current user: ${user.id}")
                            Log.d("AddExpenseVM", "👤 MemberId: $currentUserMemberId")
                            Log.d("AddExpenseVM", "  Default Payer: $defaultPayer")
                            Log.d("AddExpenseVM", "  Mode: ${if (mode is ExpenseFormMode.Add) "ADD" else "EDIT"}")
                            Log.d("AddExpenseVM", "════════════════════════════════")

                            _uiState.update {
                                it.copy(
                                    members = members,
                                    paidByMemberId = if (mode is ExpenseFormMode.Add) {
                                        defaultPayer
                                    } else {
                                        it.paidByMemberId
                                    },
                                    isLoading = false
                                )
                            }

                            if(mode is ExpenseFormMode.Add){
                                _selectedMemberIds.value =
                                    if (_isGroupExpense.value) {
                                        members.map { it.id }.toSet()
                                    } else {
                                        setOfNotNull(currentMember?.id)
                                    }
                            }

                        }

                        is Result.Error -> {
                            Log.e("AddExpenseVM", "════════════════════════════════")
                            Log.e("AddExpenseVM", "❌ ERROR loading members: ${result.message}")
                            Log.e("AddExpenseVM", "════════════════════════════════")
                            Log.e("AddExpenseVM", "❌ Error loading members: ${result.message}")
                            _uiState.update {
                                it.copy(
                                    amountError = result.message,
                                    isLoading = false
                                )
                            }
                        }

                        else -> Unit
                    }
                }
        }
    }



    fun setIsGroupExpense(isGroup: Boolean) {
        _isGroupExpense.value = isGroup

        val members = _uiState.value.members

        _selectedMemberIds.value =
            if (isGroup) {
                members.map { it.id }.toSet()
            } else {
                currentUserMemberId?.let { setOf(it) }.orEmpty()
            }

        Log.d("AddExpenseVM", "🔄 Split type changed to: ${if (isGroup) "Group" else "Personal"}")


    }

    fun toggleMember(memberId: String) {
        _selectedMemberIds.value = if (memberId in _selectedMemberIds.value) {
            _selectedMemberIds.value - memberId
        } else {
            _selectedMemberIds.value + memberId
        }
        Log.d("AddExpenseVM", "✓ Selected members: ${_selectedMemberIds.value.size}")
    }

    fun onAmountChange(amount: String) {
        // Allow empty, digits, and one decimal point with up to 2 decimal places
        if (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            _uiState.update {
                it.copy(
                    amount = amount,
                    amountError = null
                )
            }
            if(amount.isNotBlank()){
                val validation = ValidationUtils.validAmount(amount)
                if(!validation.isValid){
                    _uiState.update {
                        it.copy(
                            amountError = validation.errorMessage
                        )
                    }
                }
            }
        }
    }

    fun onDescriptionChange(description: String) {
        if (description.length <= 100) {
            _uiState.update {
                it.copy(
                    description = description,
                    descriptionError = null
                )
            }
        }
    }

    fun onCategoryChange(category: Category) {
        _uiState.update {
            it.copy(category = category)
        }
    }

    fun onDateChange(date: LocalDate) {
        _uiState.update {
            it.copy(expenseDate = date)
        }
    }

    fun onPaidByChange(memberId: String){
        _uiState.update {
            it.copy(
                paidByMemberId = memberId
            )
        }
        Log.d("AddExpenseVM", "💰 Payer changed to member: $memberId")
    }

    fun saveExpense() {
        val state = uiState.value
        var hasError = false
        val amountValue = state.amount.toDouble()
        // Validations
        val amountValidation = ValidationUtils.validAmount(state.amount)
        if(!amountValidation.isValid){
            _uiState.update {
                it.copy(amountError = amountValidation.errorMessage)
            }
            hasError = true
        }

        val descriptionValidation = ValidationUtils.validateDescription(state.description)
        if (!descriptionValidation.isValid) {
            _uiState.update {
                it.copy(descriptionError = descriptionValidation.errorMessage)
            }
            hasError = true
        }

        val paidByMemberId = state.paidByMemberId
        if (paidByMemberId == null) {
            _uiState.update {
                it.copy(amountError = "Select who paid for this expense")
            }
            Log.e("AddExpenseVM", "❌ Current user is not a member of this trip!")
            hasError = true
        }

        val participants =
            if (_isGroupExpense.value) {
                _selectedMemberIds.value.toList()
            } else {
                listOf(paidByMemberId ?: "")
            }

        if(participants.isEmpty()){
            _uiState.update {
                it.copy(amountError = "Select at least one participant")
            }
            hasError = true
        }

        Log.d("AddExpenseVM", "💾 Saving expense with ${participants.size} participants")


        Log.d("AddExpenseVM", "💾 Saving expense:")
        Log.d("AddExpenseVM", "  Amount: ₹$amountValidation")
        Log.d("AddExpenseVM", "  Description: ${state.description}")
        Log.d("AddExpenseVM", "  Paid by (memberId): $paidByMemberId")
        Log.d("AddExpenseVM", "  Is group: ${_isGroupExpense.value}")
        Log.d("AddExpenseVM", "  Participants (${participants.size}): $participants")


        if (hasError) {
            Log.e("AddExpenseVM", "❌ Validation failed")
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val currentUserId = authRepository.getCurrentUser().firstOrNull()?.id

            if (currentUserId == null) {
                _uiState.update {
                    it.copy(
                        amountError = "User not authenticated",
                        isLoading = false
                    )
                }
                return@launch
            }
            val result = when(mode){
                is ExpenseFormMode.Add -> {
                    addExpenseUseCase(
                        tripId = tripId,
                        amount = state.amount.toDouble(),
                        description = state.description.trim(),
                        category = state.category,
                        date = state.expenseDate, // Convert to millis
                        paidBy = paidByMemberId.toString(),
                        createdBy = currentUserId,
                        isGroupExpense = _isGroupExpense.value,
                        participatingMemberIds = participants
                    )
                }
                is ExpenseFormMode.Edit -> {
                    val expenseIdToUpdate = mode.expenseId

                    val expense = Expense(
                        id = expenseIdToUpdate,
                        tripId = tripId,
                        amount = state.amount.toDouble(),
                        description = state.description.trim(),
                        category = state.category,
                        expenseDate = state.expenseDate,
                        paidBy = paidByMemberId.toString(),
                        createdBy = currentUserId,
                        isGroupExpense = _isGroupExpense.value,
                        paidByName = state.paidByMemberId.toString(),
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )

                    val splitAmount = amountValue / participants.size
                    val splits = participants.map { participantId ->
                        ExpenseSplit(
                            id = UUID.randomUUID().toString(),
                            expenseId = expenseIdToUpdate,
                            memberId = participantId,
                            amountOwed = splitAmount,
                            memberName = state.paidByMemberId.toString()
                        )
                    }

                    updateExpenseUseCase(expense, splits)
                }
            }


            when (result) {
                is Result.Error -> {
                    Log.e("AddExpenseVM", "❌ Failed to save: ${result.message}")
                    _uiState.update {
                        it.copy(
                            amountError = result.message,
                            isLoading = false
                        )
                    }
                }
                is Result.Success -> {
                    Log.d("AddExpenseVM", "✅ Expense saved successfully!")
                    _uiState.update {
                        it.copy(
                            isSaved = true,
                            isLoading = false
                        )
                    }
                    // Emit navigation event

                }
                Result.Loading -> {
                    // Already handling loading state
                }
            }
        }
    }
}