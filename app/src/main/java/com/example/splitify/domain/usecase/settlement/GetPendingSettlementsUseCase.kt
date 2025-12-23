package com.example.splitify.domain.usecase.settlement

import com.example.splitify.domain.model.Settlement
import com.example.splitify.domain.repository.SettlementRepository
import com.example.splitify.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPendingSettlementsUseCase @Inject constructor(
    private val settlementRepository: SettlementRepository
) {
    operator fun invoke(memberId: String): Flow<Result<List<Settlement>>> {
        return settlementRepository.getPendingSettlementsForMember(memberId)
    }

}