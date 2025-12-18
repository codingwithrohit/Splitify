package com.example.splitify.domain.usecase.trip

import com.example.splitify.domain.model.MemberRole
import com.example.splitify.domain.model.Trip
import com.example.splitify.domain.model.TripMember
import com.example.splitify.domain.repository.AuthRepository
import com.example.splitify.domain.repository.TripMemberRepository
import com.example.splitify.domain.repository.TripRepository
import com.example.splitify.util.Result
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class CreateTripUseCase @Inject constructor(
    private val tripRepository: TripRepository,
    private val tripMemberRepository: TripMemberRepository,
    private val authRepository: AuthRepository,
){
    suspend operator fun invoke(
        name: String,
        description: String,
        startDate: LocalDate,
        endDate: LocalDate?,
        inviteCode: String
    ): Result<Unit>
    {
        val user = authRepository.getCurrentUser().first() ?: return Result.Error(Exception("User not logged in"))

        val tripId = UUID.randomUUID().toString()

        val trip = Trip(
            id = tripId,
            name = name,
            description = description,
            startDate = startDate,
            endDate = endDate,
            inviteCode = inviteCode,
            createdBy = user.id,
            isLocal = true
        )
        tripRepository.createTrip(trip)

        tripMemberRepository.addMember(
            TripMember(
                id = UUID.randomUUID().toString(),
                tripId = tripId,
                userId = user.id,
                displayName = user.userName,
                role = MemberRole.ADMIN,
                joinedAt = System.currentTimeMillis()
            )
        ).getOrThrow()

        return Result.Success(Unit)

    }
}