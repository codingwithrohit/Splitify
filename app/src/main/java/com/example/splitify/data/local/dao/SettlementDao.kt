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


    @Query("Select * from settlements where tripId = :tripId order by createdAt desc")
    fun getSettlementsForTrip(tripId: String): Flow<List<SettlementEntity>>

    @Query("Select * from settlements where id = :settlementId")
    suspend fun getSettlementById(settlementId: String): SettlementEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettlement(settlement: SettlementEntity)

    @Update
    suspend fun updateSettlement(settlement: SettlementEntity)

    @Query("""
        Select * from settlements 
        where (fromMemberId = :memberId or toMemberId = :memberId)
        and status = 'PENDING' 
        order by createdAt desc
        """)
    fun getPendingSettlementsForMember(memberId: String): Flow<List<SettlementEntity>>

    @Query("Select * from settlements where tripId = :tripId and status = 'CONFIRMED' order by settledAt desc")
    fun getConfirmedSettlements(tripId: String): Flow<List<SettlementEntity>>

    @Query("DELETE FROM settlements WHERE tripId = :tripId")
    suspend fun deleteSettlementsForTrip(tripId: String)
}