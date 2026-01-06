package com.example.splitify.presentation.tripdetail

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.model.SettlementStatus
import com.example.splitify.domain.model.Trip
import com.example.splitify.domain.model.TripMember
import com.example.splitify.domain.repository.AuthRepository
import com.example.splitify.domain.usecase.balance.CalculateTripBalancesUseCase
import com.example.splitify.domain.usecase.expense.GetExpensesUseCase
import com.example.splitify.domain.usecase.member.GetTripMemberUseCase
import com.example.splitify.domain.usecase.settlement.GetSettlementsForTripUseCase
import com.example.splitify.domain.usecase.trip.GetTripUseCase
import com.example.splitify.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TripDetailViewModel @Inject constructor(
    private val getTripUseCase: GetTripUseCase,
    private val getExpensesUseCase: GetExpensesUseCase,
    private val getTripMemberUseCase: GetTripMemberUseCase,
    private val calculateTripBalancesUseCase: CalculateTripBalancesUseCase,
    private val settlementsForTripUseCase: GetSettlementsForTripUseCase,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tripId: String = checkNotNull(savedStateHandle["tripId"])

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    val dashboardState: StateFlow<TripDashboardState> =
        combine(
            getTripUseCase(tripId),
            getExpensesUseCase(tripId),
            getTripMemberUseCase(tripId),
            calculateTripBalancesUseCase(tripId),
            settlementsForTripUseCase(tripId)
        ) { tripResult,
            expensesResult,
            membersResult,
            balancesResult,
            settlementsResult ->

            when {
                tripResult is Result.Error ->
                    TripDashboardState.Error(tripResult.message)

                expensesResult is Result.Error ->
                    TripDashboardState.Error(expensesResult.message)

                membersResult is Result.Error ->
                    TripDashboardState.Error(membersResult.message)

                balancesResult is Result.Error ->
                    TripDashboardState.Error(balancesResult.message)

                settlementsResult is Result.Error ->
                    TripDashboardState.Error(settlementsResult.message)

                tripResult is Result.Success &&
                        expensesResult is Result.Success &&
                        membersResult is Result.Success &&
                        balancesResult is Result.Success &&
                        settlementsResult is Result.Success -> {

                    val trip = tripResult.data
                    val expenses = expensesResult.data
                    val members = membersResult.data
                    val balances = balancesResult.data
                    val settlements = settlementsResult.data

                    val currentUserId =
                        authRepository.getCurrentUser().firstOrNull()?.id

                    val currentMember =
                        members.firstOrNull { it.userId == currentUserId }

                    val currentBalance =
                        balances.memberBalances
                            .firstOrNull { it.member.id == currentMember?.id }

                    val youOwe =
                        currentBalance?.netBalance
                            ?.takeIf { it < 0 }
                            ?.let { -it } ?: 0.0

                    val youAreOwed =
                        currentBalance?.netBalance
                            ?.takeIf { it > 0 } ?: 0.0

                    TripDashboardState.Success(
                        trip = trip,
                        currentUserId = currentUserId,
                        members = members,
                        expenseCount = expenses.size,
                        totalAmount = expenses.sumOf { it.amount },
                        recentExpenses = expenses
                            .sortedByDescending { it.expenseDate }
                            .take(3),
                        youOwe = youOwe,
                        youAreOwed = youAreOwed,
                        pendingSettlements = settlements.count {
                            it.status == SettlementStatus.PENDING
                        },
                        completedSettlements = settlements.count {
                            it.status == SettlementStatus.CONFIRMED
                        }
                    )
                }

                else -> TripDashboardState.Loading
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TripDashboardState.Loading
        )

}


sealed interface TripDashboardState{
    object Loading: TripDashboardState
    data class Success(
        val trip: Trip,
        val currentUserId: String?,
        val members: List<TripMember>,
        val expenseCount: Int,
        val totalAmount: Double,
        val recentExpenses: List<Expense>,
        val youOwe: Double,
        val youAreOwed: Double,
        val pendingSettlements: Int,
        val completedSettlements: Int
    ): TripDashboardState

    data class Error(val message: String): TripDashboardState
}