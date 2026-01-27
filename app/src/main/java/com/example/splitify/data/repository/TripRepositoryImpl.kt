package com.example.splitify.data.repository

import android.annotation.SuppressLint
import android.util.Log
import androidx.room.withTransaction
import com.example.splitify.data.local.AppDatabase
import com.example.splitify.data.local.SessionManager
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
import com.example.splitify.data.remote.toDomain
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
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import org.slf4j.MDC.put
import java.util.UUID
import javax.inject.Inject

class TripRepositoryImpl @Inject constructor(
    private val tripDao: TripDao,
    private val tripMemberDao: TripMemberDao,
    private val expenseDao: ExpenseDao,
    private val expenseSplitDao: ExpenseSplitDao,
    private val supabase: SupabaseClient,
    private val syncManager: SyncManager,
    private val sessionManager: SessionManager,
    private val database: AppDatabase
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

//    private suspend fun downloadExpensesForTrips(tripIds: List<String>) {
//        if (tripIds.isEmpty()) return
//
//        try {
//            Log.d("TripRepo", "💰 Downloading expenses for ${tripIds.size} trips...")
//
//            val expenseDtos = supabase.from("expenses")
//                .select {
//                    filter {
//                        isIn("trip_id", tripIds)
//                    }
//                }
//                .decodeList<ExpenseDto>()
//
//            Log.d("TripRepo", "📊 Downloaded ${expenseDtos.size} expenses")
//
//            if (expenseDtos.isEmpty()) {
//                Log.d("TripRepo", "ℹ️ No expenses found")
//                return
//            }
//
//            // Download splits for these expenses
//            val expenseIds = expenseDtos.map { it.id }
//            val splitDtos = supabase.from("expense_splits")
//                .select {
//                    filter {
//                        isIn("expense_id", expenseIds)
//                    }
//                }
//                .decodeList<ExpenseSplitDto>()
//
//            Log.d("TripRepo", "📊 Downloaded ${splitDtos.size} splits")
//
//            // Save expenses
//            expenseDtos.forEach { dto ->
//                try {
//                    val entity = dto.toEntity().copy(
//                        isLocal = false,
//                        isSynced = true
//                    )
//
//                    val existing = expenseDao.getExpenseById(entity.id)
//                    if (existing == null) {
//                        expenseDao.insertAnExpense(entity)
//                    } else {
//                        expenseDao.updateExpense(entity)
//                    }
//                } catch (e: Exception) {
//                    Log.e("TripRepo", "❌ Failed to save expense: ${e.message}")
//                }
//            }
//
//            // Save splits
//            splitDtos.forEach { dto ->
//                try {
//                    val entity = dto.toEntity()
//                    expenseSplitDao.insertSplit(entity)
//                } catch (e: Exception) {
//                    Log.e("TripRepo", "❌ Failed to save split: ${e.message}")
//                }
//            }
//
//            Log.d("TripRepo", "✅ Expenses and splits saved to Room")
//        } catch (e: Exception) {
//            Log.e("TripRepo", "❌ Failed to download expenses", e)
//        }
//    }
private suspend fun downloadExpensesForTrips(tripIds: List<String>) {
    if (tripIds.isEmpty()) return

    try {
        Log.d("TripRepo", "💰 Downloading expenses for ${tripIds.size} trips...")

        // 1. Download expenses
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

        // 2. Download splits
        val expenseIds = expenseDtos.map { it.id }
        val splitDtos = supabase.from("expense_splits")
            .select {
                filter {
                    isIn("expense_id", expenseIds)
                }
            }
            .decodeList<ExpenseSplitDto>()

        Log.d("TripRepo", "📊 Downloaded ${splitDtos.size} splits")

        // 3. Get ALL member IDs referenced in expenses
        val memberIds = expenseDtos.map { it.paidBy }.distinct()

        // 4. Check which members are missing
        val existingMembers = memberIds.mapNotNull {
            tripMemberDao.getMemberById(it)?.id
        }
        val missingMemberIds = memberIds - existingMembers.toSet()

        // 5. Download missing members
        if (missingMemberIds.isNotEmpty()) {
            Log.d("TripRepo", "⚠️ Downloading ${missingMemberIds.size} missing members: $missingMemberIds")

            // Convert String IDs to proper format if needed
            val memberIdStrings = missingMemberIds.map { it.toString() }

            val missingMembers = supabase.from("trip_members")
                .select()
                .decodeList<TripMemberDto>()
                .filter { it.id in missingMemberIds }  // Filter client-side as fallback

            Log.d("TripRepo", "📦 Found ${missingMembers.size} missing members")

            missingMembers.forEach { dto ->
                try {
                    tripMemberDao.insertMember(dto.toEntity())
                    Log.d("TripRepo", "  ✅ Saved: ${dto.displayName} (${dto.id})")
                } catch (e: Exception) {
                    Log.e("TripRepo", "  ❌ Failed: ${dto.displayName} - ${e.message}")
                }
            }
        }

        // 6. Now save expenses (all referenced members exist)
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

        // 7. Save splits
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


override suspend fun validateInviteCode(code: String): Result<Trip> {
    return withContext(Dispatchers.IO){
        try {
            if(!sessionManager.hasValidSession()){
                return@withContext Result.Error(Exception("Not connected to internet"))
            }

            Log.d("TripRepo", "Searching for invite code: $code")

            val response = supabase.from("trips")
                .select {
                    filter {
                        eq("invite_code", code)
                    }
                }
                .decodeList<TripDto>()

            Log.d("TripRepo", "Response size: ${response.size}")

            if (response.isEmpty()) {
                return@withContext Result.Error(Exception("Invalid invite code"))
            }

            Log.d("TripRepo", "Trip found: ${response.first().name}")
            Result.Success(response.first().toDomain())

        } catch (e: Exception) {
            Log.e("TripRepo", "Validate invite code failed", e)
            Result.Error(
                e,
                "Failed to validate code: ${e.message}"
            )
        }
    }
}

    override suspend fun joinTripWithInviteCode(inviteCode: String): Result<Trip> {
        return try {
            val userId = sessionManager.getCurrentUserId() ?: throw Exception("No session")
            val userName = sessionManager.getUserName() ?: "Member"

            Log.d("TripRepo", "🎫 Joining: $inviteCode")

            // 1. Find trip
            val tripDto = supabase.from("trips")
                .select { filter { eq("invite_code", inviteCode) } }
                .decodeSingle<TripDto>()

            Log.d("TripRepo", "✅ Trip found: ${tripDto.name}")

            // 2. Add yourself as member (if not exists)
            try {
                val newMember = TripMemberDto(
                    id = UUID.randomUUID().toString(),
                    tripId = tripDto.id,
                    userId = userId,
                    displayName = userName,
                    role = "member"
                )
                supabase.from("trip_members").insert(newMember)
                Log.d("TripRepo", "✅ Added yourself to trip")
            } catch (e: Exception) {
                if (e.message?.contains("duplicate") == true) {
                    Log.d("TripRepo", "ℹ️ Already a member")
                } else throw e
            }

            // 3. Download ALL members
            val allMembers = supabase.from("trip_members")
                .select { filter { eq("trip_id", tripDto.id) } }
                .decodeList<TripMemberDto>()
//            val allMembers = supabase.postgrest.rpc(
//                function = "get_all_trip_members",
//                parameters = buildJsonObject { put("trip_id", tripDto.id) }
//            ).decodeList<TripMemberDto>()

            Log.d("TripRepo", "📦 Downloaded ${allMembers.size} members: ${allMembers.map { it.displayName }}")

            // 4. Download expenses
            val expenses = supabase.from("expenses")
                .select { filter { eq("trip_id", tripDto.id) } }
                .decodeList<ExpenseDto>()

            Log.d("TripRepo", "📦 Downloaded ${expenses.size} expenses")

            // 5. Download splits
            val splits = if (expenses.isNotEmpty()) {
                supabase.from("expense_splits")
                    .select { filter { isIn("expense_id", expenses.map { it.id }) } }
                    .decodeList<ExpenseSplitDto>()
            } else emptyList()

            Log.d("TripRepo", "📦 Downloaded ${splits.size} splits")

            // 6. Save to Room in CORRECT ORDER
            database.withTransaction {
                // Step 1: Trip
                tripDao.insertTrip(tripDto.toEntity(System.currentTimeMillis()))

                // Step 2: Members (expenses depend on these)
                allMembers.forEach { memberDto ->
                    tripMemberDao.insertMember(memberDto.toEntity())
                    Log.d("TripRepo", "  ✅ Saved member: ${memberDto.displayName} (${memberDto.id})")
                }

                // Step 3: Expenses (splits depend on these)
                expenses.forEach { expDto ->
                    val memberExists = tripMemberDao.getMemberById(expDto.paidBy)
                    if (memberExists == null) {
                        Log.e("TripRepo", "  ⚠️ SKIPPING expense ${expDto.description} - missing paidBy member: ${expDto.paidBy}")
                    } else {
                        expenseDao.insertAnExpense(expDto.toEntity())
                        Log.d("TripRepo", "  ✅ Saved expense: ${expDto.description}")
                    }
                }

                // Step 4: Splits
                splits.forEach { splitDto ->
                    try {
                        expenseSplitDao.insertSplit(splitDto.toEntity())
                    } catch (e: Exception) {
                        Log.e("TripRepo", "  ⚠️ Failed to save split: ${e.message}")
                    }
                }
            }

            Log.d("TripRepo", "🎉 Success: ${allMembers.size}M ${expenses.size}E ${splits.size}S")
            tripDto.toEntity(System.currentTimeMillis()).toDomain().asSuccess()

        } catch (e: Exception) {
            Log.e("TripRepo", "❌ Join failed", e)
            e.asError()
        }
    }


    override suspend fun joinTrip(
        tripId: String,
        userId: String,
        displayName: String
    ): Result<Unit> {
        TODO("Not yet implemented")
    }




    // Helper function to sync all trip data to Room
    private suspend fun syncTripDataToRoom(tripId: String) {
        try {
            Log.d("TripRepo", "📥 Syncing trip data to Room...")

            // 1. Sync members FIRST (critical for foreign key constraints)
            val members = supabase.from("trip_members")
                .select {
                    filter {
                        eq("trip_id", tripId)
                    }
                }
                .decodeList<TripMemberDto>()

            Log.d("TripRepo", "📦 Downloaded ${members.size} members")

            members.forEach { memberDto ->
                try {
                    tripMemberDao.insertMember(memberDto.toEntity().copy(isSynced = true))
                    Log.d("TripRepo", "  ✅ Saved member: ${memberDto.displayName}")
                } catch (e: Exception) {
                    Log.e("TripRepo", "  ⚠️ Failed to save member: ${memberDto.displayName} - ${e.message}")
                }
            }
            Log.d("TripRepo", "✅ Synced ${members.size} members")

            // 2. Sync expenses (now all paidBy references will be valid)
            val expenses = supabase.from("expenses")
                .select {
                    filter {
                        eq("trip_id", tripId)
                    }
                }
                .decodeList<ExpenseDto>()

            Log.d("TripRepo", "📦 Downloaded ${expenses.size} expenses")

            expenses.forEach { expenseDto ->
                try {
                    // Verify the paidBy member exists before inserting
                    val memberExists = tripMemberDao.getMemberById(expenseDto.paidBy)
                    if (memberExists != null) {
                        expenseDao.insertAnExpense(expenseDto.toEntity().copy(
                            isLocal = false,
                            isSynced = true
                        ))
                        Log.d("TripRepo", "  ✅ Saved expense: ${expenseDto.description}")
                    } else {
                        Log.e("TripRepo", "  ⚠️ Skipping expense - paidBy member not found: ${expenseDto.paidBy}")
                    }
                } catch (e: Exception) {
                    Log.e("TripRepo", "  ❌ Failed to save expense: ${expenseDto.description} - ${e.message}")
                }
            }
            Log.d("TripRepo", "✅ Synced ${expenses.size} expenses")

            // 3. Sync splits
            val allSplits = mutableListOf<ExpenseSplitDto>()
            expenses.forEach { expense ->
                val splits = supabase.from("expense_splits")
                    .select {
                        filter {
                            eq("expense_id", expense.id)
                        }
                    }
                    .decodeList<ExpenseSplitDto>()
                allSplits.addAll(splits)
            }

            Log.d("TripRepo", "📦 Downloaded ${allSplits.size} splits")

            allSplits.forEach { splitDto ->
                try {
                    expenseSplitDao.insertSplit(splitDto.toEntity())
                } catch (e: Exception) {
                    Log.e("TripRepo", "  ❌ Failed to save split: ${e.message}")
                }
            }
            Log.d("TripRepo", "✅ Synced ${allSplits.size} expense splits")

        } catch (e: Exception) {
            Log.e("TripRepo", "⚠️ Failed to sync trip data (Ask Gemini)", e)
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

// Helper data class for parsing the RPC response
@SuppressLint("UnsafeOptInUsageError")
@kotlinx.serialization.Serializable
data class TripDataBundle(
    val trip: TripDto? = null,
    val members: List<TripMemberDto>? = null,
    val expenses: List<ExpenseDto>? = null,
    val splits: List<ExpenseSplitDto>? = null
)

