package com.example.splitify.data.repository

import android.annotation.SuppressLint
import com.example.splitify.data.local.dao.TripDao
import com.example.splitify.data.local.toDomain
import com.example.splitify.data.local.toDomainModels
import com.example.splitify.data.local.toEntity
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
            val entity = trip.toEntity()
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

            trip.asSuccess()
        }
        catch (e: Exception){
            e.printStackTrace()
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
                    createdBy = trip.createdBy,
                    inviteCode = trip.inviteCode,
                    startDate = trip.startDate,
                    endDate = trip.endDate
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

            Unit.asSuccess()
        } catch (e: Exception) {
            println("❌ Delete failed: ${e.message}")
            e.printStackTrace()
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

    @SerialName("created_by")  // Maps to 'created_by' column
    val createdBy: String,

    @SerialName("invite_code")  // Maps to 'invite_code' column
    val inviteCode: String,
    @Serializable(with = LocalDateSerializer::class)
    @SerialName("start_date")  // Maps to 'start_date' column
    val startDate: LocalDate,
    @Serializable(with = LocalDateSerializer::class)
    @SerialName("end_date")  // Maps to 'end_date' column
    val endDate: LocalDate?

)
object LocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.toString()) // yyyy-MM-dd
    }

    override fun deserialize(decoder: Decoder): LocalDate {
        return LocalDate.parse(decoder.decodeString())
    }
}
