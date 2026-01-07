package com.example.splitify.domain.usecase.settlement

import com.example.splitify.domain.model.Settlement
import com.example.splitify.domain.repository.SettlementRepository
import com.example.splitify.domain.repository.TripMemberRepository
import com.example.splitify.util.Result
import javax.inject.Inject

/**
 * Allow admin to confirm settlement on behalf of guest users
 */
class ConfirmSettlementOnBehalfUseCase @Inject constructor(
    private val settlementRepository: SettlementRepository,
    private val tripMemberRepository: TripMemberRepository
) {
    suspend operator fun invoke(
        settlementId: String,
        adminMemberId: String
    ): Result<Settlement> {
        // Verify admin is actually admin
        val member = tripMemberRepository.getMemberById(adminMemberId)

        if (member == null) {
            return Result.Error(Exception("Member not found"))
        }

        if (!member.isAdmin) {
            return Result.Error(Exception("Only trip admin can confirm on behalf of others"))
        }

        return settlementRepository.confirmSettlement(settlementId)
    }
}