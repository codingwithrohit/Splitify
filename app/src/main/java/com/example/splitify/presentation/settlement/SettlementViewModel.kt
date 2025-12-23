package com.example.splitify.presentation.settlement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitify.domain.model.Settlement
import com.example.splitify.domain.model.TripMember
import com.example.splitify.domain.usecase.member.GetTripMemberUseCase
import com.example.splitify.domain.usecase.settlement.ConfirmSettlementUseCase
import com.example.splitify.domain.usecase.settlement.CreateSettlementUseCase
import com.example.splitify.domain.usecase.settlement.GetSettlementsForTripUseCase
import com.example.splitify.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettlementViewModel @Inject constructor(
    private val createSettlementUseCase: CreateSettlementUseCase,
    private val confirmSettlementUseCase: ConfirmSettlementUseCase,
    private val getSettlementsForTripUseCase: GetSettlementsForTripUseCase,
    private val getTripMemberUseCase: GetTripMemberUseCase
): ViewModel() {
    private val _settlementState = MutableStateFlow<SettlementUiState>(SettlementUiState.Initial)
    val uiState: StateFlow<SettlementUiState> = _settlementState.asStateFlow()

    private val _settlementListState = MutableStateFlow<SettlementListUiState>(SettlementListUiState.Loading)
    val settlementListState: StateFlow<SettlementListUiState> = _settlementListState.asStateFlow()

    fun createSettlement(
        tripId: String,
        fromMemberId: String,
        toMemberId: String,
        amount: Double,
        notes: String? = null
    ){
        viewModelScope.launch {
            _settlementState.value = SettlementUiState.Loading

            val result = createSettlementUseCase(
                tripId = tripId,
                fromMemberId = fromMemberId,
                toMemberId = toMemberId,
                amount = amount,
                notes = notes
            )

            when(result){
                is Result.Success -> {
                    //Fetch member details for display
                    val members = getTripMemberUseCase(tripId).first()
                    when(members){
                        is Result.Success -> {
                            val fromMember = members.data.find { it.id == fromMemberId }
                            val toMember = members.data.find { it.id == toMemberId }
                            if(fromMember != null && toMember != null){
                                _settlementState.value = SettlementUiState.Success(
                                    settlement = result.data,
                                    fromMember = fromMember,
                                    toMember = toMember
                                )
                            }
                            else{
                                _settlementState.value = SettlementUiState.Error("Failed to fetch member details")
                            }
                        }
                        is Result.Error -> {
                            _settlementState.value = SettlementUiState.Error(members.message)
                        }
                        Result.Loading -> TODO()
                    }
                }

                is Result.Error -> {
                    _settlementState.value = SettlementUiState.Error(result.message)
                }
                Result.Loading -> TODO()
            }
        }
    }

    fun confirmSettlement(settlementId: String, confirmingMemberId: String){
        viewModelScope.launch {
            _settlementState.value = SettlementUiState.Loading

            val result = confirmSettlementUseCase(settlementId, confirmingMemberId)
            when(result){
                is Result.Success -> {
                    val settlement = result.data
                    val members = getTripMemberUseCase(settlement.tripId).first()

                    when (members) {
                        is Result.Success -> {
                            val fromMember = members.data.find { it.id == settlement.fromMemberId }
                            val toMember = members.data.find { it.id == settlement.toMemberId }

                            if (fromMember != null && toMember != null) {
                                _settlementState.value = SettlementUiState.Success(
                                    settlement = settlement,
                                    fromMember = fromMember,
                                    toMember = toMember
                                )
                            } else {
                                _settlementState.value = SettlementUiState.Error("Member not found")
                            }
                        }
                        is Result.Error -> {
                            _settlementState.value = SettlementUiState.Error(members.message)
                        }
                        is Result.Loading -> Unit
                    }
                }
                is Result.Error -> {
                    _settlementState.value = SettlementUiState.Error(result.message)
                }
                Result.Loading -> Unit

            }
        }
    }

    fun loadSettlements(tripId: String) {
        viewModelScope.launch {
            _settlementListState.value = SettlementListUiState.Loading

            try {
                combine(
                    getSettlementsForTripUseCase(tripId),
                    getTripMemberUseCase(tripId)
                ) { settlementsResult, membersResult ->
                    processSettlementResults(settlementsResult, membersResult)
                }.collect { state ->
                    _settlementListState.value = state
                }
            } catch (e: Exception) {
                _settlementListState.value = SettlementListUiState.Error(
                    "Failed to load settlements: ${e.message}"
                )
            }
        }
    }

    private fun processSettlementResults(
        settlementsResult: Result<List<Settlement>>,
        membersResult: Result<List<TripMember>>
    ): SettlementListUiState {
        return when {
            settlementsResult is Result.Success && membersResult is Result.Success -> {
                val settlements = settlementsResult.data
                val members = membersResult.data

                val settlementsWithMembers = settlements.mapNotNull { settlement ->
                    val fromMember = members.find { it.id == settlement.fromMemberId }
                    val toMember = members.find { it.id == settlement.toMemberId }

                    if (fromMember != null && toMember != null) {
                        SettlementWithMember(settlement, fromMember, toMember)
                    } else {
                        null
                    }
                }

                SettlementListUiState.Success(settlementsWithMembers)
            }

            settlementsResult is Result.Error -> {
                SettlementListUiState.Error(settlementsResult.message)
            }

            membersResult is Result.Error -> {
                SettlementListUiState.Error(membersResult.message)
            }

            else -> {
                SettlementListUiState.Loading
            }
        }
    }

    fun resetSettlementState() {
        _settlementState.value = SettlementUiState.Initial
    }

}