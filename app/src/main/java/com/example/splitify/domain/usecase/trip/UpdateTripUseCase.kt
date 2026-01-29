package com.example.splitify.domain.usecase.trip

import com.example.splitify.domain.model.Trip
import com.example.splitify.domain.repository.AuthRepository
import com.example.splitify.domain.repository.TripRepository
import com.example.splitify.util.Result
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

class UpdateTripUseCase @Inject constructor(
    private val tripRepository: TripRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        tripId: String,
        name: String,
        description: String,
        startDate: LocalDate,
        endDate: LocalDate?,
        inviteCode: String
    ): Result<Trip> {

        val user = authRepository.getCurrentUser().first()
            ?: return Result.Error(Exception("User not logged in"))

        // Fetch existing trip
        val existingTrip = tripRepository.getTripById(tripId)
            ?: return Result.Error(Exception("Trip not found"))

        // Optional but IMPORTANT: Authorization check
        if (existingTrip.createdBy != user.id) {
            return Result.Error(Exception("You are not allowed to update this trip"))
        }

        val updatedTrip = existingTrip.copy(
            name = name,
            description = description,
            startDate = startDate,
            endDate = endDate,
            inviteCode = inviteCode,
            isLocal = true
        )

        tripRepository.updateTrip(updatedTrip)

        return Result.Success(updatedTrip)
    }
}
