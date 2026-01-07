package com.example.splitify.domain.repository

import com.example.splitify.domain.model.MemberRole
import com.example.splitify.domain.model.TripMember
import com.example.splitify.util.Result
import kotlinx.coroutines.flow.Flow

interface TripMemberRepository {

    fun getMembersForTrip(tripId: String): Flow<Result<List<TripMember>>>

    suspend fun getMemberById(memberId: String): TripMember?

    suspend fun addMember(tripMember: TripMember): Result<Unit>

    suspend fun removeMember(tripId: String,memberId: String): Result<Unit>

    suspend fun updateMemberRole(tripId: String, memberId: String, newRole: String): Result<Unit>

    fun searchUsers(query: String): Flow<Result<List<TripMember>>>

}