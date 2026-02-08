package com.example.splitify

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.splitify.data.local.SessionManager
import com.example.splitify.data.local.dao.TripDao
import com.example.splitify.data.sync.RealtimeManager
import com.example.splitify.data.sync.SyncManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class SplitifyApplication: Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    @Inject
    lateinit var syncManager: SyncManager

    @Inject
    lateinit var realtimeManager: RealtimeManager
    @Inject
    lateinit var sessionManager: SessionManager
    @Inject
    lateinit var tripDao: TripDao

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default )


    override fun onCreate() {
        super.onCreate()

        syncManager.schedulePeriodicSync()

        registerNetworkCallback()


    }


//    private fun observeLoginState(){
//        appScope.launch {
//            sessionManager.getCurrentUserFlow().collect { userSession ->
//                if(userSession != null){
//                    subscribeToUserTrips(userSession.userId)
//                }
//                else {
//                    //User logged out → unsubscribe all
//                    realtimeManager.unsubscribeAll()
//                    Log.d("SplitifyApp", "🔕 Unsubscribed from all trips (logout)")
//                }
//
//            }
//        }
//    }
//
//    private suspend fun subscribeToUserTrips(userId: String){
//        try {
//            // Get all trip IDs for current user
//            val tripIds = tripDao.getTripIdsByUser(userId)
//
//            Log.d("SplitifyApp", "🔔 Subscribing to ${tripIds.size} trips for user: $userId")
//
//            //Subscribe to each trip
//            tripIds.forEach { tripId ->
//                realtimeManager.subscribeToTrip(tripId)
//            }
//
//            Log.d("SplitifyApp", "✅ Global subscriptions active")
//        } catch (e: Exception) {
//            Log.e("SplitifyApp", "❌ Failed to subscribe to trips", e)
//        }
//    }

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