package com.example.splitify.data.repository

import com.example.splitify.data.local.dao.SettlementDao
import com.example.splitify.data.local.toDomain
import com.example.splitify.data.local.toEntity
import com.example.splitify.domain.model.Settlement
import com.example.splitify.domain.model.SettlementStatus
import com.example.splitify.domain.repository.SettlementRepository
import com.example.splitify.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class SettlementRepositoryImpl @Inject constructor(
    private val settlementDao: SettlementDao
): SettlementRepository {

    override suspend fun createSettlement(
        tripId: String,
        fromMemberId: String,
        toMemberId: String,
        amount: Double,
        notes: String?
    ): Result<Settlement> {
        return try {
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
            Result.Success(settlement)
        } catch (e: Exception){
            Result.Error(e, "Failed to create settlement")
        }
    }

    override suspend fun confirmSettlement(settlementId: String): Result<Settlement> {
        return try {
            // Retrieve the settlement from the database
            val settlement = settlementDao.getSettlementById(settlementId) ?:
            return Result.Error(Exception("Settlement not found"))

            // Check if settlement is already confirmed
            if(settlement.status == SettlementStatus.CONFIRMED.name){
                return Result.Error(Exception("Settlement is already confirmed"))
            }

            // Update the settlement status to confirmed and settled at time
            val updatedSettlement = settlement.copy(
                status = SettlementStatus.CONFIRMED.name,
                settledAt = System.currentTimeMillis()
            )
            settlementDao.updateSettlement(updatedSettlement)
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
            .catch { e ->
                Result.Error(e, "Failed to load settlements for trip")
            }
    }

    override fun getPendingSettlementsForMember(memberId: String): Flow<Result<List<Settlement>>> {
        return settlementDao.getPendingSettlementsForMember(memberId)
            .map { entities ->
                Result.Success(entities.toDomain())
            }
            .catch { e ->
                Result.Error(Exception("Failed to load pending settlements for member"), e.message.toString())
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
            .catch { e ->
                Result.Error(Exception("Failed to load pending settlements between members"), e.message.toString())
            }
    }

    override suspend fun updateSettlementStatus(
        settlementId: String,
        status: SettlementStatus
    ): Result<Settlement> {
        return try {
            val settlement = settlementDao.getSettlementById(settlementId)

            val updatedSettlement = settlement.copy(
                status = status.name,
                settledAt = if(status == SettlementStatus.CONFIRMED){
                    System.currentTimeMillis()
                }
                else{
                    settlement.settledAt
                }
            )
            settlementDao.updateSettlement(updatedSettlement)
            Result.Success(updatedSettlement.toDomain())
        }catch (e: Exception){
            Result.Error(Exception("Failed to update settlement"), e.message.toString())
        }
    }
}