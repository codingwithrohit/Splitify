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


    val tripId: String? = null,
    val expenseId: String? = null,
    val actionLabel: String? = null,
    val deepLinkRoute: String? = null,

    val actorName: String? = null
)

enum class NotificationType(
    val icon: ImageVector,
    val colorName: String
) {
    SUCCESS(Icons.Default.CheckCircle, "green"),
    ERROR(Icons.Default.Error, "red"),
    WARNING(Icons.Default.Warning, "orange"),
    INFO(Icons.Default.Info, "blue")
}

enum class NotificationSource {
    LOCAL_CREATE,
    LOCAL_UPDATE,
    LOCAL_DELETE,

    MEMBER_ADDED,
    MEMBER_LEFT,

    SYNC_SUCCESS,
    SYNC_FAILURE,

    REALTIME_TRIP_UPDATE,
    REALTIME_MEMBER_ADDED,
    REALTIME_MEMBER_REMOVED,
    REALTIME_EXPENSE_ADDED,
    REALTIME_EXPENSE_UPDATED,
    REALTIME_EXPENSE_DELETED,
    REALTIME_SETTLEMENT_CREATED,
    REALTIME_SETTLEMENT_CONFIRMED,

    SYSTEM_REMINDER,
    SYSTEM_ERROR
}


object NotificationTemplates {

    // ============================================
    // TRIP OPERATIONS
    // ============================================

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
        type = NotificationType.SUCCESS,
        source = NotificationSource.LOCAL_DELETE
    )

    // ============================================
    // MEMBER OPERATIONS (Enhanced)
    // ============================================

    // Local: You added someone
    fun memberAdded(memberName: String, tripName: String) = AppNotification(
        title = "Member Added",
        message = "$memberName joined \"$tripName\"",
        type = NotificationType.SUCCESS,
        source = NotificationSource.LOCAL_CREATE
    )


    fun memberAddedRemote(
        addedByName: String,
        memberName: String,
        tripName: String,
        tripId: String
    ) = AppNotification(
        title = "New Member in $tripName",
        message = "$addedByName added $memberName",
        type = NotificationType.INFO,
        source = NotificationSource.REALTIME_MEMBER_ADDED,
        tripId = tripId,
        actorName = addedByName,  // Store for later use
        actionLabel = "View",
        deepLinkRoute = "trip_detail/$tripId"
    )


    fun memberRemovedRemote(
        removedByName: String,
        memberName: String,
        tripName: String,
        tripId: String
    ) = AppNotification(
        title = "Member Removed",
        message = "$removedByName removed $memberName from \"$tripName\"",
        type = NotificationType.WARNING,
        source = NotificationSource.REALTIME_MEMBER_REMOVED,
        tripId = tripId,
        actorName = removedByName,
        actionLabel = "View",
        deepLinkRoute = "trip_detail/$tripId"
    )

    // ============================================
    // EXPENSE OPERATIONS (Enhanced)
    // ============================================

    // Local: You added expense
    fun expenseAdded(amount: Double, description: String) = AppNotification(
        title = "Expense Added",
        message = "₹${"%.2f".format(amount)} - $description",
        type = NotificationType.SUCCESS,
        source = NotificationSource.LOCAL_CREATE
    )

    fun expenseAddedRemote(
        addedByName: String,
        amount: Double,
        description: String,
        paidByName: String,
        tripName: String,
        tripId: String,
        expenseId: String
    ) = AppNotification(
        title = "New Expense in $tripName",
        message = "$addedByName added: $paidByName paid ₹${"%.2f".format(amount)} for $description",
        type = NotificationType.INFO,
        source = NotificationSource.REALTIME_EXPENSE_ADDED,
        tripId = tripId,
        expenseId = expenseId,
        actorName = addedByName,
        actionLabel = "View",
        deepLinkRoute = "trip_detail/$tripId"
    )

    // Local: You updated
    fun expenseUpdated(description: String) = AppNotification(
        title = "Expense Updated",
        message = "\"$description\" updated successfully",
        type = NotificationType.SUCCESS,
        source = NotificationSource.LOCAL_UPDATE
    )


    fun expenseUpdatedRemote(
        updatedByName: String,
        description: String,
        tripName: String,
        tripId: String
    ) = AppNotification(
        title = "Expense Updated",
        message = "$updatedByName modified \"$description\" in \"$tripName\"",
        type = NotificationType.INFO,
        source = NotificationSource.REALTIME_EXPENSE_UPDATED,
        tripId = tripId,
        actorName = updatedByName,
        actionLabel = "View",
        deepLinkRoute = "trip_detail/$tripId"
    )

    // Local: You deleted
    fun expenseDeleted(description: String) = AppNotification(
        title = "Expense Deleted",
        message = "\"$description\" removed",
        type = NotificationType.SUCCESS,
        source = NotificationSource.LOCAL_DELETE
    )


    fun expenseDeletedRemote(
        deletedByName: String,
        description: String,
        tripName: String,
        tripId: String
    ) = AppNotification(
        title = "Expense Deleted",
        message = "$deletedByName removed \"$description\" from \"$tripName\"",
        type = NotificationType.WARNING,
        source = NotificationSource.REALTIME_EXPENSE_DELETED,
        tripId = tripId,
        actorName = deletedByName,
        actionLabel = "View",
        deepLinkRoute = "trip_detail/$tripId"
    )

    // ============================================
    // SETTLEMENT OPERATIONS
    // ============================================

    fun settlementMarked(amount: Double, toName: String) = AppNotification(
        title = "Settlement Marked",
        message = "Marked ₹${"%.2f".format(amount)} paid to $toName",
        type = NotificationType.SUCCESS,
        source = NotificationSource.LOCAL_CREATE
    )

    fun settlementConfirmed(
        amount: Double,
        fromName: String,
        tripName: String,
        tripId: String
    ) = AppNotification(
        title = "Payment Received",
        message = "$fromName confirmed ₹${"%.2f".format(amount)} in \"$tripName\"",
        type = NotificationType.SUCCESS,
        source = NotificationSource.REALTIME_SETTLEMENT_CONFIRMED,
        tripId = tripId,
        actorName = fromName,
        actionLabel = "View",
        deepLinkRoute = "trip_detail/$tripId"
    )

    // ============================================
    // SYNC & SYSTEM
    // ============================================

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

    fun settlementReminder(count: Int, totalAmount: Double) = AppNotification(
        title = "Pending Settlements",
        message = "You have $count settlements totaling ₹${"%.2f".format(totalAmount)}",
        type = NotificationType.INFO,
        source = NotificationSource.SYSTEM_REMINDER,
        actionLabel = "Review"
    )

    // Generic Helpers
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