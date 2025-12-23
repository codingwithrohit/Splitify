package com.example.splitify.domain.usecase.settlement

import com.example.splitify.domain.model.Settlement
import com.example.splitify.domain.repository.SettlementRepository
import com.example.splitify.util.Result
import javax.inject.Inject

class CreateSettlementUseCase @Inject constructor(
    private val settlementRepository: SettlementRepository
) {
    suspend operator fun invoke(
        tripId: String,
        fromMemberId: String,
        toMemberId: String,
        amount: Double,
        notes: String? = null
    ): Result<Settlement> {

        if(amount <= 0){
            return Result.Error(Exception("Amount must be greater than 0"))
        }

        if(fromMemberId == toMemberId){
            return Result.Error(Exception("From and to members cannot be the same"))
        }

        return settlementRepository.createSettlement(
            tripId = tripId,
            fromMemberId = fromMemberId,
            toMemberId = toMemberId,
            amount = amount,
            notes = notes
        )
    }

}
