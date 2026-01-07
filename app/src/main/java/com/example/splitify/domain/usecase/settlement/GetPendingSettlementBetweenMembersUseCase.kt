package com.example.splitify.domain.usecase.settlement

import com.example.splitify.domain.model.Settlement
import com.example.splitify.domain.repository.SettlementRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import com.example.splitify.util.Result

class GetPendingSettlementBetweenMembersUseCase @Inject constructor(
    private val settlementRepository: SettlementRepository
) {
    suspend operator fun invoke(
        fromMemberId: String,
        toMemberId: String
    ): Result<Settlement?> {
        return try {
            val result = settlementRepository
                .getPendingSettlementsBetweenMembers(fromMemberId, toMemberId)
                .first()

            when (result) {
                is Result.Success -> {
                    // Return first pending settlement or null if none
                    Result.Success(result.data.firstOrNull())
                }
                is Result.Error -> result
                Result.Loading -> Result.Success(null)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}