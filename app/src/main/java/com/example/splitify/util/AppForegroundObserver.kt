package com.example.splitify.util

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App Foreground Observer
 *
 * Detects if app is in foreground or background
 * Uses ProcessLifecycleOwner (observes entire app, not just single activity)
 *
 * NO Firebase needed - uses Android's built-in lifecycle
 */
@Singleton
class AppForegroundObserver @Inject constructor() : DefaultLifecycleObserver {

    private var isInForeground = false

    companion object {
        private const val TAG = "AppForeground"
    }

    init {
        // Register observer
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        Log.d(TAG, "✅ AppForegroundObserver initialized")
    }

    /**
     * Called when app comes to foreground
     */
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        isInForeground = true
        Log.d(TAG, "📱 App entered FOREGROUND")
    }

    /**
     * Called when app goes to background
     */
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        isInForeground = false
        Log.d(TAG, "🌙 App entered BACKGROUND")
    }

    /**
     * Check if app is currently in foreground
     */
    fun isAppInForeground(): Boolean {
        return isInForeground
    }
}

/**
 * USAGE IN NOTIFICATION LOGIC:
 *
 * if (appForegroundObserver.isAppInForeground()) {
 *     // Show in-app SnackBar
 * } else {
 *     // Show system notification (status bar)
 * }
 *
 * DIFFERENCE FROM ACTIVITY LIFECYCLE:
 *
 * Activity Lifecycle:
 * - Tracks SINGLE activity (MainActivity)
 * - onStart/onStop called when rotating, opening dialog, etc.
 * - Not reliable for "is app visible" check
 *
 * Process Lifecycle (this):
 * - Tracks ENTIRE app process
 * - onStart = app became visible (any activity)
 * - onStop = app went to background (all activities stopped)
 * - Perfect for "is app visible to user" check
 */