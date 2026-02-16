package com.example.splitify.data.sync

import com.example.splitify.domain.model.NotificationTemplates
import com.example.splitify.util.NotificationManager

class RealtimeNotificationHelper(
    private val notificationManager: NotificationManager
) {
    fun onMemberAdded(memberName: String, tripName: String, tripId: String, addedByName: String) {
        notificationManager.showNotification(
            NotificationTemplates.memberAddedRemote(
                addedByName = addedByName,
                memberName = memberName,
                tripName = tripName,
                tripId = tripId
            )
        )
    }

    fun onMemberRemoved(memberName: String, tripName: String, tripId: String, removedByName: String) {
        notificationManager.showNotification(
            NotificationTemplates.memberRemovedRemote(
                removedByName = removedByName,
                memberName = memberName,
                tripName = tripName,
                tripId = tripId
            )
        )
    }

    fun onExpenseAdded(
        amount: Double,
        description: String,
        paidByName: String,
        tripName: String,
        tripId: String,
        expenseId: String,
        addedByName: String
    ) {
        notificationManager.showNotification(
            NotificationTemplates.expenseAddedRemote(
                addedByName = addedByName,
                amount = amount,
                description = description,
                paidByName = paidByName,
                tripName = tripName,
                tripId = tripId,
                expenseId = expenseId
            )
        )
    }

    fun onExpenseUpdated(description: String, tripName: String, tripId: String, updatedByName: String) {
        notificationManager.showNotification(
            NotificationTemplates.expenseUpdatedRemote(
                updatedByName = updatedByName,
                description = description,
                tripName = tripName,
                tripId = tripId
            )
        )
    }

    fun onExpenseDeleted(description: String, tripName: String, tripId: String, deletedByName: String) {
        notificationManager.showNotification(
            NotificationTemplates.expenseDeletedRemote(
                deletedByName = deletedByName,
                description = description,
                tripName = tripName,
                tripId = tripId
            )
        )
    }
}