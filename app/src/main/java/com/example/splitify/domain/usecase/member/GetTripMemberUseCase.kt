package com.example.splitify.domain.usecase.member

import com.example.splitify.domain.model.TripMember
import com.example.splitify.domain.repository.TripMemberRepository
import com.example.splitify.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTripMemberUseCase @Inject constructor(
    private val repository: TripMemberRepository
) {
    operator fun invoke(tripId: String): Flow<Result<List<TripMember>>> {
        return repository.getMembersForTrip(tripId)
    }
}