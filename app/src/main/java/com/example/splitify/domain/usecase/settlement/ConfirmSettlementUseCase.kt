package com.example.splitify.domain.usecase.settlement

import com.example.splitify.domain.model.Settlement
import com.example.splitify.domain.repository.SettlementRepository
import com.example.splitify.util.Result
import javax.inject.Inject

class ConfirmSettlementUseCase @Inject constructor(
    private val settlementRepository: SettlementRepository
) {

    suspend operator fun invoke(
        settlementId: String,
        confirmingMemberId: String
    ): Result<Settlement> {

        return settlementRepository.confirmSettlement(settlementId)
    }

}