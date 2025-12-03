package com.example.splitify.data.repository

import android.annotation.SuppressLint
import com.example.splitify.data.local.dao.TripDao
import com.example.splitify.data.local.toDomainModel
import com.example.splitify.data.local.toDomainModels
import com.example.splitify.data.local.toEntity
import com.example.splitify.domain.model.Trip
import com.example.splitify.domain.repository.TripRepository
import com.example.splitify.util.Result
import com.example.splitify.util.asError
import com.example.splitify.util.asSuccess
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

import javax.inject.Inject

class TripRepositoryImpl @Inject constructor(
    private val tripDao: TripDao,
    private val supabase: SupabaseClient
): TripRepository {

    //Get all trips from local database
    //Converts entities to domain models automatically
    override fun getAllTrips(): Flow<List<Trip>> {
        return tripDao.getAllTrips()
            .map { entities ->
                entities.toDomainModels()
            }
    }

    override fun getTripsByUser(userId: String): Flow<List<Trip>> {
        return tripDao.getTripsByUser(userId)
            .map { entities ->
                entities.toDomainModels()
            }
    }

    override suspend fun getTripById(tripId: String): Trip? {
        return try {
            tripDao.getTripById(tripId)?.toDomainModel()
        }
        catch (e: Exception){
            null
        }
    }

    override suspend fun createTrip(trip: Trip): Result<Trip> {
        return try {
            //1. Save to local database first
            val entity = trip.toEntity()
            tripDao.insertTrip(entity)

            //2. Try to sync to supabase
            try {
                val tripDto = TripDto(
                    id = trip.id,
                    name = trip.name,
                    description = trip.description,
                    created_by = trip.createdBy,
                    invite_code = trip.inviteCode,
                    start_date = trip.startDate.toString(),
                    end_date = trip.endDate.toString()
                )
                supabase.from("trips").insert(tripDto)
                println("✅ Trip synced to Supabase: ${trip.name}")
                // Mark as synced in local DB
                tripDao.insertTrip(entity.copy(isLocal = false, isSynced = true))
            }catch (syncError: Exception){
                println("⚠️ Failed to sync to Supabase: ${syncError.message}")
            }

            trip.asSuccess()
        }
        catch (e: Exception){
            e.asError()
        }
    }

    override suspend fun updateTrip(trip: Trip): Result<Unit> {
        return try {
            val entity = trip.toEntity()
            tripDao.updateTrip(entity)

            try {
                val tripDto = TripDto(
                    id = trip.id,
                    name = trip.name,
                    description = trip.description,
                    created_by = trip.createdBy,
                    invite_code = trip.inviteCode,
                    start_date = trip.startDate.toString(),
                    end_date = trip.endDate.toString()
                )
                supabase.from("trips").update(tripDto) {
                    filter {
                        eq("id", trip.id)
                    }
                }
                println("✅ Trip update synced to Supabase")
            }catch (syncError: Exception) {
                println("⚠️ Failed to sync update: ${syncError.message}")
            }

            Unit.asSuccess()
        }
        catch (e: Exception){
            e.asError()
        }
    }

    override suspend fun deleteTrip(tripId: String): Result<Unit> {
        return try {
            tripDao.deleteTripById(tripId)

            try {
                supabase.from("trips").delete {
                    filter {
                        eq("id", tripId)
                    }
                }
            }catch (syncError:Exception){
                println("⚠️ Failed to delete from Supabase: ${syncError.message}")
            }

            Unit.asSuccess()
        } catch (e: Exception) {
            e.asError()
        }
    }

    override suspend fun syncTrips(): Result<Unit> {
        return try {
            // TODO: Implement Supabase sync
            // 1. Get unsynced trips (isLocal = true)
            // 2. Upload to Supabase
            // 3. Mark as synced
            Unit.asSuccess()
        } catch (e: Exception) {
            e.asError()
        }
    }

     override suspend fun clearLocalTrips(): Result<Unit> {
        return try {
            tripDao.deleteAllTrips()
            Unit.asSuccess()
        } catch (e: Exception) {
            e.asError()
        }
    }
}

//DTO for supabase trips table
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class TripDto(
    val id: String,
    val name: String,
    val description: String?,
    val created_by: String,
    val invite_code: String,
    val start_date: String,
    val end_date: String?= null,

)