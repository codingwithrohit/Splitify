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
class NotificationManager @Inject constructor(
    private val systemNotificationManager: SystemNotificationManager,
    private val appForegroundObserver: AppForegroundObserver
) {

    companion object {
        private const val TAG = "NotificationManager"
        private const val MAX_HISTORY_SIZE = 50
    }
    private var currentActiveTripId: String? = null

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

    fun setActiveTripId(tripId: String?) {
        currentActiveTripId = tripId
        Log.d(TAG, "🎯 Active trip: $tripId")
    }

    fun showNotification(notification: AppNotification) {
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "📢 showNotification()")
        Log.d(TAG, "Type: ${notification.type}")
        Log.d(TAG, "   Title: ${notification.title}")
        Log.d(TAG, "   Source: ${notification.source}")
        Log.d(TAG, "   Trip: ${notification.tripId}")
        Log.d(TAG, "   Active trip: $currentActiveTripId")
        Log.d(TAG, "   Foreground: ${appForegroundObserver.isAppInForeground()}")

        val showInApp = shouldShowInApp(notification)
        val showSystem = shouldShowSystem(notification)

        Log.d(TAG, "   → InApp=$showInApp, System=$showSystem")

        // In-app notification
        if (showInApp) {
            _notificationEvent.tryEmit(notification)
            Log.d(TAG, "   ✅ In-app shown")
        }

        // System notification
        if (showSystem) {
            systemNotificationManager.showSystemNotification(notification)
            Log.d(TAG, "   📲 System shown")
        }

        addToHistory(notification)
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    /**
     * Show in-app if:
     * - Local operation (always)
     * - Viewing the exact trip (user is looking at it)
     * - App in foreground (even different trip - user will see)
     */
    private fun shouldShowInApp(notification: AppNotification): Boolean {
        return when {
            // Local = always in-app
            notification.source in listOf(
                NotificationSource.LOCAL_CREATE,
                NotificationSource.LOCAL_UPDATE,
                NotificationSource.LOCAL_DELETE
            ) -> true

            // Viewing this exact trip = in-app only
            notification.tripId == currentActiveTripId -> true

            // Sync = in-app only
            notification.source in listOf(
                NotificationSource.SYNC_SUCCESS,
                NotificationSource.SYNC_FAILURE
            ) -> true

            // Foreground = show in-app too
            appForegroundObserver.isAppInForeground() -> true

            else -> false
        }
    }

    /**
     * Show system if:
     * - Realtime event AND (background OR different trip)
     */
    private fun shouldShowSystem(notification: AppNotification): Boolean {
        return when {
            // Never system for local/sync
            notification.source in listOf(
                NotificationSource.LOCAL_CREATE,
                NotificationSource.LOCAL_UPDATE,
                NotificationSource.LOCAL_DELETE,
                NotificationSource.SYNC_SUCCESS,
                NotificationSource.SYNC_FAILURE
            ) -> false

            // Realtime events:
            notification.source.name.startsWith("REALTIME_") -> {
                when {
                    // Background = always system
                    !appForegroundObserver.isAppInForeground() -> true

                    // Foreground but different trip = system (user needs to know)
                    notification.tripId != currentActiveTripId -> true

                    // Same trip = no system (in-app is enough)
                    else -> false
                }
            }

            else -> false
        }
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