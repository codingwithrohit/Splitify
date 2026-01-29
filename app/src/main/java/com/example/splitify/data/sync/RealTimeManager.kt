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
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.decodeOldRecord
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealtimeManager @Inject constructor(
    private val supabase: SupabaseClient,
    private val tripDao: TripDao,
    private val tripMemberDao: TripMemberDao,
    private val expenseDao: ExpenseDao,
    private val expenseSplitDao: ExpenseSplitDao
) {
    companion object {
        private const val TAG = "RealtimeManager"
        private var subscriptionCount = 0
    }

    private var globalChannelSubscribed = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeChannels = mutableMapOf<String, io.github.jan.supabase.realtime.RealtimeChannel>()


    suspend fun subscribeToUserTrips(userId: String) {
        if (globalChannelSubscribed) {
            Log.d(TAG, "⚠️ Already subscribed to global trips")
            return
        }

        val globalChannel = supabase.realtime.channel("global_trips")

        globalChannel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "trips"
        }.onEach { action ->
            if (action is PostgresAction.Delete) {
                val id = action.oldRecord["id"]?.toString()?.replace("\"", "")
                Log.d(TAG, "Attempting to delete trip from local DB: $id")
                id?.let { tripDao.deleteTripById(it) }
            }
        }.launchIn(scope)

        globalChannel.subscribe()
        activeChannels["global"] = globalChannel
        globalChannelSubscribed = true
        Log.d(TAG, "✅ Subscribed to global trips")
    }


    fun subscribeToTrip(tripId: String) {
        subscriptionCount++
        Log.d(TAG, "📊 Subscription attempt #$subscriptionCount for trip: $tripId")

        if (activeChannels.containsKey(tripId)) {
            Log.d(TAG, "⚠️ Already subscribed to trip: $tripId")
            return
        }

        scope.launch {
            try {
                val channel = supabase.realtime.channel("trip_$tripId")


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
                        is PostgresAction.Update -> {
                            val record = action.decodeRecord<ExpenseDto>()
                            handleExpenseUpdate(record)
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
                            handleMemberInsert(record)
                        }
                        is PostgresAction.Delete -> {
                            val memberId = action.oldRecord["id"]?.toString()?.replace("\"", "")

                            if (memberId != null) {
                                handleMemberDelete(memberId)
                                Log.d(TAG, "🗑️ Member deleted: $memberId")
                            } else {
                                Log.e(TAG, "❌ DELETE event missing member ID")
                            }
                        }
                        is PostgresAction.Update -> {
                            val record = action.decodeRecord<TripMemberDto>()
                            handleMemberUpdate(record)
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
                        is PostgresAction.Delete -> {
                            val splitId = action.oldRecord["id"] as? String
                            splitId?.let { expenseSplitDao.deleteSplit(it) }
                        }
                        // Insert/Update are handled by the handleExpenseUpdate transaction
                        is PostgresAction.Insert -> {}
                        is PostgresAction.Select -> {}
                        is PostgresAction.Update -> {}
                    }
                }.launchIn(scope)

                channel.subscribe()
                activeChannels[tripId] = channel
                Log.d(TAG, "✅ Flow-based subscription active for: $tripId")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Subscription failed", e)
                activeChannels.remove(tripId)
            }
        }
    }

    fun unsubscribeFromTrip(tripId: String) {
        Log.d(TAG, "🔕 Unsubscribing from trip: $tripId")

        activeChannels[tripId]?.let { channel ->
            scope.launch {
                try {
                    supabase.realtime.removeChannel(channel)
                    activeChannels.remove(tripId)
                    Log.d(TAG, "✅ Unsubscribed from trip: $tripId")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Failed to unsubscribe", e)
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
        try {
            val entity = memberDto.toEntity().copy(isSynced = true)

            val existing = tripMemberDao.getMemberById(entity.id)
            if (existing == null) {
                tripMemberDao.insertMember(entity)
                Log.d(TAG, "✅ Member added to Room: ${entity.displayName}")
            } else {
                Log.d(TAG, "ℹ️ Member already exists")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to insert member", e)
        }
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
            expenseSplitDao.deleteSplitsForMember(memberId)
            tripMemberDao.deleteMember(memberId)
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

                // Fetch and insert splits
                fetchAndInsertSplits(entity.id)
            } else {
                Log.d(TAG, "ℹ️ Expense already exists")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to insert expense", e)
        }
    }

    private suspend fun handleExpenseUpdate(expenseDto: ExpenseDto) {
        try {
            val entity = expenseDto.toEntity().copy(isLocal = false, isSynced = true)

            // 1. Fetch splits
            val splitDtos = supabase.from("expense_splits")
                .select { filter { eq("expense_id", entity.id) } }
                .decodeList<ExpenseSplitDto>()

            // 2. Ensure ALL members exist BEFORE transaction
            val missingMembers = splitDtos.mapNotNull { split ->
                val memberExists = tripMemberDao.getMemberById(split.memberId) != null
                if (!memberExists) split.memberId else null
            }

            // Fetch ALL missing members synchronously
            if (missingMembers.isNotEmpty()) {
                Log.d(TAG, "⚠️ Missing ${missingMembers.size} members, fetching...")
                missingMembers.forEach { memberId ->
                    fetchAndSyncSingleMember(memberId)
                }
                Log.d(TAG, "✅ All missing members fetched")
            }

            val splitEntities = splitDtos.map { it.toEntity() }

            // 3. Atomic Transaction (now safe)
            expenseDao.updateExpenseAndSplits(entity, splitEntities)

            Log.d(TAG, "✅ Atomic update complete for: ${entity.description}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Sync failed", e)
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
            expenseSplitDao.deleteSplitsForExpense(expenseId)
            expenseDao.deleteExpenseById(expenseId)
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
}