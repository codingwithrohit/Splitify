package com.example.splitify.data.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.splitify.data.local.SessionManager
import com.example.splitify.data.repository.ExpenseRepositoryImpl
import com.example.splitify.data.repository.TripMemberRepositoryImpl
import com.example.splitify.data.repository.TripRepositoryImpl
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val supabase: SupabaseClient
) {

    companion object {
        private const val TAG = "SyncManager"
        private const val SYNC_WORK_NAME = "expense_tracker_sync"
        private const val PERIODIC_SYNC_INTERVAL_MINUTES = 15L
    }

    private val workManager = WorkManager.getInstance(context)

    fun schedulePeriodicSync() {
        Log.d(TAG, "📅 Scheduling periodic sync")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            PERIODIC_SYNC_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )

        Log.d(TAG, "✅ Periodic sync scheduled")
    }

    fun triggerImmediateSync() {
        Log.d(TAG, "⚡ Triggering immediate sync")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueue(syncRequest)
    }

    fun cancelSync() {
        Log.d(TAG, "🛑 Cancelling all sync")
        workManager.cancelUniqueWork(SYNC_WORK_NAME)
    }

    suspend fun isAuthenticated(): Boolean {
        return try {
            supabase.auth.currentSessionOrNull() != null
        } catch (e: Exception) {
            Log.e(TAG, "❌ Auth check failed", e)
            false
        }
    }
}


@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val tripRepository: TripRepositoryImpl,
    private val tripMemberRepository: TripMemberRepositoryImpl,
    private val expenseRepository: ExpenseRepositoryImpl,
    private val syncManager: SyncManager,
    private val sessionManager: SessionManager
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "SyncWorker"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "🔄 Starting sync...")

        if (!sessionManager.hasValidSession()) {
            Log.d(TAG, "❌ No session, skipping sync")
            return Result.success()
        }

        return try {
            val currentUserId = sessionManager.getCurrentUserId()
            if (currentUserId == null) {
                Log.e(TAG, " No user ID, aborting sync")
                return Result.success()
            }

            // 1. Sync all trips first
            when (val result = tripRepository.syncTrips()) {
                is com.example.splitify.util.Result.Success -> {
                    Log.d("SyncWorker", "✅ Trips synced")
                }
                is com.example.splitify.util.Result.Error -> {
                    Log.e("SyncWorker", "❌ Trip sync failed: ${result.message}")
                    return Result.retry()
                }
                else -> Unit
            }

            // 2. Get all trip IDs to sync their data
            val tripIds = tripRepository.getUserTripIds(currentUserId)
            Log.d(TAG, "📊 Found ${tripIds.size} trips to sync")

            // 3. Sync members and expenses for each trip
            tripIds.forEach { tripId ->
                syncTripData(tripId)
            }

            Log.d(TAG, "✅ Sync completed successfully")
            Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "❌ Sync failed", e)

            if (runAttemptCount < 3) {
                Log.d(TAG, "🔁 Will retry (attempt ${runAttemptCount + 1}/3)")
                Result.retry()
            } else {
                Log.e(TAG, "❌ Max retries reached")
                Result.failure()
            }
        }
    }

    private suspend fun syncTripData(tripId: String) {
        try {
            Log.d(TAG, "🔄 Syncing trip: $tripId")

            // Sync members first (expenses reference them)
            when (val memberResult = tripMemberRepository.syncUnsyncedMembers(tripId)) {
                is com.example.splitify.util.Result.Success -> {
                    Log.d("SyncWorker", "  ✅ Members synced")
                }
                is com.example.splitify.util.Result.Error -> {
                    Log.e("SyncWorker", "  ❌ Members sync failed: ${memberResult.message}")
                }
                else -> Unit
            }

            // Sync expenses
            when (val expenseResult = expenseRepository.syncUnsyncedExpenses(tripId)) {
                is com.example.splitify.util.Result.Success -> {
                    Log.d("SyncWorker", "  ✅ Expenses synced")
                }
                is com.example.splitify.util.Result.Error -> {
                    Log.e("SyncWorker", "  ❌ Expenses sync failed: ${expenseResult.message}")
                }
                else -> Unit
            }

            Log.d(TAG, "✅ Trip $tripId fully synced")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to sync trip $tripId", e)
            throw e
        }
    }
}