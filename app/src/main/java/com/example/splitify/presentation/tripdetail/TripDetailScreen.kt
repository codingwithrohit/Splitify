package com.example.splitify.presentation.tripdetail

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.splitify.presentation.components.ErrorStateWithRetry
import com.example.splitify.presentation.components.LoadingScreen
import com.example.splitify.presentation.components.SplitifyAppBar
import com.example.splitify.presentation.theme.NeutralColors
import com.example.splitify.presentation.theme.PrimaryColors
import com.example.splitify.presentation.tripdetail.cards.InsightsPreviewCard
import com.example.splitify.presentation.tripdetail.cards.MembersCard
import com.example.splitify.presentation.tripdetail.cards.QuickBalanceCard
import com.example.splitify.presentation.tripdetail.cards.RecentExpensesCard
import com.example.splitify.presentation.tripdetail.cards.SettlementsCard
import com.example.splitify.presentation.tripdetail.cards.TripInfoCard
import com.example.splitify.util.NotificationManager

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    tripId: String,
    onNavigateBack: () -> Unit,
    onNavigateToExpense: (String?, String?, String?) -> Unit,
    onNavigateToMembers: () -> Unit,
    onNavigateToBalances: (String) -> Unit,
    onNavigateToInsights: () -> Unit,
    onNavigateToSettlement: (String, String) -> Unit,
    onAddExpense: () -> Unit,
    onAddMembers: () -> Unit,
    onNavigateToEditTrip: (String) -> Unit,
    navController: NavController,
    viewModel: TripDetailViewModel = hiltViewModel(),
    notificationManager: NotificationManager
){

    val dashboardState by viewModel.dashboardState.collectAsStateWithLifecycle()

    DisposableEffect(tripId) {
        notificationManager.setActiveTripId(tripId)

        onDispose {
            notificationManager.setActiveTripId(null)
        }
    }

    LaunchedEffect(dashboardState) {
        Log.d("TripDetailScreen", "📊 State Changed: ${dashboardState::class.simpleName}")
        when (dashboardState) {
            is TripDashboardState.Loading -> {
                Log.d("TripDetailScreen", "  Status: Loading...")
            }
            is TripDashboardState.Success -> {
                val s = dashboardState as TripDashboardState.Success
                Log.d("TripDetailScreen", "  Status: Success")
                Log.d("TripDetailScreen", "  Trip: ${s.trip.name}")
                Log.d("TripDetailScreen", "  Members: ${s.members.size}")
                Log.d("TripDetailScreen", "  Expenses: ${s.expenseCount}")
            }
            is TripDashboardState.Error -> {
                val s = dashboardState as TripDashboardState.Error
                Log.e("TripDetailScreen", "  Status: Error - ${s.message}")
            }

            TripDashboardState.Deleted -> {

            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            SplitifyAppBar(
                title = when (val state = dashboardState) {
                    is TripDashboardState.Success -> state.trip.name
                    else -> "Trip Details"
                },
                onBackClick = onNavigateBack
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddExpense,
                icon = { Icon(Icons.Default.Add, "Add expense") },
                text = { Text("New Expense", fontWeight = FontWeight.Bold) },
                containerColor = PrimaryColors.Primary600,
                contentColor = Color.White,
                shape = CircleShape
            )
        }
    ) { paddingValues ->
        when(val state = dashboardState){
            is TripDashboardState.Loading ->{
                LoadingScreen(
                    message = "Loading trip details...",
                    modifier = Modifier.padding(paddingValues))
            }
            is TripDashboardState.Error -> {
                ErrorStateWithRetry(
                    message = state.message,
                    onRetry = {  },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is TripDashboardState.Success -> {
                TripDashboard(
                    state = state,
                    onNavigateToExpenses = onNavigateToExpense,
                    onNavigateToMembers = onNavigateToMembers,
                    onNavigateToBalances = onNavigateToBalances,
                    onNavigateToInsights = onNavigateToInsights,
                    onNavigateToSettlement = onNavigateToSettlement,
                    onNavigateToEditTrip = onNavigateToEditTrip,
                    onAddMembers = onAddMembers,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            TripDashboardState.Deleted -> {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Trip Deleted",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This trip has been deleted by the admin",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { navController.navigateUp() }) {
                            Text("Go Back to Trips")
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun TripDashboard(
    state: TripDashboardState.Success,
    onNavigateToExpenses: (tripId: String?, currentUserId: String?, currentMemberId: String?) -> Unit,
    onNavigateToBalances: (currentMemberId: String) -> Unit,
    onNavigateToMembers: () -> Unit,
    onNavigateToInsights: () -> Unit,
    onNavigateToSettlement: (tripId: String, currentMemberId: String) -> Unit,
    onNavigateToEditTrip: (String) -> Unit,
    onAddMembers: () -> Unit,
    modifier: Modifier = Modifier
){
    val currentUserId = state.currentUserId
    val currentMemberId = state.members.firstOrNull(){it.userId == currentUserId}?.id

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    ) {
        item { Spacer(modifier = Modifier.height(20.dp)) }

        item {
            TripInfoCard(
                trip = state.trip,
                memberCount = state.members.size,
                totalAmount = state.totalAmount,
                onClick = { state.trip.id.let { onNavigateToEditTrip(it) }}
            )
        }

        item { Spacer(modifier = Modifier.height(18.dp)) }

        item {
            QuickBalanceCard(
                youOwe = state.youOwe,
                youAreOwed = state.youAreOwed,
                onClick = {
                    currentMemberId?.let {
                        onNavigateToBalances(it)
                    }
                }
            )
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        item {
            RecentExpensesCard(
                totalExpenses = state.expenseCount,
                totalAmount = state.totalAmount,
                recentExpenses = state.recentExpenses,
                onClick = {
                    currentMemberId?.let {
                        onNavigateToExpenses(state.trip.id, currentUserId, it)
                    }
                }
            )
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        item {
            MembersCard(
                members = state.members,
                onClick = onNavigateToMembers
            )
        }

        if (state.expenseCount > 0) {
            item { Spacer(modifier = Modifier.height(12.dp)) }

            item {
                InsightsPreviewCard(
                    onClick = onNavigateToInsights
                )
            }
        }

        if (state.pendingSettlements > 0 || state.completedSettlements > 0) {
            item { Spacer(modifier = Modifier.height(12.dp)) }

            item {
                SettlementsCard(
                    pendingCount = state.pendingSettlements,
                    completedCount = state.completedSettlements,
                    onClick = {
                        currentMemberId?.let {
                            onNavigateToSettlement(state.trip.id, currentMemberId)
                        }
                    }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}