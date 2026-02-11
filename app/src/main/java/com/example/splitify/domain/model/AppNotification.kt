package com.example.splitify.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector


data class AppNotification(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val type: NotificationType,
    val source: NotificationSource,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,

    // Optional fields for rich notifications
    val tripId: String? = null,
    val expenseId: String? = null,
    val actionLabel: String? = null,
    val deepLinkRoute: String? = null
)

enum class NotificationType(
    val icon: ImageVector,
    val colorName: String // We'll map this to actual colors in UI
) {
    SUCCESS(Icons.Default.CheckCircle, "green"),
    ERROR(Icons.Default.Error, "red"),
    WARNING(Icons.Default.Warning, "orange"),
    INFO(Icons.Default.Info, "blue")
}

enum class NotificationSource {
    // Local operations (no network involved)
    LOCAL_CREATE,
    LOCAL_UPDATE,
    LOCAL_DELETE,


    MEMBER_ADDED,
    MEMBER_LEFT,
    // Sync operations (background sync succeeded/failed)
    SYNC_SUCCESS,
    SYNC_FAILURE,

    // Real-time events (from Supabase Realtime)
    REALTIME_TRIP_UPDATE,
    REALTIME_MEMBER_ADDED,
    REALTIME_MEMBER_REMOVED,
    REALTIME_EXPENSE_ADDED,
    REALTIME_EXPENSE_UPDATED,
    REALTIME_EXPENSE_DELETED,
    REALTIME_SETTLEMENT_CREATED,
    REALTIME_SETTLEMENT_CONFIRMED,

    // System notifications
    SYSTEM_REMINDER,
    SYSTEM_ERROR
}


object NotificationTemplates {

    // Trip operations
    fun tripCreated(tripName: String) = AppNotification(
        title = "Trip Created",
        message = "\"$tripName\" is ready to track expenses!",
        type = NotificationType.SUCCESS,
        source = NotificationSource.LOCAL_CREATE
    )

    fun tripUpdated(tripName: String) = AppNotification(
        title = "Trip Updated",
        message = "Changes to \"$tripName\" saved successfully",
        type = NotificationType.SUCCESS,
        source = NotificationSource.LOCAL_UPDATE
    )

    fun tripDeleted(tripName: String) = AppNotification(
        title = "Trip Deleted",
        message = "\"$tripName\" and all expenses removed",
        type = NotificationType.INFO,
        source = NotificationSource.LOCAL_DELETE
    )

    // Member operations
    fun memberAdded(memberName: String, tripName: String) = AppNotification(
        title = "Member Added",
        message = "$memberName joined \"$tripName\"",
        type = NotificationType.SUCCESS,
        source = NotificationSource.LOCAL_CREATE
    )

    fun memberAddedRemote(memberName: String, tripName: String, tripId: String) = AppNotification(
        title = "New Member",
        message = "$memberName joined \"$tripName\"",
        type = NotificationType.INFO,
        source = NotificationSource.REALTIME_MEMBER_ADDED,
        tripId = tripId,
        actionLabel = "View",
        deepLinkRoute = "trip_detail/$tripId"
    )

    fun memberRemoved(memberName: String, tripName: String) = AppNotification(
        title = "Member Removed",
        message = "$memberName left \"$tripName\"",
        type = NotificationType.INFO,
        source = NotificationSource.REALTIME_MEMBER_REMOVED
    )

    // Expense operations
    fun expenseAdded(amount: Double, description: String) = AppNotification(
        title = "Expense Added",
        message = "₹${"%.2f".format(amount)} - $description",
        type = NotificationType.SUCCESS,
        source = NotificationSource.LOCAL_CREATE
    )

    fun expenseAddedRemote(
        amount: Double,
        description: String,
        paidByName: String,
        tripName: String,
        tripId: String,
        expenseId: String
    ) = AppNotification(
        title = "New Expense in $tripName",
        message = "$paidByName paid ₹${"%.2f".format(amount)} for $description",
        type = NotificationType.INFO,
        source = NotificationSource.REALTIME_EXPENSE_ADDED,
        tripId = tripId,
        expenseId = expenseId,
        actionLabel = "View",
        deepLinkRoute = "trip_detail/$tripId"
    )

    fun expenseUpdated(description: String) = AppNotification(
        title = "Expense Updated",
        message = "\"$description\" updated successfully",
        type = NotificationType.SUCCESS,
        source = NotificationSource.LOCAL_UPDATE
    )

    fun expenseUpdatedRemote(
        description: String,
        tripName: String,
        tripId: String
    ) = AppNotification(
        title = "Expense Updated",
        message = "\"$description\" was modified in \"$tripName\"",
        type = NotificationType.INFO,
        source = NotificationSource.REALTIME_EXPENSE_UPDATED,
        tripId = tripId,
        actionLabel = "View",
        deepLinkRoute = "trip_detail/$tripId"
    )

    fun expenseDeleted(description: String) = AppNotification(
        title = "Expense Deleted",
        message = "\"$description\" removed",
        type = NotificationType.INFO,
        source = NotificationSource.LOCAL_DELETE
    )

    fun expenseDeletedRemote(
        description: String,
        tripName: String
    ) = AppNotification(
        title = "Expense Deleted",
        message = "\"$description\" was removed from \"$tripName\"",
        type = NotificationType.WARNING,
        source = NotificationSource.REALTIME_EXPENSE_DELETED
    )

    // Settlement operations
    fun settlementMarked(amount: Double, toName: String) = AppNotification(
        title = "Settlement Marked",
        message = "Marked ₹${"%.2f".format(amount)} paid to $toName",
        type = NotificationType.SUCCESS,
        source = NotificationSource.LOCAL_CREATE
    )

    fun settlementConfirmed(amount: Double, fromName: String, tripName: String, tripId: String) = AppNotification(
        title = "Payment Received",
        message = "$fromName confirmed ₹${"%.2f".format(amount)} in \"$tripName\"",
        type = NotificationType.SUCCESS,
        source = NotificationSource.REALTIME_SETTLEMENT_CONFIRMED,
        tripId = tripId,
        actionLabel = "View",
        deepLinkRoute = "trip_detail/$tripId"
    )

    // Sync operations
    fun syncSuccess(itemCount: Int) = AppNotification(
        title = "Sync Complete",
        message = "$itemCount items synced successfully",
        type = NotificationType.SUCCESS,
        source = NotificationSource.SYNC_SUCCESS
    )

    fun syncFailure(errorMessage: String) = AppNotification(
        title = "Sync Failed",
        message = errorMessage,
        type = NotificationType.ERROR,
        source = NotificationSource.SYNC_FAILURE
    )

    // Error notifications
    fun networkError() = AppNotification(
        title = "No Internet",
        message = "Changes will sync when online",
        type = NotificationType.WARNING,
        source = NotificationSource.SYSTEM_ERROR
    )

    fun genericError(message: String) = AppNotification(
        title = "Error",
        message = message,
        type = NotificationType.ERROR,
        source = NotificationSource.SYSTEM_ERROR
    )

    // System reminders
    fun settlementReminder(count: Int, totalAmount: Double) = AppNotification(
        title = "Pending Settlements",
        message = "You have $count settlements totaling ₹${"%.2f".format(totalAmount)}",
        type = NotificationType.INFO,
        source = NotificationSource.SYSTEM_REMINDER,
        actionLabel = "Review"
    )

    // Generic Helpers for Custom Messages
    fun success(title: String, message: String) = AppNotification(
        title = title,
        message = message,
        type = NotificationType.SUCCESS,
        source = NotificationSource.LOCAL_CREATE
    )

    fun error(title: String, message: String) = AppNotification(
        title = title,
        message = message,
        type = NotificationType.ERROR,
        source = NotificationSource.SYSTEM_ERROR
    )

    fun warning(title: String, message: String) = AppNotification(
        title = title,
        message = message,
        type = NotificationType.WARNING,
        source = NotificationSource.SYSTEM_ERROR
    )
}