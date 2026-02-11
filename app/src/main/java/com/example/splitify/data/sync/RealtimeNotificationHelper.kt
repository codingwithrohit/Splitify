package com.example.splitify.data.sync

import android.util.Log
import com.example.splitify.domain.model.NotificationTemplates
import com.example.splitify.util.NotificationManager

/**
 * Extension functions to add notification support to RealtimeManager
 *
 * This file shows you how to integrate the notification system
 * with your existing realtime events.
 *
 * Pattern:
 * 1. Detect realtime event
 * 2. Create appropriate notification
 * 3. Show notification via NotificationManager
 */

/**
 * Helper class to store context needed for notifications
 * Add this as a property in RealtimeManager
 */
class RealtimeNotificationHelper(
    private val notificationManager: NotificationManager
) {
    private val TAG = "RealtimeNotifications"

    /**
     * Call this when a member is added via realtime
     */
    fun onMemberAdded(memberName: String, tripName: String, tripId: String) {
        Log.d(TAG, "📢 Member added notification: $memberName → $tripName")

        val notification = NotificationTemplates.memberAddedRemote(
            memberName = memberName,
            tripName = tripName,
            tripId = tripId
        )

        notificationManager.showNotification(notification)
    }

    /**
     * Call this when a member is removed via realtime
     */
    fun onMemberRemoved(memberName: String, tripName: String) {
        Log.d(TAG, "📢 Member removed notification: $memberName ← $tripName")

        val notification = NotificationTemplates.memberRemoved(
            memberName = memberName,
            tripName = tripName
        )

        notificationManager.showNotification(notification)
    }

    /**
     * Call this when an expense is added via realtime
     */
    fun onExpenseAdded(
        amount: Double,
        description: String,
        paidByName: String,
        tripName: String,
        tripId: String,
        expenseId: String
    ) {
        Log.d(TAG, "📢 Expense added notification: $description")

        val notification = NotificationTemplates.expenseAddedRemote(
            amount = amount,
            description = description,
            paidByName = paidByName,
            tripName = tripName,
            tripId = tripId,
            expenseId = expenseId
        )

        notificationManager.showNotification(notification)
    }

    /**
     * Call this when an expense is updated via realtime
     */
    fun onExpenseUpdated(
        description: String,
        tripName: String,
        tripId: String
    ) {
        Log.d(TAG, "📢 Expense updated notification: $description")

        val notification = NotificationTemplates.expenseUpdatedRemote(
            description = description,
            tripName = tripName,
            tripId = tripId
        )

        notificationManager.showNotification(notification)
    }

    /**
     * Call this when an expense is deleted via realtime
     */
    fun onExpenseDeleted(
        description: String,
        tripName: String
    ) {
        Log.d(TAG, "📢 Expense deleted notification: $description")

        val notification = NotificationTemplates.expenseDeletedRemote(
            description = description,
            tripName = tripName
        )

        notificationManager.showNotification(notification)
    }

    /**
     * Call this when a settlement is confirmed via realtime
     */
    fun onSettlementConfirmed(
        amount: Double,
        fromName: String,
        tripName: String,
        tripId: String
    ) {
        Log.d(TAG, "📢 Settlement confirmed notification")

        val notification = NotificationTemplates.settlementConfirmed(
            amount = amount,
            fromName = fromName,
            tripName = tripName,
            tripId = tripId
        )

        notificationManager.showNotification(notification)
    }
}

/**
 * INTEGRATION GUIDE FOR RealtimeManager.kt
 * ========================================
 *
 * Step 1: Add NotificationManager to constructor
 * ----------------------------------------------
 * @Singleton
 * class RealtimeManager @Inject constructor(
 *     private val supabase: SupabaseClient,
 *     private val tripDao: TripDao,
 *     private val tripMemberDao: TripMemberDao,
 *     private val expenseDao: ExpenseDao,
 *     private val expenseSplitDao: ExpenseSplitDao,
 *     private val notificationManager: NotificationManager  // ADD THIS
 * ) {
 *
 * Step 2: Create notification helper
 * -----------------------------------
 * private val notificationHelper = RealtimeNotificationHelper(notificationManager)
 *
 * Step 3: Add notifications to your handlers
 * ------------------------------------------
 *
 * Example for handleExpenseInsert:
 *
 * private suspend fun handleExpenseInsert(expenseDto: ExpenseDto) {
 *     try {
 *         val entity = expenseDto.toEntity().copy(isLocal = false, isSynced = true)
 *
 *         val existing = expenseDao.getExpenseById(entity.id)
 *         if (existing == null) {
 *             expenseDao.insertAnExpense(entity)
 *
 *             // FETCH TRIP NAME FOR NOTIFICATION
 *             val trip = tripDao.getTripById(entity.tripId)
 *
 *             // SHOW NOTIFICATION
 *             trip?.let {
 *                 notificationHelper.onExpenseAdded(
 *                     amount = entity.amount,
 *                     description = entity.description,
 *                     paidByName = entity.paidByName,
 *                     tripName = it.name,
 *                     tripId = it.id,
 *                     expenseId = entity.id
 *                 )
 *             }
 *
 *             fetchAndInsertSplits(entity.id)
 *         }
 *     } catch (e: Exception) {
 *         Log.e(TAG, "❌ Failed to insert expense", e)
 *     }
 * }
 *
 * Example for handleMemberInsert:
 *
 * private suspend fun handleMemberInsert(memberDto: TripMemberDto) {
 *     val entity = memberDto.toEntity().copy(isSynced = true)
 *     tripMemberDao.insertMember(entity)
 *
 *     // FETCH TRIP NAME
 *     val trip = tripDao.getTripById(entity.tripId)
 *
 *     // SHOW NOTIFICATION
 *     trip?.let {
 *         notificationHelper.onMemberAdded(
 *             memberName = entity.displayName,
 *             tripName = it.name,
 *             tripId = it.id
 *         )
 *     }
 * }
 *
 * Similar pattern for:
 * - handleMemberDelete() → onMemberRemoved()
 * - handleExpenseUpdate() → onExpenseUpdated()
 * - handleExpenseDelete() → onExpenseDeleted()
 */