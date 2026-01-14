package com.example.splitify.data.repository

import android.annotation.SuppressLint
import android.util.Log
import com.example.splitify.data.local.dao.TripDao
import com.example.splitify.data.local.toDomain
import com.example.splitify.data.local.toDomainModels
import com.example.splitify.data.local.toEntity
import com.example.splitify.data.remote.dto.TripDto
import com.example.splitify.data.remote.toDto
import com.example.splitify.data.remote.toEntity
import com.example.splitify.data.sync.SyncManager
import com.example.splitify.domain.model.Trip
import com.example.splitify.domain.repository.TripRepository
import com.example.splitify.util.Result
import com.example.splitify.util.asError
import com.example.splitify.util.asSuccess
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate

import javax.inject.Inject

class TripRepositoryImpl @Inject constructor(
    private val tripDao: TripDao,
    private val supabase: SupabaseClient,
    private val syncManager: SyncManager
): TripRepository {

    suspend fun getAllTripsId(): List<String>{
        return tripDao.getAllTripsId()
    }

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
            tripDao.getTripById(tripId)?.toDomain()
        }
        catch (e: Exception){
            null
        }
    }

    override fun observeTripById(tripId: String): Flow<Result<Trip>> = flow {
        emit(Result.Loading)
        try {
            val trip = tripDao.getTripById(tripId)?.toDomain()
            if (trip != null) {
                emit(Result.Success(trip))
            } else {
                emit(Result.Error(Exception("Trip not found")))
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }


    override suspend fun createTrip(trip: Trip): Result<Trip> {
        return try {
            //1. Save to local database first
            println("Creating trip in Room: ${trip.name}")
            val entity = trip.toEntity().copy(isLocal = true, isSynced = false)
            tripDao.insertTrip(entity)
            println("Created trip in Room: ${trip.name}")

            //2. Try to sync to supabase
            println("📤 Syncing to Supabase...")
            try {
                val session = supabase.auth.currentSessionOrNull()
                if(session==null){
                    println("⚠️ No session, skipping Supabase sync")
                    return trip.asSuccess()

                }

                val tripDto = TripDto(
                    id = trip.id,
                    name = trip.name,
                    description = trip.description,
                    createdBy = trip.createdBy,
                    inviteCode = trip.inviteCode,
                    startDate = trip.startDate,
                    endDate = trip.endDate
                )
                supabase.from("trips").insert(tripDto)
                println("✅ Trip synced to Supabase: ${trip.name}")

                // Mark as synced in local DB
                tripDao.insertTrip(entity.copy(isLocal = false, isSynced = true))

            }catch (syncError: Exception){
                println("⚠️ Failed to sync to Supabase: ${syncError.message}")
                syncError.printStackTrace()
            }

            //Trigger immediate sync
            syncManager.triggerImmediateSync()

            trip.asSuccess()
        }
        catch (e: Exception){
            e.printStackTrace()
            e.asError()
        }
    }

    override suspend fun updateTrip(trip: Trip): Result<Unit> {
        return try {
            //1. Save to local database first
            val entity = trip.toEntity().copy(isLocal = false, isSynced = false)
            tripDao.updateTrip(entity)


            //2. Try to sync to supabase
            try {
                val tripDto = trip.toEntity().toDto()
                supabase.from("trips").update(tripDto) {
                    filter {
                        eq("id", trip.id)
                    }
                }
                tripDao.updateTrip(entity.copy(isSynced = true))
                println("✅ Trip update synced to Supabase")
            }catch (syncError: Exception) {
                println("⚠️ Failed to sync update: ${syncError.message}")
            }

            //Trigger immediate sync
            syncManager.triggerImmediateSync()
            Unit.asSuccess()
        }
        catch (e: Exception){
            e.asError()
        }
    }

    override suspend fun deleteTrip(tripId: String): Result<Unit> {
        return try {
            println("Deleting trip from Room: $tripId")
            tripDao.deleteTripById(tripId)
            println("✅ Deleted from Room")

            try {
                println("📤 Syncing to Supabase...")
                val session = supabase.auth.currentSessionOrNull()
                if (session == null) {
                    println("⚠️ No session, skipping Supabase delete")
                    return Unit.asSuccess()
                }
                println("📤 Deleting from Supabase...")
                supabase.from("trips").delete {
                    filter {
                        eq("id", tripId)
                    }
                    println("✅ Deleted from Supabase")
                }
            }catch (syncError:Exception){
                println("⚠️ Failed to delete from Supabase: ${syncError.message}")
                syncError.printStackTrace()
            }

            //Trigger immediate sync
            syncManager.triggerImmediateSync()

            Unit.asSuccess()
        } catch (e: Exception) {
            println("❌ Delete failed: ${e.message}")
            e.printStackTrace()
            e.asError()
        }
    }

    override suspend fun syncTrips(): Result<Unit> {
        return try {
            // 1. Get unsynced trips (isLocal = true)
            val unsyncedTrips = tripDao.getUnsyncedTrips()
            // 2. Upload to Supabase
            unsyncedTrips.forEach { entity ->
                try {
                    supabase.from("trips").insert(entity.toDto())
                    // MUST save the update back to the database
                    tripDao.updateTrip(entity.copy(isLocal = false, isSynced = true))
                } catch (e: Exception) {
                    println("❌ Failed to sync individual trip ${entity.id}")
                }
            }
            // 3. Mark as synced
            unsyncedTrips.forEach {tripEntity ->
                tripEntity.copy(
                    isSynced = true,
                    isLocal = false
                )
            }
            Unit.asSuccess()
        } catch (e: Exception) {
            e.asError()
        }
    }


    override suspend fun downloadTripsFromSupabase(): Result<Unit> {
        return try {
            Log.d("TripRepo", "📥 Downloading trips from Supabase...")

            val session = supabase.auth.currentSessionOrNull()
            if (session == null) {
                Log.w("TripRepo", "⚠️ No session")
                return Result.Success(Unit)
            }

            val userId = session.user?.id ?: return Result.Success(Unit)

            // Fetch trips from Supabase
            val tripDtos = supabase.from("trips")
                .select()
                .decodeList<TripDto>()

            Log.d("TripRepo", "📊 Found ${tripDtos.size} trips on server")

            // Save to Room
            tripDtos.forEach { dto ->
                val entity = dto.toEntity(createdAt = System.currentTimeMillis()).copy(
                    isLocal = false,
                    isSynced = true
                )
                tripDao.insertTrip(entity)
            }

            Log.d("TripRepo", "✅ Downloaded ${tripDtos.size} trips")
            Result.Success(Unit)

        } catch (e: Exception) {
            Log.e("TripRepo", "❌ Download failed", e)
            Result.Error(e)
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

