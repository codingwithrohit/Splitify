package com.example.splitify.domain.usecase.balance

import com.example.splitify.domain.model.MemberRole
import com.example.splitify.domain.model.TripMember
import com.example.splitify.domain.repository.ExpenseRepository
import com.example.splitify.domain.repository.TripMemberRepository

class CalculateTripBalancesUseCaseTest {

    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var tripMemberRepository: TripMemberRepository
    private lateinit var simplifyDebtsUseCase: SimplifyDebtsUseCase
    private lateinit var calculateTripBalancesUseCase: CalculateTripBalancesUseCase

    private val tripId = "test-trip-1"

    private val alice = TripMember(
        id = "member-alice",
        tripId = tripId,
        userId = "user-alice",
        displayName = "Alice",
        role = MemberRole.ADMIN,
        joinedAt = 0L
    )
    private val bob = TripMember(
        id = "member-bob",
        tripId = tripId,
        userId = "user-bob",
        displayName = "Bob",
        role = MemberRole.MEMBER,
        joinedAt = 0L
    )

    private val charlie = TripMember(
        id = "member-charlie",
        tripId = tripId,
        userId = "user-charlie",
        displayName = "Charlie",
        role = MemberRole.MEMBER,
        joinedAt = 0L
    )


}
