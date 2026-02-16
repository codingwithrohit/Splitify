package com.example.splitify.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.splitify.MainActivity
import com.example.splitify.R
import com.example.splitify.domain.model.AppNotification
import com.example.splitify.domain.model.NotificationType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * System Notification Manager - Phase 2
 *
 * Shows Android system tray notifications (like WhatsApp)
 * NO Firebase needed - uses Android's NotificationManager + Supabase Realtime
 */
@Singleton
class SystemNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "SystemNotif"

        // Notification Channels
        private const val CHANNEL_REALTIME = "realtime_events"
        private const val CHANNEL_SETTLEMENTS = "settlements"
        private const val CHANNEL_SYNC = "sync_status"

        // Notification IDs
        private const val NOTIF_ID_MEMBER = 1001
        private const val NOTIF_ID_EXPENSE = 1002
        private const val NOTIF_ID_SETTLEMENT = 1003

        // Intent extras
        const val EXTRA_TRIP_ID = "extra_trip_id"
        const val EXTRA_EXPENSE_ID = "extra_expense_id"
    }

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createNotificationChannels()
    }

    /**
     * Create notification channels (Android 8.0+)
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_REALTIME,
                    "Trip Updates",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Real-time updates when others modify shared trips"
                    enableLights(true)
                    lightColor = android.graphics.Color.BLUE
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 250, 250, 250)
                },

                NotificationChannel(
                    CHANNEL_SETTLEMENTS,
                    "Settlements",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Payment confirmations"
                    enableLights(true)
                    lightColor = android.graphics.Color.GREEN
                },

                NotificationChannel(
                    CHANNEL_SYNC,
                    "Sync Status",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Background sync progress"
                    setShowBadge(false)
                }
            )

            val systemManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            channels.forEach { systemManager.createNotificationChannel(it) }

            Log.d(TAG, "✅ Created ${channels.size} notification channels")
        }
    }

    /**
     * Show system notification
     */
    fun showSystemNotification(notification: AppNotification) {
        // Check permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w(TAG, "⚠️ Notification permission not granted")
                return
            }
        }

        val (channelId, notifId) = getChannelAndId(notification)

        // Create tap intent
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            notification.tripId?.let { putExtra(EXTRA_TRIP_ID, it) }
            notification.expenseId?.let { putExtra(EXTRA_EXPENSE_ID, it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notification.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(notification.title)
            .setContentText(notification.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notification.message))
            .setColor(getColor(notification.type))
            .setPriority(getPriority(notification.type))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        // Add action button
        notification.actionLabel?.let { label ->
            builder.addAction(0, label, pendingIntent)
        }

        // Show
        try {
            notificationManager.notify(notifId, builder.build())
            Log.d(TAG, "📲 System notification shown: ${notification.title}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to show notification", e)
        }
    }

    private fun getChannelAndId(notification: AppNotification): Pair<String, Int> {
        return when (notification.source) {
            com.example.splitify.domain.model.NotificationSource.REALTIME_MEMBER_ADDED,
            com.example.splitify.domain.model.NotificationSource.REALTIME_MEMBER_REMOVED ->
                CHANNEL_REALTIME to NOTIF_ID_MEMBER

            com.example.splitify.domain.model.NotificationSource.REALTIME_EXPENSE_ADDED,
            com.example.splitify.domain.model.NotificationSource.REALTIME_EXPENSE_UPDATED,
            com.example.splitify.domain.model.NotificationSource.REALTIME_EXPENSE_DELETED ->
                CHANNEL_REALTIME to NOTIF_ID_EXPENSE

            com.example.splitify.domain.model.NotificationSource.REALTIME_SETTLEMENT_CREATED,
            com.example.splitify.domain.model.NotificationSource.REALTIME_SETTLEMENT_CONFIRMED ->
                CHANNEL_SETTLEMENTS to NOTIF_ID_SETTLEMENT

            else -> CHANNEL_REALTIME to NOTIF_ID_EXPENSE
        }
    }

    private fun getColor(type: NotificationType): Int {
        return when (type) {
            NotificationType.SUCCESS -> android.graphics.Color.parseColor("#10B981")
            NotificationType.ERROR -> android.graphics.Color.parseColor("#EF4444")
            NotificationType.WARNING -> android.graphics.Color.parseColor("#F59E0B")
            NotificationType.INFO -> android.graphics.Color.parseColor("#6366F1")
        }
    }

    private fun getPriority(type: NotificationType): Int {
        return when (type) {
            NotificationType.ERROR -> NotificationCompat.PRIORITY_HIGH
            else -> NotificationCompat.PRIORITY_DEFAULT
        }
    }

    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
}