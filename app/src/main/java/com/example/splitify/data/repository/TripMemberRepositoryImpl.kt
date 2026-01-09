package com.example.splitify.data.repository

import com.example.splitify.data.local.dao.TripMemberDao
import com.example.splitify.data.local.entity.TripMemberEntity
import com.example.splitify.data.local.toDomain
import com.example.splitify.data.local.toEntity
import com.example.splitify.domain.model.TripMember
import com.example.splitify.domain.repository.TripMemberRepository
import com.example.splitify.util.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TripMemberRepositoryImpl @Inject constructor(
    private val tripMemberDao: TripMemberDao
) : TripMemberRepository {

    override fun getMembersForTrip(tripId: String): Flow<Result<List<TripMember>>> {
        return tripMemberDao
            .getAllMembersForTrip(tripId)
            .map<List<TripMemberEntity>, Result<List<TripMember>>> { entities ->
                Result.Success(entities.map { it.toDomain() })
            }
            .catch { e ->
                if (e is CancellationException) {
                    throw e
                } else {
                    emit(Result.Error(e))
                }
            }
    }


    override fun searchUsers(query: String): Flow<Result<List<TripMember>>> {
        return tripMemberDao
            .searchUsers(query)
            .map<List<TripMemberEntity>, Result<List<TripMember>>> { entities ->
                Result.Success(entities.map { it.toDomain() })
            }
            .catch { e ->
                if (e is CancellationException) {
                    throw e
                } else {
                    emit(Result.Error(e))
                }
            }
    }


    override suspend fun getMemberById(memberId: String): TripMember? {
        return try {
            tripMemberDao.getMemberById(memberId)?.toDomain()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun addMember(tripMember: TripMember): Result<Unit> {
        return try {
            val existingMember = tripMemberDao.getMemberById(tripMember.id)
            if (existingMember != null) {
                Result.Error(Exception("Member already exists"))
            } else {
                tripMemberDao.insertMember(tripMember.toEntity())
                Result.Success(Unit)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun removeMember(
        tripId: String,
        memberId: String
    ): Result<Unit> {
        return try {
            val member = tripMemberDao.getMemberById(memberId)
                ?: return Result.Error(Exception("Member not found"))

            if (member.tripId != tripId) {
                return Result.Error(Exception("Member does not belong to this trip"))
            }

            tripMemberDao.deleteMember(memberId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateMemberRole(
        tripId: String,
        memberId: String,
        newRole: String
    ): Result<Unit> {
        return try {
            val member = tripMemberDao.getMemberById(memberId)
                ?: return Result.Error(Exception("Member not found"))

            val updated = member.copy(
                role = newRole,
                lastModified = System.currentTimeMillis(),
                isSynced = false
            )
            tripMemberDao.updateMember(updated)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
