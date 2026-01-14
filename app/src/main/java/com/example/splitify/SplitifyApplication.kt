package com.example.splitify

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.splitify.data.sync.SyncManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SplitifyApplication: Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    @Inject
    lateinit var syncManager: SyncManager

    override fun onCreate() {
        super.onCreate()

        syncManager.schedulePeriodicSync()
    }

    override val workManagerConfiguration: Configuration

        get() {
            Log.d("SplitifyApp", "Configuring WorkManager. Factory null? ${!::workerFactory.isInitialized}")
            return Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build()
        }

}