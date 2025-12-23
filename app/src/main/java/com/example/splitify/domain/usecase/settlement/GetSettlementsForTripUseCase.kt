package com.example.splitify.domain.usecase.settlement

import com.example.splitify.domain.model.Settlement
import com.example.splitify.domain.repository.SettlementRepository
import kotlinx.coroutines.flow.Flow
import com.example.splitify.util.Result
import javax.inject.Inject

class GetSettlementsForTripUseCase @Inject constructor(
    private val settlementRepository: SettlementRepository
) {
    operator fun invoke(tripId: String): Flow<Result<List<Settlement>>> {
        return settlementRepository.getSettlementsForTrip(tripId)
    }
}

