package com.example.splitify.domain.usecase.trip

import com.example.splitify.domain.repository.TripRepository
import com.example.splitify.util.Result
import javax.inject.Inject

class JoinTripUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {
    suspend operator fun invoke(
        tripId: String,
        userId: String,
        displayName: String
    ): Result<Unit> {
        return tripRepository.joinTrip(tripId, userId, displayName)
    }
}