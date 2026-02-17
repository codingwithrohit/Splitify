package com.example.splitify

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.splitify.data.local.SessionManager
import com.example.splitify.data.sync.SyncManager
import com.example.splitify.domain.repository.AuthRepository
import com.example.splitify.presentation.navigation.SplitifyNavGraph
import com.example.splitify.presentation.theme.SplitifyTheme
import com.example.splitify.util.NotificationManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var sessionManager: SessionManager
    @Inject
    lateinit var syncManager: SyncManager
    @Inject
    lateinit var authRepository: AuthRepository
    @Inject
    lateinit var notificationManager: NotificationManager

    private val deepLinkUri = mutableStateOf<android.net.Uri?>(null)


    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        intent?.data?.let { deepLinkUri.value = it }

        setContent {
            SplitifyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    SplitifyNavGraph(
                        sessionManager = sessionManager,
                        authRepository = authRepository,
                        navController = navController,
                        notificationManager = notificationManager,
                        deepLinkUri = deepLinkUri.value
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.data?.let {
            android.util.Log.d("MainActivity", "onNewIntent URI: $it")
            deepLinkUri.value = it
        }
    }

}



