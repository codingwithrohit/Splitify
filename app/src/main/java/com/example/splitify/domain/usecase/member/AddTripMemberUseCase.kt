package com.example.splitify.domain.usecase.member

import com.example.splitify.domain.model.MemberRole
import com.example.splitify.domain.model.TripMember
import com.example.splitify.domain.repository.TripMemberRepository
import com.example.splitify.util.Result
import java.util.UUID
import javax.inject.Inject

class AddTripMemberUseCase @Inject constructor(
    private val repository: TripMemberRepository
) {
    suspend operator fun invoke(
        tripId: String,
        displayName: String,
        userId: String? = null,
        role: MemberRole = MemberRole.MEMBER
    ): Result<Unit> {

        if(displayName.isBlank()){
            return Result.Error(exception = Exception("Display name cannot be empty"))
        }

        if(displayName.length < 2){
            return Result.Error(Exception("Name must be at least 2 characters"))
        }

        val member = TripMember(
            id = UUID.randomUUID().toString(),
            tripId = tripId,
            userId = userId,
            displayName = displayName,
            role = role,
            joinedAt = System.currentTimeMillis()
        )

        return repository.addMember(member)

    }
}