package com.example.splitify.domain.usecase.member

import com.example.splitify.domain.repository.TripMemberRepository
import com.example.splitify.util.Result
import javax.inject.Inject

class RemoveTripMemberUseCase @Inject constructor(
    private val canRemoveMemberUseCase: CanRemoveMemberUseCase,
    private val removeMemberWithSplitsUpdateUseCase: RemoveMemberWithSplitsUpdateUseCase,
    private val tripMemberRepository: TripMemberRepository
) {
    suspend operator fun invoke(tripId: String, memberId: String): Result<Unit> {
        val member = tripMemberRepository.getMemberById(memberId)
        if (member == null) {
            return Result.Error(Exception("Member not found"))
        }

        // Validate if member can be removed
        val validation = canRemoveMemberUseCase(
            tripId = tripId,
            memberId = memberId,
            isAdmin = member.isAdmin,
        )

        return when (validation) {
            is Result.Success -> {
                if (!validation.data.canRemove) {
                    // Cannot remove - return friendly error
                    Result.Error(Exception(validation.data.reason ?: "Cannot remove member"))
                } else {
                    // Can remove - proceed with deletion and split updates
                    removeMemberWithSplitsUpdateUseCase(tripId, memberId)
                }
            }
            is Result.Error -> {
                Result.Error(validation.exception ?: Exception("Validation failed"))
            }
            else -> Result.Error(Exception("Validation failed"))
        }


    }
}