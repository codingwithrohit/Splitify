package com.example.splitify.presentation.expense

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitify.domain.model.Category
import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.model.ExpenseSplit
import com.example.splitify.domain.model.NotificationTemplates
import com.example.splitify.domain.repository.AuthRepository
import com.example.splitify.domain.usecase.expense.AddExpenseUseCase
import com.example.splitify.domain.usecase.expense.GetExpenseByIdUseCase
import com.example.splitify.domain.usecase.expense.UpdateExpenseUseCase
import com.example.splitify.domain.usecase.member.GetTripMemberUseCase
import com.example.splitify.presentation.navigation.Screen
import com.example.splitify.util.NotificationManager
import com.example.splitify.util.Result
import com.example.splitify.util.ValidationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val getTripMembersUseCase: GetTripMemberUseCase,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val updateExpenseUseCase: UpdateExpenseUseCase,
    private val getExpenseByIdUseCase: GetExpenseByIdUseCase,
    private val notificationManager: NotificationManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tripId: String = checkNotNull(
        savedStateHandle[Screen.AddExpense.ARG_TRIP_ID]
    )

    private val expenseId: String? =
        savedStateHandle[Screen.EditExpense.ARG_EXPENSE_ID]

    val mode: ExpenseFormMode =
        if (expenseId != null) ExpenseFormMode.Edit(expenseId)
        else ExpenseFormMode.Add

    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    private val _scrollToError = MutableStateFlow<ScrollTarget?>(null)
    val scrollToError: StateFlow<ScrollTarget?> = _scrollToError.asStateFlow()

    enum class ScrollTarget { AMOUNT, DESCRIPTION }

    private val _isGroupExpense = MutableStateFlow(false)
    val isGroupExpense: StateFlow<Boolean> = _isGroupExpense.asStateFlow()

    private val _selectedMemberIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedMemberIds: StateFlow<Set<String>> = _selectedMemberIds.asStateFlow()

    private var currentUserMemberId: String? = null

    private val currentUserFlow = authRepository.getCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        observeUserAndMembers()
        if (mode is ExpenseFormMode.Edit) {
            loadExpenseForEditing(mode.expenseId)
        }
    }

    private fun loadExpenseForEditing(expenseId: String) {
        viewModelScope.launch {
            getExpenseByIdUseCase(expenseId).collect { result ->
                when (result) {
                    is Result.Success -> {
                        val (expense, splits) = result.data
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
                        _selectedMemberIds.value =
                            splits.map { it.memberId }.toSet()
                    }

                    is Result.Error -> {
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
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeUserAndMembers() {
        viewModelScope.launch {
            currentUserFlow
                .filterNotNull()
                .flatMapLatest { user ->
                    getTripMembersUseCase(tripId).map { result -> user to result }
                }
                .collect { (user, result) ->
                    when (result) {
                        is Result.Success -> {
                            val members = result.data
                            val currentMember =
                                members.find { it.userId == user.id }

                            currentUserMemberId = currentMember?.id
                            val defaultPayer =
                                currentMember?.id ?: members.firstOrNull()?.id

                            _uiState.update {
                                it.copy(
                                    members = members,
                                    paidByMemberId =
                                        if (mode is ExpenseFormMode.Add)
                                            defaultPayer
                                        else it.paidByMemberId,
                                    isLoading = false
                                )
                            }

                            if (mode is ExpenseFormMode.Add) {
                                _selectedMemberIds.value =
                                    if (_isGroupExpense.value)
                                        members.map { it.id }.toSet()
                                    else setOfNotNull(currentMember?.id)
                            }
                        }

                        is Result.Error -> {
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

    fun onAmountChange(amount: String) {
        if (amount.isEmpty() ||
            amount.matches(Regex("^\\d*\\.?\\d{0,2}$"))
        ) {
            _uiState.update { it.copy(amount = amount, amountError = null) }

            if (amount.isNotBlank()) {
                val validation = ValidationUtils.validAmount(amount)
                if (!validation.isValid) {
                    _uiState.update {
                        it.copy(amountError = validation.errorMessage)
                    }
                }
            }
        }
    }

    fun onDescriptionChange(description: String) {
        if (description.length <= 100) {
            _uiState.update {
                it.copy(description = description, descriptionError = null)
            }
        }
    }

    fun onCategoryChange(category: Category) {
        _uiState.update { it.copy(category = category) }
    }

    fun onDateChange(date: LocalDate) {
        _uiState.update { it.copy(expenseDate = date) }
    }

    fun onPaidByChange(memberId: String) {
        _uiState.update { it.copy(paidByMemberId = memberId) }
    }

    fun saveExpense() {
        val state = uiState.value

        val amountValidation = ValidationUtils.validAmount(state.amount)
        if (!amountValidation.isValid) {
            _uiState.update { it.copy(amountError = amountValidation.errorMessage) }
            _scrollToError.value = ScrollTarget.AMOUNT
            return
        }

        val descriptionValidation =
            ValidationUtils.validateDescription(state.description)
        if (!descriptionValidation.isValid) {
            _uiState.update {
                it.copy(descriptionError = descriptionValidation.errorMessage)
            }
            _scrollToError.value = ScrollTarget.DESCRIPTION
            return
        }

        val paidByMemberId = state.paidByMemberId ?: return

        val participants =
            if (_isGroupExpense.value)
                _selectedMemberIds.value.toList()
            else listOf(paidByMemberId)

        if (_isGroupExpense.value && participants.size < 2) {
            _uiState.update {
                it.copy(amountError = "Select at least one other member")
            }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val currentUser =
                authRepository.getCurrentUser().filterNotNull().first()

            val result = when (mode) {

                is ExpenseFormMode.Add -> {
                    addExpenseUseCase(
                        tripId = tripId,
                        amount = state.amount.toDouble(),
                        description = state.description.trim(),
                        category = state.category,
                        date = state.expenseDate,
                        paidBy = paidByMemberId,
                        createdBy = currentUser.id,
                        isGroupExpense = _isGroupExpense.value,
                        participatingMemberIds = participants
                    )
                }

                is ExpenseFormMode.Edit -> {

                    val existing =
                        getExpenseByIdUseCase(mode.expenseId).first()

                    val expense = Expense(
                        id = mode.expenseId,
                        tripId = tripId,
                        amount = state.amount.toDouble(),
                        description = state.description.trim(),
                        category = state.category,
                        expenseDate = state.expenseDate,
                        paidBy = paidByMemberId,
                        createdBy = currentUser.id,
                        isGroupExpense = _isGroupExpense.value,
                        paidByName = state.members
                            .find { it.id == paidByMemberId }
                            ?.displayName ?: "",
                        createdAt =
                            if (existing is Result.Success)
                                existing.data.expense.createdAt
                            else System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )

                    val splitAmount =
                        expense.amount / participants.size

                    val splits = participants.map { id ->
                        ExpenseSplit(
                            id = UUID.randomUUID().toString(),
                            expenseId = mode.expenseId,
                            memberId = id,
                            amountOwed = splitAmount,
                            memberName = state.members
                                .find { it.id == id }
                                ?.displayName ?: ""
                        )
                    }

                    updateExpenseUseCase(expense, splits)
                }
            }

            when (result) {
                is Result.Success -> {
                    if (mode is ExpenseFormMode.Add) {
                        notificationManager.showNotification(
                            NotificationTemplates.expenseAdded(
                                state.amount.toDouble(),
                                state.description.trim()
                            )
                        )
                    } else {
                        notificationManager.showNotification(
                            NotificationTemplates.expenseUpdated(
                                state.description.trim()
                            )
                        )
                    }

                    _uiState.update {
                        it.copy(
                            isSaved = true,
                            isLoading = false
                        )
                    }
                }

                is Result.Error -> {
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

    fun resetScrollTarget() {
        _scrollToError.value = null
    }
}