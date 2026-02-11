package com.example.splitify.util

import android.util.Log
import com.example.splitify.domain.model.AppNotification
import com.example.splitify.domain.model.NotificationSource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized notification manager for the entire app
 *
 * ADDED: Detailed logging to trace notification flow
 */
@Singleton
class NotificationManager @Inject constructor() {

    companion object {
        private const val TAG = "NotificationManager"
        private const val MAX_HISTORY_SIZE = 50
    }

    // SharedFlow for one-time events (SnackBars)
    // replay = 0 means only new subscribers get new notifications
    private val _notificationEvent = MutableSharedFlow<AppNotification>(
        replay = 1,
        extraBufferCapacity = 10
    )
    val notificationEvent: SharedFlow<AppNotification> = _notificationEvent.asSharedFlow()

    // StateFlow for notification history
    private val _notificationHistory = MutableStateFlow<List<AppNotification>>(emptyList())
    val notificationHistory: StateFlow<List<AppNotification>> = _notificationHistory.asStateFlow()

    /**
     * Show a notification immediately
     */
    fun showNotification(notification: AppNotification) {
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "📢 showNotification() CALLED")
        Log.d(TAG, "  Title: ${notification.title}")
        Log.d(TAG, "  Message: ${notification.message}")
        Log.d(TAG, "  Type: ${notification.type}")
        Log.d(TAG, "  Source: ${notification.source}")
        Log.d(TAG, "  ID: ${notification.id}")

        // Emit for immediate display
        val emitted = _notificationEvent.tryEmit(notification)

        if (emitted) {
            Log.d(TAG, "  ✅ Notification EMITTED to flow successfully")
            Log.d(TAG, "  📊 Subscription count: ${_notificationEvent.subscriptionCount.value}")
        } else {
            Log.e(TAG, "  ❌ FAILED to emit notification!")
            Log.e(TAG, "  Buffer might be full or no collectors")
        }

        // Add to history
        addToHistory(notification)
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    /**
     * Convenience methods for common notifications
     */
    fun showSuccess(title: String, message: String) {
        Log.d(TAG, "🎉 showSuccess() called: $title")
        showNotification(
            AppNotification(
                title = title,
                message = message,
                type = com.example.splitify.domain.model.NotificationType.SUCCESS,
                source = NotificationSource.LOCAL_CREATE
            )
        )
    }

    fun showError(title: String, message: String) {
        Log.e(TAG, "❌ showError() called: $title")
        showNotification(
            AppNotification(
                title = title,
                message = message,
                type = com.example.splitify.domain.model.NotificationType.ERROR,
                source = NotificationSource.SYSTEM_ERROR
            )
        )
    }

    fun showInfo(title: String, message: String) {
        Log.d(TAG, "ℹ️ showInfo() called: $title")
        showNotification(
            AppNotification(
                title = title,
                message = message,
                type = com.example.splitify.domain.model.NotificationType.INFO,
                source = NotificationSource.SYSTEM_REMINDER
            )
        )
    }

    fun showWarning(title: String, message: String) {
        Log.w(TAG, "⚠️ showWarning() called: $title")
        showNotification(
            AppNotification(
                title = title,
                message = message,
                type = com.example.splitify.domain.model.NotificationType.WARNING,
                source = NotificationSource.SYSTEM_ERROR
            )
        )
    }

    /**
     * Add notification to history
     */
    private fun addToHistory(notification: AppNotification) {
        val currentHistory = _notificationHistory.value.toMutableList()

        // Add to beginning (newest first)
        currentHistory.add(0, notification)

        // Keep only last N notifications
        if (currentHistory.size > MAX_HISTORY_SIZE) {
            currentHistory.removeAt(currentHistory.lastIndex)
        }

        _notificationHistory.value = currentHistory
        Log.d(TAG, "  📝 Added to history (total: ${currentHistory.size})")
    }

    /**
     * Mark a notification as read
     */
    fun markAsRead(notificationId: String) {
        val updatedHistory = _notificationHistory.value.map { notification ->
            if (notification.id == notificationId) {
                notification.copy(isRead = true)
            } else {
                notification
            }
        }
        _notificationHistory.value = updatedHistory
    }

    /**
     * Mark all notifications as read
     */
    fun markAllAsRead() {
        val updatedHistory = _notificationHistory.value.map { it.copy(isRead = true) }
        _notificationHistory.value = updatedHistory
    }

    /**
     * Clear all notification history
     */
    fun clearHistory() {
        _notificationHistory.value = emptyList()
        Log.d(TAG, "🗑️ Notification history cleared")
    }

    /**
     * Get unread notification count
     */
    fun getUnreadCount(): Int {
        return _notificationHistory.value.count { !it.isRead }
    }
}