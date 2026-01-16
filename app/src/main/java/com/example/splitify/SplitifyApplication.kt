package com.example.splitify

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
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

        registerNetworkCallback()
    }

    private fun registerNetworkCallback() {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        connectivityManager.registerDefaultNetworkCallback(
            object : ConnectivityManager.NetworkCallback() {

                override fun onAvailable(network: Network) {
                    Log.d("SplitifyApp", "🌐 Network available → trigger immediate sync")
                    syncManager.triggerImmediateSync()
                }
            }
        )
    }

    override val workManagerConfiguration: Configuration

        get() {
            Log.d("SplitifyApp", "Configuring WorkManager. Factory null? ${!::workerFactory.isInitialized}")
            return Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build()
        }


}