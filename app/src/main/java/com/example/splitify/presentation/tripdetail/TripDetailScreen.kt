package com.example.splitify.presentation.tripdetail

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.splitify.presentation.components.ErrorStateWithRetry
import com.example.splitify.presentation.components.LoadingScreen
import com.example.splitify.presentation.tripdetail.cards.InsightsPreviewCard
import com.example.splitify.presentation.tripdetail.cards.MembersCard
import com.example.splitify.presentation.tripdetail.cards.PremiumTripCard
import com.example.splitify.presentation.tripdetail.cards.QuickBalanceCard
import com.example.splitify.presentation.tripdetail.cards.RecentExpensesCard
import com.example.splitify.presentation.tripdetail.cards.SettlementsCard
import com.example.splitify.presentation.tripdetail.cards.TripInfoCard

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
    viewModel: TripDetailViewModel = hiltViewModel()
){
    LaunchedEffect(tripId) {
        Log.d("TripDetailScreen", "════════════════════════════════")
        Log.d("TripDetailScreen", "🎯 Screen Loaded")
        Log.d("TripDetailScreen", "  Trip ID: $tripId")
        Log.d("TripDetailScreen", "════════════════════════════════")
    }

    val dashboardState by viewModel.dashboardState.collectAsStateWithLifecycle()

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
        }
    }

    Scaffold(
        topBar = {
            TripDetailTopBar(
                tripName = when (val state = dashboardState) {
                    is TripDashboardState.Success -> state.trip.name
                    else -> "Trip Details"
                },
                onBack = onNavigateBack
            )
        },
        floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = onAddExpense,
                    icon = { Icon(Icons.Default.Add, "Add expense") },
                    text = { Text("New Expense") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
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
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TripDetailTopBar(
    tripName: String,
    onBack: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = tripName,
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            IconButton(onClick = { /* TODO: Share trip */ }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share"
                )
            }
            IconButton(onClick = { /* TODO: Trip settings */ }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options"
                )
            }
        }
    )
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

    LazyColumn(modifier = modifier
        .fillMaxSize()
        .padding(16.dp)) {


        item{
            PremiumTripCard(
                trip = state.trip,
                memberCount = state.members.size,
                totalAmount = state.totalAmount,
                onClick = { state.trip.id.let { onNavigateToEditTrip(it) }}
            )
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }


        // Recent Balance Card
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

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Recent Expenses Card
        item {
            RecentExpensesCard(
                totalExpenses = state.expenseCount,
                totalAmount = state.totalAmount,
                recentExpenses = state.recentExpenses,
                onClick = {
                    currentMemberId?.let {
                        onNavigateToExpenses(state.trip.id,currentUserId, it)
                    }
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Members Card
        item{
            MembersCard(
                members = state.members,
                onClick = onNavigateToMembers
            )
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Insights Card (only show if there are expenses)
        if (state.expenseCount > 0) {
            item {
                InsightsPreviewCard(
                    onClick = onNavigateToInsights
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Settlements Card (only show if there are settlements)
        if (state.pendingSettlements > 0 || state.completedSettlements > 0) {
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

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }


    }
}




