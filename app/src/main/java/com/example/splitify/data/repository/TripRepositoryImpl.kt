package com.example.splitify.data.repository

import android.annotation.SuppressLint
import android.util.Log
import com.example.splitify.data.local.dao.ExpenseDao
import com.example.splitify.data.local.dao.ExpenseSplitDao
import com.example.splitify.data.local.dao.TripDao
import com.example.splitify.data.local.dao.TripMemberDao
import com.example.splitify.data.local.toDomain
import com.example.splitify.data.local.toDomainModels
import com.example.splitify.data.local.toEntity
import com.example.splitify.data.remote.dto.ExpenseDto
import com.example.splitify.data.remote.dto.ExpenseSplitDto
import com.example.splitify.data.remote.dto.TripDto
import com.example.splitify.data.remote.dto.TripMemberDto
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
    private val tripMemberDao: TripMemberDao,
    private val expenseDao: ExpenseDao,
    private val expenseSplitDao: ExpenseSplitDao,
    private val supabase: SupabaseClient,
    private val syncManager: SyncManager
): TripRepository {

    override suspend fun getUserTripIds(userId: String): List<String> {
        return tripDao.getTripIdsByUser(userId)
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
            val session = supabase.auth.currentSessionOrNull() ?: return Unit.asSuccess()
            val userId = session.user?.id ?: ""

            val unsyncedTrips = tripDao.getUnsyncedTrips(userId)

            if (unsyncedTrips.isEmpty()) {
                return Unit.asSuccess()
            }

            Log.d("TripRepo", "🔄 Syncing ${unsyncedTrips.size} pending trips...")

            unsyncedTrips.forEach { entity ->
                try {
                    val dto = entity.toDto()

                    // Using upsert is safer: it inserts if new, updates if exists
                    supabase.from("trips").upsert(dto)

                    tripDao.updateTrip(
                        entity.copy(
                            isLocal = false,
                            isSynced = true,
                            lastModified = System.currentTimeMillis()
                        )
                    )
                    Log.d("TripRepo", "  ✅ Synced: ${entity.name}")
                } catch (e: Exception) {
                    Log.e("TripRepo", "  ❌ Sync failed for ${entity.name}: ${e.message}")
                }
            }
            Unit.asSuccess()
        } catch (e: Exception) {
            Log.e("TripRepo", "❌ Sync process interrupted", e)
            e.asError()
        }
    }


    override suspend fun downloadTripsFromSupabase(): Result<Unit> {
        return try {
            Log.d("TripRepo", "📥 Starting download from Supabase...")

            // 1. Check if user is logged in
            val session = supabase.auth.currentSessionOrNull()
            if (session == null) {
                Log.d("TripRepo", "⚠️ No session, skipping download")
                return Result.Success(Unit)
            }

            val userId = session.user?.id
            if (userId == null) {
                Log.e("TripRepo", "❌ No user ID in session")
                return Result.Success(Unit)
            }

            Log.d("TripRepo", "👤 Downloading trips for user: $userId")

            // 2. Fetch trips where user is a MEMBER (not just creator)
            // This includes trips created by user AND trips they were added to
            val memberTrips = supabase.from("trip_members")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<TripMemberDto>()

            if (memberTrips.isEmpty()) {
                Log.d("TripRepo", "ℹ️ No trips found for this user (first-time user)")
                return Result.Success(Unit)
            }
            Log.d("TripRepo", "📊 Found ${memberTrips.size} trip memberships")
            val tripIds = memberTrips.map {
                Log.d("TripRepo", "  - Trip: ${it.tripId}, Member: ${it.displayName}")
                it.tripId
            }.distinct()
            Log.d("TripRepo", "📊 User is member of ${tripIds.size} trips")

            // 3. Download those trips
            val tripDtos = supabase.from("trips")
                .select {
                    filter {
                        isIn("id", tripIds)
                    }
                }
                .decodeList<TripDto>()

            Log.d("TripRepo", "📦 Downloaded ${tripDtos.size} trips")

            // 4. Insert trips into Room (one by one to handle errors)
            tripDtos.forEach { dto ->
                try {
                    val entity = dto.toEntity(
                        createdAt = System.currentTimeMillis()
                    ).copy(
                        isLocal = false,
                        isSynced = true
                    )

                    // Check if trip already exists
                    val existing = tripDao.getTripById(entity.id)
                    if (existing == null) {
                        tripDao.insertTrip(entity)
                        Log.d("TripRepo", "✅ Inserted: ${entity.name}")
                    } else {
                        // Update if server version is newer
                        tripDao.updateTrip(entity)
                        Log.d("TripRepo", "🔄 Updated: ${entity.name}")
                    }
                } catch (e: Exception) {
                    Log.e("TripRepo", "❌ Failed to save trip ${dto.name}: ${e.message}")
                }
            }


            // 5. Download members for these trips
            downloadMembersForTrips(tripIds)

            // 6. Download expenses for these trips
            downloadExpensesForTrips(tripIds)

            Result.Success(Unit)

        } catch (e: Exception) {
            Log.e("TripRepo", "❌ Download failed", e)
            // ✅ Return success anyway - don't block app
            Result.Success(Unit)
        }
    }

    private suspend fun downloadMembersForTrips(tripIds: List<String>) {
        if (tripIds.isEmpty()) return

        try {
            Log.d("TripRepo", "👥 Downloading members for ${tripIds.size} trips...")

            val memberDtos = supabase.from("trip_members")
                .select {
                    filter {
                        isIn("trip_id", tripIds)
                    }
                }
                .decodeList<TripMemberDto>()

            Log.d("TripRepo", "📊 Downloaded ${memberDtos.size} members")

            memberDtos.forEach { dto ->
                try {
                    val entity = dto.toEntity().copy(isSynced = true)

                    val existing = tripMemberDao.getMemberById(entity.id)
                    if (existing == null) {
                        tripMemberDao.insertMember(entity)
                    } else {
                        tripMemberDao.updateMember(entity)
                    }
                } catch (e: Exception) {
                    Log.e("TripRepo", "❌ Failed to save member: ${e.message}")
                }
            }

            Log.d("TripRepo", "✅ Members saved to Room")
        } catch (e: Exception) {
            Log.e("TripRepo", "❌ Failed to download members", e)
        }
    }

    private suspend fun downloadExpensesForTrips(tripIds: List<String>) {
        if (tripIds.isEmpty()) return

        try {
            Log.d("TripRepo", "💰 Downloading expenses for ${tripIds.size} trips...")

            val expenseDtos = supabase.from("expenses")
                .select {
                    filter {
                        isIn("trip_id", tripIds)
                    }
                }
                .decodeList<ExpenseDto>()

            Log.d("TripRepo", "📊 Downloaded ${expenseDtos.size} expenses")

            if (expenseDtos.isEmpty()) {
                Log.d("TripRepo", "ℹ️ No expenses found")
                return
            }

            // Download splits for these expenses
            val expenseIds = expenseDtos.map { it.id }
            val splitDtos = supabase.from("expense_splits")
                .select {
                    filter {
                        isIn("expense_id", expenseIds)
                    }
                }
                .decodeList<ExpenseSplitDto>()

            Log.d("TripRepo", "📊 Downloaded ${splitDtos.size} splits")

            // Save expenses
            expenseDtos.forEach { dto ->
                try {
                    val entity = dto.toEntity().copy(
                        isLocal = false,
                        isSynced = true
                    )

                    val existing = expenseDao.getExpenseById(entity.id)
                    if (existing == null) {
                        expenseDao.insertAnExpense(entity)
                    } else {
                        expenseDao.updateExpense(entity)
                    }
                } catch (e: Exception) {
                    Log.e("TripRepo", "❌ Failed to save expense: ${e.message}")
                }
            }

            // Save splits
            splitDtos.forEach { dto ->
                try {
                    val entity = dto.toEntity()
                    expenseSplitDao.insertSplit(entity)
                } catch (e: Exception) {
                    Log.e("TripRepo", "❌ Failed to save split: ${e.message}")
                }
            }

            Log.d("TripRepo", "✅ Expenses and splits saved to Room")
        } catch (e: Exception) {
            Log.e("TripRepo", "❌ Failed to download expenses", e)
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

