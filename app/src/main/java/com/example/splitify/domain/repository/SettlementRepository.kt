package com.example.splitify.domain.repository

import com.example.splitify.domain.model.Settlement
import com.example.splitify.domain.model.SettlementStatus
import com.example.splitify.util.Result
import kotlinx.coroutines.flow.Flow

interface SettlementRepository {

    suspend fun createSettlement(
        tripId: String,
        fromMemberId: String,
        toMemberId: String,
        amount: Double,
        notes: String? = null
    ): Result<Settlement>

    suspend fun confirmSettlement(settlementId: String): Result<Settlement>

    fun getSettlementsForTrip(tripId: String): Flow<Result<List<Settlement>>>

    suspend fun cancelSettlement(
        settlementId: String,
        cancellingMemberId: String
    ): Result<Unit>

    fun getPendingSettlementsForMember(memberId: String): Flow<Result<List<Settlement>>>

    fun getPendingSettlementsBetweenMembers(fromMemberId: String, toMemberId: String): Flow<Result<List<Settlement>>>

    suspend fun updateSettlementStatus(settlementId: String, status: SettlementStatus): Result<Settlement>
}