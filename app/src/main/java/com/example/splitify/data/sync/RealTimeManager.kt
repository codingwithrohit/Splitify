package com.example.splitify.data.sync

import android.util.Log
import com.example.splitify.data.local.dao.ExpenseDao
import com.example.splitify.data.local.dao.ExpenseSplitDao
import com.example.splitify.data.local.dao.TripDao
import com.example.splitify.data.local.dao.TripMemberDao
import com.example.splitify.data.local.entity.ExpenseSplitEntity
import com.example.splitify.data.remote.dto.ExpenseDto
import com.example.splitify.data.remote.dto.ExpenseSplitDto
import com.example.splitify.data.remote.dto.TripMemberDto
import com.example.splitify.data.remote.toEntity
import com.example.splitify.util.NotificationManager
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.decodeOldRecord
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.broadcastFlow
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealtimeManager @Inject constructor(
    private val supabase: SupabaseClient,
    private val tripDao: TripDao,
    private val tripMemberDao: TripMemberDao,
    private val expenseDao: ExpenseDao,
    private val expenseSplitDao: ExpenseSplitDao,
    private val notificationManager: NotificationManager
) {
    companion object {
        private const val TAG = "RealtimeManager"
        private var subscriptionCount = 0
    }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeChannels = mutableMapOf<String, io.github.jan.supabase.realtime.RealtimeChannel>()
    private val subscriptionMutex = Mutex()
    private val pendingExpenseUpdates = mutableSetOf<String>()
    private val updateMutex = Mutex()
    private val notificationHelper = RealtimeNotificationHelper(notificationManager)


    fun subscribeToGlobalTrips() {
        scope.launch {
            subscriptionMutex.withLock {
                if (activeChannels.containsKey("global")) {
                    Log.w(TAG, "⚠️ Already subscribed to global trips")
                    return@launch
                }
            }

            Log.d(TAG, "🔌 Subscribing to global_trips channel")

            val channel = supabase.realtime.channel("global_trips")

            channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "trips"
            }.onEach { action ->
                if (action is PostgresAction.Delete) {
                    val id = action.oldRecord["id"]
                        ?.toString()
                        ?.replace("\"", "")

                    Log.d(TAG, "Attempting to delete trip from local DB: $id")

                    id?.let { tripDao.deleteTripById(it) }
                }
            }.launchIn(scope)

            channel.subscribe()

            subscriptionMutex.withLock {
                activeChannels["global"] = channel
            }

            Log.d(TAG, "✅ Subscribed to global trips")
        }
    }

    suspend fun broadcastRefresh(tripId: String) {
        try {
            val channel = subscriptionMutex.withLock { activeChannels[tripId] }
            // In newer versions, we pass the object directly as the first argument
            // or specifically use the parameter name 'message'
            channel?.broadcast(
                event = "sync_event",
                message = buildJsonObject {
                    put("action", "REFRESH_REQUIRED")
                }
            )
            Log.d(TAG, "📡 Broadcast sent for trip: $tripId")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Broadcast failed", e)
        }
    }

    fun subscribeToTrip(tripId: String) {
        subscriptionCount++
        Log.d(TAG, "📊 Subscription attempt #$subscriptionCount for trip: $tripId")
        Log.d(TAG, "📊 subscribeToTrip() called from:")
        Thread.currentThread().stackTrace.take(10).forEach {
            Log.d(TAG, "  ${it.className}.${it.methodName}:${it.lineNumber}")
        }

        scope.launch {

//            val currentCount = subscriptionMutex.withLock {
//                activeChannels.size
//            }
//
//            if (currentCount >= 2) {  // Allow max 2: global + 1 active trip
//                Log.e(TAG, "❌ SUBSCRIPTION LIMIT REACHED ($currentCount active)")
//                Log.e(TAG, "  Active channels: ${activeChannels.keys}")
//                Log.e(TAG, "  Rejecting subscription to: $tripId")
//                return@launch
//            }

            val alreadySubscribed = subscriptionMutex.withLock {
                activeChannels.containsKey(tripId)
            }

            if (alreadySubscribed) return@launch

            // Check subscription limit
            val currentCount = subscriptionMutex.withLock {
                activeChannels.size
            }

            if (currentCount >= 3) {
                Log.e(TAG, "❌ SUBSCRIPTION LIMIT REACHED ($currentCount active)")
                return@launch
            }

            try {
                val channel = supabase.realtime.channel("trip_$tripId")
                subscriptionMutex.withLock {
                    activeChannels[tripId] = channel
                }

                // Inside subscribeToTrip
                channel.broadcastFlow<JsonObject>(event = "sync_event")
                    .onEach { message -> // Changed 'payload' to 'message' to match the result
                        val action = message["action"]?.jsonPrimitive?.content
                        if (action == "REFRESH_REQUIRED") {
                            Log.d(TAG, "🔄 Refresh signal received via Broadcast!")
                        }
                    }.launchIn(scope)

                channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                    table = "trips"
                    filter = "id=eq.$tripId"
                }.onEach { action ->
                    when (action) {
                        is PostgresAction.Delete -> {
                            Log.w(TAG, "⚠️ Trip $tripId was deleted by admin")
                            handleTripDelete(tripId)
                        }
                        else -> {}
                    }
                }.launchIn(scope)

                // --- EXPENSES FLOW ---
                channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                    table = "expenses"
                    filter = "trip_id=eq.$tripId"
                }.onEach { action ->
                    when (action) {
                        is PostgresAction.Insert -> {
                            val record = action.decodeRecord<ExpenseDto>()
                            handleExpenseInsert(record)
                        }
//                        is PostgresAction.Update -> {
//                            val record = action.decodeRecord<ExpenseDto>()
//                            handleExpenseUpdate(record)
//                        }
                        is PostgresAction.Update -> {
                            val record = action.decodeRecord<ExpenseDto>()

                            // Mark as update pending
                            updateMutex.withLock {
                                pendingExpenseUpdates.add(record.id)
                            }

                            try {
                                handleExpenseUpdate(record)
                            } finally {
                                // Clear pending flag after processing
                                updateMutex.withLock {
                                    pendingExpenseUpdates.remove(record.id)
                                }
                            }
                        }
                        is PostgresAction.Delete -> {
                            val expenseId = action.oldRecord["id"]?.toString()?.replace("\"", "")

                            if (expenseId != null) {
                                handleExpenseDelete(expenseId)
                                Log.d(TAG, "🗑️ Expense deleted: $expenseId")
                            } else {
                                Log.e(TAG, "❌ DELETE event missing expense ID")
                            }
                        }
                        is PostgresAction.Select -> {
                            Log.d(TAG, "ℹ️ SELECT event received (ignored)")
                        }
                    }
                }.launchIn(scope)

                // --- MEMBERS FLOW ---
                channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                    table = "trip_members"
                    filter = "trip_id=eq.$tripId"
                }.onEach { action ->
                    when (action) {
                        is PostgresAction.Insert -> {
                            val record = action.decodeRecord<TripMemberDto>()
                            Log.d(TAG, "🔔 MEMBER INSERT EVENT")
                            Log.d(TAG, "  Member: ${record.displayName}")
                            Log.d(TAG, "  Trip ID: ${record.tripId}")
                            Log.d(TAG, "  Current trip filter: $tripId")
                            //handleMemberInsert(record)
                            if (record.tripId == tripId) {
                                handleMemberInsert(record)
                                Log.d(TAG, "✅ Added member to local DB: ${record.displayName}")
                            } else {
                                Log.d(TAG, "ℹ️ Ignoring member from different trip")
                            }
                        }

                        is PostgresAction.Delete -> {
                            // .contentOrNull removes the surrounding quotes that .toString() leaves behind
                            val memberId = action.oldRecord["id"]?.jsonPrimitive?.contentOrNull
                            val deletedTripId = action.oldRecord["trip_id"]?.jsonPrimitive?.contentOrNull

                            if (memberId != null) {
                                // Now the comparison will actually work (e.g., "uuid" == "uuid")
                                if (deletedTripId == tripId || deletedTripId == null) {
                                    handleMemberDelete(memberId)
                                    Log.d(TAG, "🗑️ Member deleted: $memberId")
                                } else {
                                    Log.d(TAG, "ℹ️ Delete ignored: Belongs to trip $deletedTripId")
                                }
                            } else {
                                Log.e(TAG, "❌ DELETE event failed: 'id' key missing in oldRecord. Keys: ${action.oldRecord.keys}")
                            }
                        }
                        is PostgresAction.Update -> {
                            val record = action.decodeRecord<TripMemberDto>()
                            //handleMemberUpdate(record)
                            if (record.tripId == tripId) {
                                handleMemberUpdate(record)
                                Log.d(TAG, "✅ Updated member in local DB")
                            }
                        }
                        is PostgresAction.Select -> {
                            Log.d(TAG, "ℹ️ SELECT event received (ignored)")
                        }
                    }
                }.launchIn(scope)


                // --- SPLITS FLOW ---
                channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                    table = "expense_splits"
                    // Note: We can't filter by trip_id here easily because the table doesn't have it.
                    // Instead, we handle events if the expense_id exists in our local DB.
                }.onEach { action ->
                    when (action) {
                        is PostgresAction.Insert -> {
                            val record = action.decodeRecord<ExpenseSplitDto>()

                            // Check if expense UPDATE is pending
                            val isUpdatePending = updateMutex.withLock {
                                pendingExpenseUpdates.contains(record.expenseId)
                            }

                            if (isUpdatePending) {
                                Log.d(TAG, "⏳ Split INSERT ignored - UPDATE pending for expense: ${record.expenseId}")
                                return@onEach
                            }

                            handleSplitInsert(record)
                        }
                        is PostgresAction.Delete -> {
                            val splitId = action.oldRecord["id"] as? String
                            splitId?.let { expenseSplitDao.deleteSplit(it) }
                        }
                        // Insert/Update are handled by the handleExpenseUpdate transaction
                        is PostgresAction.Insert -> {
                            val record = action.decodeRecord<ExpenseSplitDto>()
                            handleSplitInsert(record)
                        }
                        is PostgresAction.Select -> {}
                        is PostgresAction.Update -> {}
                    }
                }.launchIn(scope)

                channel.subscribe()
                Log.d(TAG, "✅ Subscribed to channel: trip_$tripId")
                Log.d(TAG, "📡 Active channels: ${activeChannels.keys}")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Subscription failed", e)
                subscriptionMutex.withLock {
                    activeChannels.remove(tripId)
                }
            }
        }
    }

    fun unsubscribeFromTrip(tripId: String) {
        scope.launch {
            val channel = subscriptionMutex.withLock {
                activeChannels.remove(tripId)
            }
            channel?.let {
                try {
                    it.unsubscribe()
                    Log.d(TAG, "✅ Unsubscribed from trip: $tripId")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Unsubscribe failed", e)
                }
            }
        }
    }

    fun unsubscribeAll() {
        Log.d(TAG, "🔕 Unsubscribing from all trips")
        activeChannels.keys.toList().forEach { tripId ->
            unsubscribeFromTrip(tripId)
        }
    }

    // =====================================================
    // HANDLERS
    // =====================================================

    private suspend fun handleMemberInsert(memberDto: TripMemberDto) {

        val entity = memberDto.toEntity().copy(isSynced = true)
        tripMemberDao.insertMember(entity)
        val trip = tripDao.getTripById(entity.tripId)
        trip?.let {
            notificationHelper.onMemberAdded(
                memberName = entity.displayName,
                tripName = it.name,
                tripId = it.id
            )
        }
        Log.d(TAG, "✅ Realtime Sync: Updated member ${entity.displayName}")
    }

    private suspend fun handleMemberUpdate(memberDto: TripMemberDto) {
        try {
            val entity = memberDto.toEntity().copy(isSynced = true)
            tripMemberDao.updateMember(entity)
            Log.d(TAG, "✅ Member updated: ${entity.displayName}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to update member", e)
        }
    }

    private suspend fun handleMemberDelete(memberId: String) {
        try {
            val member = tripMemberDao.getMemberById(memberId)
            val trip = member?.let { tripDao.getTripById(it.tripId) }

            expenseSplitDao.deleteSplitsForMember(memberId)
            tripMemberDao.deleteMember(memberId)

            if (member != null && trip != null) {
                notificationHelper.onMemberRemoved(
                    memberName = member.displayName,
                    tripName = trip.name
                )
            }
            Log.d(TAG, "✅ Member deleted from Room")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to delete member", e)
        }
    }

    private suspend fun handleExpenseInsert(expenseDto: ExpenseDto) {
        try {
            val entity = expenseDto.toEntity().copy(
                isLocal = false,
                isSynced = true
            )

            val existing = expenseDao.getExpenseById(entity.id)
            if (existing == null) {
                expenseDao.insertAnExpense(entity)
                Log.d(TAG, "✅ Expense added to Room: ${entity.description}")

                val trip = tripDao.getTripById(entity.tripId)
                trip?.let {
                    notificationHelper.onExpenseAdded(
                        amount = entity.amount,
                        description = entity.description,
                        paidByName = entity.paidByName,
                        tripName = it.name,
                        tripId = it.id,
                        expenseId = entity.id
                    )
                }

                // Fetch and insert splits
                fetchAndInsertSplits(entity.id)
            } else {
                Log.d(TAG, "ℹ️ Expense already exists")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to insert expense", e)
        }
    }

//    private suspend fun handleExpenseUpdate(expenseDto: ExpenseDto) {
//        try {
//            val entity = expenseDto.toEntity().copy(isLocal = false, isSynced = true)
//
//            // 1. Fetch splits
//            val splitDtos = supabase.from("expense_splits")
//                .select { filter { eq("expense_id", entity.id) } }
//                .decodeList<ExpenseSplitDto>()
//
//            // 2. Ensure ALL members exist BEFORE transaction
//            val missingMembers = splitDtos.mapNotNull { split ->
//                val memberExists = tripMemberDao.getMemberById(split.memberId) != null
//                if (!memberExists) split.memberId else null
//            }
//
//            // Fetch ALL missing members synchronously
//            if (missingMembers.isNotEmpty()) {
//                Log.d(TAG, "⚠️ Missing ${missingMembers.size} members, fetching...")
//                missingMembers.forEach { memberId ->
//                    fetchAndSyncSingleMember(memberId)
//                }
//                Log.d(TAG, "✅ All missing members fetched")
//            }
//
//            val splitEntities = splitDtos.map { it.toEntity() }
//
//            // 3. Atomic Transaction (now safe)
//            expenseDao.updateExpenseAndSplits(entity, splitEntities)
//            delay(50)
//
//            Log.d(TAG, "✅ Atomic update complete for: ${entity.description}")
//        } catch (e: Exception) {
//            Log.e(TAG, "❌ Sync failed", e)
//        }
//    }

    private suspend fun handleExpenseUpdate(expenseDto: ExpenseDto) {
        try {
            Log.d(TAG, "🔄 Processing expense UPDATE: ${expenseDto.description}")

            val entity = expenseDto.toEntity().copy(isLocal = false, isSynced = true)

            // 1. Fetch NEW splits from Supabase
            val splitDtos = supabase.from("expense_splits")
                .select { filter { eq("expense_id", entity.id) } }
                .decodeList<ExpenseSplitDto>()

            Log.d(TAG, "📊 Fetched ${splitDtos.size} splits from Supabase")

            // 2. Ensure ALL members exist BEFORE transaction
            val missingMembers = splitDtos.mapNotNull { split ->
                val memberExists = tripMemberDao.getMemberById(split.memberId) != null
                if (!memberExists) split.memberId else null
            }

            if (missingMembers.isNotEmpty()) {
                Log.d(TAG, "⚠️ Missing ${missingMembers.size} members, fetching...")
                missingMembers.forEach { memberId ->
                    fetchAndSyncSingleMember(memberId)
                }
                Log.d(TAG, "✅ All missing members fetched")
            }

            // 3. DELETE ALL OLD SPLITS (before inserting new ones)
            Log.d(TAG, "🗑️ Deleting old splits for expense: ${entity.id}")
            expenseSplitDao.deleteSplitsForExpense(entity.id)

            // Small delay to ensure delete completes
            delay(50)

            // 4. Convert new splits to entities
            val splitEntities = splitDtos.map { it.toEntity() }

            // 5. Atomic Transaction (update expense + insert new splits)
            expenseDao.updateExpenseAndSplits(entity, splitEntities)

            val trip = tripDao.getTripById(entity.tripId)
            trip?.let {
                notificationHelper.onExpenseUpdated(
                    description = entity.description,
                    tripName = it.name,
                    tripId = it.id
                )
            }

            Log.d(TAG, "✅ Atomic update complete for: ${entity.description} (${splitEntities.size} splits)")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Expense update failed", e)
        }
    }

    private suspend fun fetchAndSyncSingleMember(memberId: String) {
        try {
            val memberDto = supabase.from("trip_members")
                .select { filter { eq("id", memberId) } }
                .decodeSingleOrNull<TripMemberDto>()

            memberDto?.let { tripMemberDao.insertMember(it.toEntity()) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch missing member", e)
        }
    }

    private suspend fun handleExpenseDelete(expenseId: String) {
        try {
            val expense = expenseDao.getExpenseById(expenseId)
            val trip = expense?.let { tripDao.getTripById(it.tripId) }

            expenseSplitDao.deleteSplitsForExpense(expenseId)
            expenseDao.deleteExpenseById(expenseId)

            if (expense != null && trip != null) {
                notificationHelper.onExpenseDeleted(
                    description = expense.description,
                    tripName = trip.name
                )
            }
            Log.d(TAG, "✅ Expense deleted from Room")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to delete expense", e)
        }
    }

    private suspend fun fetchAndInsertSplits(expenseId: String) {
        try {
            val splitDtos = supabase.from("expense_splits")
                .select {
                    filter {
                        eq("expense_id", expenseId)
                    }
                }
                .decodeList<ExpenseSplitDto>()

            splitDtos.forEach { dto ->
                val entity = dto.toEntity()
                expenseSplitDao.insertSplit(entity)
            }

            Log.d(TAG, "✅ Inserted ${splitDtos.size} splits for expense: $expenseId")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to fetch splits", e)
        }
    }

    private suspend fun handleSplitInsert(splitDto: ExpenseSplitDto) {
        try {
            // 1. Ensure the member exists locally so the Foreign Key doesn't fail
            val memberExists = tripMemberDao.getMemberById(splitDto.memberId) != null
            if (!memberExists) {
                fetchAndSyncSingleMember(splitDto.memberId)
            }

            val existingSplit = expenseSplitDao.getSplitById(splitDto.id)
            if (existingSplit != null) {
                Log.d(TAG, " Split already exists locally, skipping realtime insert")
                return
            }

            // 2. Insert the split
            val entity = splitDto.toEntity()
            expenseSplitDao.upsertSplit(entity)
            //expenseSplitDao.insertSplit(entity)

            Log.d(TAG, "✅ Realtime Split Sync: ${entity.memberName} owes ${entity.amountOwed}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to sync split via realtime", e)
        }
    }

    private suspend fun handleTripDelete(tripId: String) {
        try {
            Log.d(TAG, "🗑️ Handling trip deletion: $tripId")

            // Delete all related data in correct order (avoid FK violations)
            expenseSplitDao.deleteAllSplitsForTrip(tripId)
            expenseDao.deleteAllExpensesByTrip(tripId)
            tripMemberDao.deleteAllMembersForTrip(tripId)
            tripDao.deleteTripById(tripId)

            // Unsubscribe from realtime
            unsubscribeFromTrip(tripId)

            Log.d(TAG, "✅ Trip deleted locally and unsubscribed")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to handle trip deletion", e)
        }
    }

}

