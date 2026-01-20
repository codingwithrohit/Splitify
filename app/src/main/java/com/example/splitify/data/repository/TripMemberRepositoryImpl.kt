package com.example.splitify.data.repository

import android.util.Log
import com.example.splitify.data.local.SessionManager
import com.example.splitify.data.local.dao.TripMemberDao
import com.example.splitify.data.local.entity.TripMemberEntity
import com.example.splitify.data.local.toDomain
import com.example.splitify.data.local.toEntity
import com.example.splitify.data.remote.dto.TripMemberDto
import com.example.splitify.data.remote.toDto
import com.example.splitify.data.sync.SyncManager
import com.example.splitify.domain.model.TripMember
import com.example.splitify.domain.repository.TripMemberRepository
import com.example.splitify.util.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TripMemberRepositoryImpl @Inject constructor(
    private val tripMemberDao: TripMemberDao,
    private val supabase: SupabaseClient,
    private val sessionManager: SessionManager
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

            Log.d("TripMemberRepo", "📝 Adding member: ${tripMember.displayName}")

            val existingMember = tripMemberDao.getMemberById(tripMember.id)
            if (existingMember != null) {
                Result.Error(Exception("Member already exists"))
            }

            //1. Save to room
            val entity = tripMember.toEntity().copy(
                isSynced = false,
                lastModified = System.currentTimeMillis()
            )

            tripMemberDao.insertMember(entity)
            Log.d("TripMemberRepo", "✅ Saved to Room")

            //2. Try to sync to supabase
            try {
                val session = supabase.auth.currentSessionOrNull()
                if(session == null){
                    Log.w("TripMemberRepo", "⚠️ No session, skipping sync")
                    return Result.Success(Unit)
                }

                Log.d("TripMemberRepo", "📤 Syncing to Supabase...")
                val dto = entity.toDto()
                supabase.from("trip_members").insert(dto)

                //3. Mark as synced
                tripMemberDao.updateMember(entity.copy(isSynced = true))
                Log.d("TripMemberRepo", "✅ Synced to Supabase")
            }catch (e: Exception){
                Log.e("TripMemberRepo", "⚠️ Sync failed: ${e.message}")
            }

            Result.Success(Unit)
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

            //1. Delete from Room
            Log.d("TripMemberRepo", "📝 Deleting member: ${member.displayName}")
            tripMemberDao.deleteMember(memberId)
            Log.d("TripMemberRepo", "✅ Deleted from Room")

            //2. Try to delete from Supabase
            try {
                val session = supabase.auth.currentSessionOrNull()
                if(session == null){
                    Log.w("TripMemberRepo", "⚠️ No session, skipping sync")
                    return Result.Success(Unit)
                }
                Log.d("TripMemberRepo", "📤 Syncing to Supabase...")
                supabase.from("trip_members").delete {
                    filter {  eq("id", memberId)}
                }
            }catch (syncError: Exception){
                Log.e("TripMemberRepo", "⚠️ Sync failed: ${syncError.message}")
            }

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
            Log.d("TripMemberRepo", "✏️ Updating member role: $memberId → $newRole")
            val member = tripMemberDao.getMemberById(memberId)
                ?: return Result.Error(Exception("Member not found"))

            //1. Update in room
            val updated = member.copy(
                role = newRole,
                lastModified = System.currentTimeMillis(),
                isSynced = false
            )
            tripMemberDao.updateMember(updated)
            Log.d("TripMemberRepo", "✅ Updated in Room")

            //2. Try to update in Supabase
            try {
                val session = supabase.auth.currentSessionOrNull()
                if(session == null){
                    Log.w("TripMemberRepo", "⚠️ No session, skipping sync")
                    return Result.Success(Unit)
                }

                Log.d("TripMemberRepo", "📤 Syncing to Supabase...")
                supabase.from("trip_members").update(
                    TripMemberDto(
                        id = updated.id,
                        tripId = updated.tripId,
                        userId = updated.userId,
                        displayName = updated.displayName,
                        role = newRole,
                        avatarUrl = updated.avatarUrl,
                        joinedAt = null
                    )
                ){
                    filter { eq("id", memberId) }
                }

                // 3. Mark as synced
                tripMemberDao.updateMember(updated.copy(isSynced = true))
                Log.d("TripMemberRepo", "✅ Synced to Supabase")

            }catch (syncError: Exception){
                Log.e("TripMemberRepo", "⚠️ Sync failed: ${syncError.message}")
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun syncUnsyncedMembers(tripId: String): Result<Unit> {
        return try {
            Log.d("TripMemberRepo", "🔄 Syncing unsynced members for trip: $tripId")

            if (!sessionManager.hasValidSession()) {
                Log.w("TripMemberRepo", "⚠️ No session, aborting sync")
                return Result.Success(Unit)
            }

            val unsyncedMembers = tripMemberDao.getUnsyncedMembersForTrip(tripId)
            Log.d("TripMemberRepo", "📊 Found ${unsyncedMembers.size} unsynced members")

            if (unsyncedMembers.isEmpty()) {
                Log.d("TripMemberRepo", "✅ No members to sync")
                return Result.Success(Unit)
            }

            unsyncedMembers.forEach { entity ->
                try {
                    val dto = entity.toDto()

                    // Check if member already exists on server
                    val existing = supabase.from("trip_members")
                        .select {
                            filter {
                                eq("id", entity.id)
                            }
                        }
                        .decodeSingleOrNull<TripMemberDto>()

                    if (existing == null) {
                        // Insert new member
                        supabase.from("trip_members").insert(dto)
                        Log.d("TripMemberRepo", "  ✅ Inserted: ${entity.displayName}")
                    } else {
                        // Update existing member
                        supabase.from("trip_members").update(dto) {
                            filter {
                                eq("id", entity.id)
                            }
                        }
                        Log.d("TripMemberRepo", "  🔄 Updated: ${entity.displayName}")
                    }

                    tripMemberDao.updateMember(
                        entity.copy(
                            isSynced = true,
                            lastModified = System.currentTimeMillis()
                        )
                    )

                } catch (e: Exception) {
                    Log.e("TripMemberRepo", "  ❌ Failed to sync member ${entity.displayName}", e)
                }
            }

            Result.Success(Unit)

        }catch (e: Exception){
            Log.e("TripMemberRepo", "❌ Sync failed", e)
            Result.Error(e)
        }
    }




}
