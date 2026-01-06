package com.example.splitify.domain.usecase.trip

import com.example.splitify.domain.model.Trip
import com.example.splitify.domain.repository.TripRepository
import com.example.splitify.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTripUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {

    operator fun invoke(tripId: String):  Flow<Result<Trip>> {
        return tripRepository.observeTripById(tripId)
    }
}