package com.example.splitify.data.repository

import android.util.Log
import com.example.splitify.data.local.dao.SettlementDao
import com.example.splitify.data.local.entity.SettlementEntity
import com.example.splitify.data.local.toDomain
import com.example.splitify.data.local.toEntity
import com.example.splitify.data.remote.toDto
import com.example.splitify.data.sync.SyncManager
import com.example.splitify.domain.model.Settlement
import com.example.splitify.domain.model.SettlementStatus
import com.example.splitify.domain.repository.SettlementRepository
import com.example.splitify.util.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class SettlementRepositoryImpl @Inject constructor(
    private val settlementDao: SettlementDao,
    private val supabase: SupabaseClient,
    private val syncManager: SyncManager
): SettlementRepository {

    override suspend fun createSettlement(
        tripId: String,
        fromMemberId: String,
        toMemberId: String,
        amount: Double,
        notes: String?
    ): Result<Settlement> {
        return try {
            Log.d("SettlementRepo", "Creating settlement: $amount")
            val settlement = Settlement(
                id = UUID.randomUUID().toString(),
                tripId = tripId,
                fromMemberId = fromMemberId,
                toMemberId = toMemberId,
                amount = amount,
                notes = notes,
                status = SettlementStatus.PENDING,
                createdAt = System.currentTimeMillis(),
                settledAt = null
            )
            settlementDao.insertSettlement(settlement.toEntity())
            Log.d("SettlementRepo", "Settlement saved to room")

            try {
                val session = supabase.auth.currentSessionOrNull()
                if(session == null){
                    Log.d("SettlementRepo", "No session, skipping sync")
                    return Result.Success(settlement)
                }

                val settlementDto = settlement.toEntity().toDto()
                supabase.from("settlements").insert(settlementDto)
                Log.d("SettlementRepo", "Settlement synced to Supabase")

            }catch (e: Exception){
                Result.Error(Exception("Failed to sync to Supabase"))
            }

            //Trigger immediate sync
            syncManager.triggerImmediateSync()

            Result.Success(settlement)
        } catch (e: Exception){
            Result.Error(e, "Failed to create settlement")
        }
    }

    override suspend fun confirmSettlement(settlementId: String): Result<Settlement> {
        return try {
            Log.d("SettlementRepo", "✓ Confirming settlement: $settlementId")

            // 1. Get settlement
            val settlement = settlementDao.getSettlementById(settlementId)
                ?: return Result.Error(Exception("Settlement not found"))

            // Check if settlement is already confirmed
            if(settlement.status == SettlementStatus.CONFIRMED.name){
                return Result.Error(Exception("Settlement is already confirmed"))
            }

            //2. Update the settlement status to confirmed and settled at time
            val updatedSettlement = settlement.copy(
                status = SettlementStatus.CONFIRMED.name,
                settledAt = System.currentTimeMillis()
            )
            settlementDao.updateSettlement(updatedSettlement)
            Log.d("SettlementRepo", "✓ Settlement confirmed")

            //3. Try to sync to Supabase
            try {
                val session = supabase.auth.currentSessionOrNull()
                if (session != null) {
                    Log.d("SettlementRepo", "📤 Syncing to Supabase...")
                    supabase.from("settlements").update(updatedSettlement.toDto()) {
                        filter { eq("id", settlementId) }
                    }
                    Log.d("SettlementRepo", "✅ Synced to Supabase")
                }
            }catch (e: Exception) {
                Log.e("SettlementRepo", "⚠️ Sync failed: ${e.message}")
            }

            //Trigger sync
            syncManager.triggerImmediateSync()

            Result.Success(updatedSettlement.toDomain())
        } catch (e: Exception){
            Result.Error(e, "Failed to confirm settlement")
        }
    }

    override suspend fun cancelSettlement(
        settlementId: String,
        cancellingMemberId: String
    ): Result<Unit> {
        return try {
            Log.d("SettlementRepo", "✕ Cancelling settlement: $settlementId")
            val settlement = settlementDao.getSettlementById(settlementId)

            if (settlement == null) {
                return Result.Error(Exception("Settlement not found"))
            }

            if (settlement.status != "pending") {
                return Result.Error(Exception("Can only cancel pending settlements"))
            }

            if (settlement.fromMemberId != cancellingMemberId) {
                return Result.Error(Exception("Only the payer can cancel this settlement"))
            }

            settlementDao.deleteSettlement(settlementId)
            Log.d("SettlementRepo", "✅ Deleted from Room")

            // 2. Delete from Supabase
            try {
                val session = supabase.auth.currentSessionOrNull()
                if (session != null) {
                    Log.d("SettlementRepo", "📤 Deleting from Supabase...")
                    supabase.from("settlements").delete {
                        filter { eq("id", settlementId) }
                    }
                    Log.d("SettlementRepo", "✅ Deleted from Supabase")
                }
            } catch (e: Exception) {
                Log.e("SettlementRepo", "⚠️ Delete sync failed: ${e.message}")
            }

            // Trigger sync
            syncManager.triggerImmediateSync()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override fun getSettlementsForTrip(tripId: String): Flow<Result<List<Settlement>>> {
        return settlementDao.getSettlementsForTrip(tripId)
            .map { entities ->
                Result.Success(entities.toDomain())
            }
    }

    override fun getPendingSettlementsForMember(memberId: String): Flow<Result<List<Settlement>>> {
        return settlementDao.getPendingSettlementsForMember(memberId)
            .map { entities ->
                Result.Success(entities.toDomain())
            }
    }

    override fun getPendingSettlementsBetweenMembers(
        fromMemberId: String,
        toMemberId: String
    ): Flow<Result<List<Settlement>>> {
        return settlementDao.getPendingSettlementsBetweenMembers(fromMemberId, toMemberId)
            .map { entities ->
                Result.Success(entities.toDomain())
            }
    }

    override suspend fun updateSettlementStatus(
        settlementId: String,
        status: SettlementStatus
    ): Result<Settlement> {
        return try {
            Log.d("SettlementRepo", "✏️ Updating settlement status: $status")

            val settlement = settlementDao.getSettlementById(settlementId)
                ?: return Result.Error(Exception("Settlement not found"))

            // 1. Update in Room
            val updatedSettlement = settlement.copy(
                status = status.name,
                settledAt = if (status == SettlementStatus.CONFIRMED) {
                    System.currentTimeMillis()
                } else {
                    settlement.settledAt
                }
            )
            settlementDao.updateSettlement(updatedSettlement)
            Log.d("SettlementRepo", "✅ Updated in Room")

            // 2. Sync to Supabase
            try {
                val session = supabase.auth.currentSessionOrNull()
                if (session != null) {
                    Log.d("SettlementRepo", "📤 Syncing to Supabase...")
                    supabase.from("settlements").update(updatedSettlement.toDto()) {
                        filter { eq("id", settlementId) }
                    }
                    Log.d("SettlementRepo", "✅ Synced to Supabase")
                }
            } catch (e: Exception) {
                Log.e("SettlementRepo", "⚠️ Sync failed: ${e.message}")
            }

            // Trigger sync
            syncManager.triggerImmediateSync()

            Result.Success(updatedSettlement.toDomain())
        }catch (e: Exception){
            Result.Error(e, "Failed to update settlement: ${e.message}")
        }
    }
}