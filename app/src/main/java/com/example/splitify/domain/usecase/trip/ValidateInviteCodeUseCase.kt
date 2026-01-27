package com.example.splitify.domain.usecase.trip

import com.example.splitify.domain.model.Trip
import com.example.splitify.domain.repository.TripRepository
import javax.inject.Inject
import com.example.splitify.util.Result

class ValidateInviteCodeUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {
    suspend operator fun invoke(code: String): Result<Trip> {
        if (code.isBlank()) {
            return Result.Error(exception = Exception(),"Invite code cannot be empty")
        }

        if (code.length != 8) {
            return Result.Error(Exception(),"Invite code must be 8 characters")
        }

        return tripRepository.validateInviteCode(code)
    }
}