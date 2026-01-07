package com.example.splitify.domain.usecase.settlement

import com.example.splitify.domain.repository.SettlementRepository
import com.example.splitify.util.Result
import javax.inject.Inject


class CancelSettlementUseCase @Inject constructor(
    private val settlementRepository: SettlementRepository
) {
    suspend operator fun invoke(
        settlementId: String,
        cancellingMemberId: String
    ): Result<Unit> {
        return settlementRepository.cancelSettlement(settlementId, cancellingMemberId)
    }
}