package com.example.splitify.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.splitify.data.local.entity.SettlementEntity
import com.example.splitify.domain.model.SettlementStatus
import kotlinx.coroutines.flow.Flow
@Dao
interface SettlementDao {


    @Query("Select * from settlements where trip_id = :tripId order by created_at desc")
    fun getSettlementsForTrip(tripId: String): Flow<List<SettlementEntity>>

    @Query("Select * from settlements where id = :settlementId")
    suspend fun getSettlementById(settlementId: String): SettlementEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettlement(settlement: SettlementEntity)

    @Update
    suspend fun updateSettlement(settlement: SettlementEntity)

    @Query("""
        Select * from settlements 
        where (from_member_id = :memberId or to_member_id = :memberId)
        and status = 'PENDING' 
        order by created_at desc
        """)
    fun getPendingSettlementsForMember(memberId: String): Flow<List<SettlementEntity>>

    @Query("Select * from settlements where trip_id = :tripId and status = 'CONFIRMED' order by settled_at desc")
    fun getConfirmedSettlements(tripId: String): Flow<List<SettlementEntity>>

    @Query("DELETE FROM settlements WHERE trip_id = :tripId")
    suspend fun deleteSettlementsForTrip(tripId: String)
}