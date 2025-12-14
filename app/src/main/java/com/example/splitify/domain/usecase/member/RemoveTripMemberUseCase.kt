package com.example.splitify.domain.usecase.member

import com.example.splitify.domain.repository.TripMemberRepository
import com.example.splitify.util.Result
import javax.inject.Inject

class RemoveTripMemberUseCase @Inject constructor(
    private val repository: TripMemberRepository
) {
    suspend operator fun invoke(tripId: String, memberId: String): Result<Unit> {
        return repository.removeMember(tripId, memberId)
    }
}