package com.example.splitify.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.splitify.data.local.entity.TripMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripMemberDao {
    @Query("Select * from trip_members where trip_id = :tripId order by joined_at asc")
    fun getAllMembersForTrip(tripId: String): Flow<List<TripMemberEntity>>

    @Query("Select * from trip_members where id = :memberId")
    suspend fun getMemberById(memberId: String): TripMemberEntity?

    @Query("Select * from trip_members where trip_id = :tripId and user_id = :userId")
    suspend fun getMemberByTripAndUser(tripId: String, userId: String): TripMemberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: TripMemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<TripMemberEntity>)

    @Update
    suspend fun updateMember(member: TripMemberEntity)

    @Query("Delete from trip_members where id = :memberId")
    suspend fun deleteMember(memberId: String)

    @Query("Delete from trip_members where trip_id = :tripId")
    suspend fun deleteAllMembersForTrip(tripId: String)

    @Query("Select * from trip_members where trip_id = :tripId and is_synced = 0")
    suspend fun getUnsyncedMembersForTrip(tripId: String): List<TripMemberEntity>

    @Query("Update trip_members set is_synced = 1 where id = :memberId")
    suspend fun markedAsSynced(memberId: String)

    @Query("DELETE FROM trip_members WHERE user_id = :userId")
    suspend fun deleteAllMembersForUser(userId: String)



    @Query("""
        SELECT * FROM trip_members 
        WHERE user_id IS NOT NULL 
        AND display_name LIKE '%' || :query || '%'
        GROUP BY user_id
        LIMIT 20
    """)
    fun searchUsers(query: String): Flow<List<TripMemberEntity>>

    @Query("DELETE FROM trip_members")
    fun deleteAllMembers()
}