package com.example.splitify.domain.usecase.trip

import com.example.splitify.domain.model.Trip
import com.example.splitify.domain.repository.TripRepository
import com.example.splitify.util.Result
import javax.inject.Inject

class JoinTripUsingInviteCode @Inject constructor(
    private val tripRepository: TripRepository
) {
    suspend operator fun invoke(inviteCode: String): Result<Trip> {
        return tripRepository.joinTripWithInviteCode(inviteCode)

    }
}