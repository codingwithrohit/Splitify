package com.example.splitify.presentation.navigation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.splitify.data.local.SessionManager
import com.example.splitify.domain.model.NotificationType
import com.example.splitify.domain.repository.AuthRepository
import com.example.splitify.presentation.addmembers.AddMemberScreen
import com.example.splitify.presentation.addmembers.MembersScreen
import com.example.splitify.presentation.auth.LoginScreen
import com.example.splitify.presentation.auth.SignUpScreen
import com.example.splitify.presentation.balances.BalancesScreen
import com.example.splitify.presentation.components.CustomNotificationSnackbar
import com.example.splitify.presentation.expense.AddExpenseScreen
import com.example.splitify.presentation.expense.ExpensesScreen
import com.example.splitify.presentation.insights.InsightsScreen
import com.example.splitify.presentation.jointrip.JoinTripScreen
import com.example.splitify.presentation.settlement.SettlementHistoryScreen
import com.example.splitify.presentation.tripdetail.TripDetailScreen
import com.example.splitify.presentation.tripdetail.TripDetailViewModel
import com.example.splitify.presentation.trips.CreateTripScreen
import com.example.splitify.presentation.trips.TripsScreen
import com.example.splitify.presentation.trips.TripsViewModel
import com.example.splitify.util.NotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun SplitifyNavGraph(
    navController: NavHostController = rememberNavController(),
    authRepository: AuthRepository,
    sessionManager: SessionManager,
    notificationManager: NotificationManager
) {
    var isCheckingSession by remember { mutableStateOf(true) }
    var startDestination by remember { mutableStateOf(Screen.Login.route) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("NavGraph", "🔄 Checking session...")

                // Check if session exists
                val hasSession = sessionManager.hasValidSession()

                if (hasSession) {
                    Log.d("NavGraph", "✅ Session found, initializing...")

                    // Try to restore/refresh session
                    val sessionValid = authRepository.initializeSession()

                    if (sessionValid) {
                        Log.d("NavGraph", "✅ Session valid → Trips screen")
                        startDestination = Screen.Main.route
                    } else {
                        Log.d("NavGraph", "❌ Session invalid → Login screen")
                        startDestination = Screen.Login.route
                    }
                } else {
                    Log.d("NavGraph", "❌ No session → Login screen")
                    startDestination = Screen.Login.route
                }

            } catch (e: Exception) {
                Log.e("NavGraph", "❌ Session check failed", e)
                startDestination = Screen.Login.route
            } finally {
                isCheckingSession = false
            }
        }
    }

    if (!isCheckingSession) {

        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(Unit) {
            notificationManager.notificationEvent.collectLatest { notification ->
                snackbarHostState.showSnackbar(
                    message = notification.message,
                    actionLabel = "Dismiss",
                    withDismissAction = true,
                    duration = when (notification.type) {
                        NotificationType.ERROR -> SnackbarDuration.Long
                        else -> SnackbarDuration.Short
                    }
                )

            }
        }

        Scaffold(
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) { data ->
                    CustomNotificationSnackbar(
                        message = data.visuals.message,
                        actionLabel = data.visuals.actionLabel,
                        onAction = { data.performAction() },
                        onDismiss = { data.dismiss() }
                    )
                }
            }
        ) { paddingValues ->

            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(paddingValues)
            ) {
                // Login Screen
                composable(route = Screen.Login.route) {
                    LoginScreen(
                        onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                        onLoginSuccess = {
                            navController.navigate(Screen.Main.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    )
                }

                // SignUp Screen
                composable(route = Screen.SignUp.route) {
                    SignUpScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onSignUpSuccess = {
                            navController.navigate(Screen.Main.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(route = Screen.Main.route) {
                    MainScreen(
                        onLogOut = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToTripDetail = { tripId ->
                            navController.navigate(Screen.TripDetail.createRoute(tripId))
                        },
                        onNavigateToCreateTrip = {
                            navController.navigate(Screen.CreateTrip.route)
                        },
                        onNavigateToJoinTrip = {
                            navController.navigate(Screen.JoinTrip.route)
                        },
                        sessionManager = sessionManager,
                        authRepository = authRepository
                    )
                }

                // Create Trip Screen
                composable(
                    route = Screen.CreateTrip.route,
                    arguments = listOf(navArgument("tripId") {
                        type = NavType.StringType
                        nullable = true
                    })
                ) { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getString("tripId")
                    val tripsViewModel: TripsViewModel = hiltViewModel()

                    CreateTripScreen(
                        tripId = tripId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                //Join Trip Screen
                composable(Screen.JoinTrip.route) {
                    JoinTripScreen(
                        onBackClick = { navController.popBackStack() },
                        onTripJoined = { tripId ->
                            navController.popBackStack()
                            navController.navigate(Screen.TripDetail.createRoute(tripId))
                        }
                    )
                }

                //Edit Trip
                composable(route = Screen.EditTrip.route,
                    arguments = listOf(
                        navArgument(Screen.EditTrip.ARG_TRIP_ID){
                            type = NavType.StringType
                        }
                    )){
                    val tripId = it.arguments?.getString(Screen.EditTrip.ARG_TRIP_ID)
                    CreateTripScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        tripId = tripId
                    )
                }

                // Trip Detail Screen
                composable(
                    route = Screen.TripDetail.route,
                    arguments = listOf(
                        navArgument(Screen.TripDetail.ARG_TRIP_ID) {
                            type = NavType.StringType
                        }
                    )
                ) {backStackEntry ->  // ← Add this parameter
                    val tripId = backStackEntry.arguments?.getString(Screen.TripDetail.ARG_TRIP_ID)
                        ?: return@composable

                    Log.d("NavGraph", "🧭 TripDetailScreen - tripId: $tripId")

                    // FIX: Scope ViewModel to backStackEntry
                    val viewModel: TripDetailViewModel = hiltViewModel(backStackEntry)

                    TripDetailScreen(
                        tripId = tripId,
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() },
                        navController = navController,

                        // 1: Add Expense
                        onAddExpense = {
                            Log.d("NavGraph", "🧭 Navigating to AddExpense with tripId: $tripId")
                            navController.navigate(Screen.AddExpense.createRoute(tripId))
                        },

                        // 2: Add Members
                        onAddMembers = {
                            Log.d("NavGraph", "🧭 Navigating to AddMember with tripId: $tripId")
                            navController.navigate(Screen.AddMember.createRoute(tripId))
                        },

                        //  3: Navigate to Expenses Screen
                        onNavigateToExpense = { _, userId, memberId ->
                            Log.d("NavGraph", "🧭 Navigating to ExpensesScreen")
                            navController.navigate(
                                Screen.ExpensesScreen.createRoute(tripId, userId!!, memberId!!)
                            )
                        },

                        // 4: Navigate to Members Screen (was missing tripId!)
                        onNavigateToMembers = {
                            Log.d("NavGraph", "🧭 Navigating to MembersScreen with tripId: $tripId")
                            navController.navigate(Screen.MembersScreen.createRoute(tripId))
                        },

                        // 5: Navigate to Balances Screen
                        onNavigateToBalances = { memberId ->
                            Log.d("NavGraph", "🧭 Navigating to BalancesScreen")
                            navController.navigate(
                                Screen.BalancesScreen.createRoute(tripId, memberId)
                            )
                        },

                        // 6: Navigate to Insights (was going to CreateTrip!)
                        onNavigateToInsights = {
                            Log.d("NavGraph", "🧭 Navigating to TripInsights with tripId: $tripId")
                            navController.navigate(Screen.InsightsScreen.createRoute(tripId))
                        },

                        // 7: Navigate to Settlement
                        onNavigateToSettlement = { _, memberId ->
                            Log.d("NavGraph", "🧭 Navigating to SettlementHistory")
                            navController.navigate(
                                Screen.SettlementHistory.createRoute(tripId, memberId)
                            )
                        },
                        // 8. Edit Trip
                        onNavigateToEditTrip = { tripId ->
                            Log.d("NavGraph", "🧭 Navigating to EditTrip with tripId: $tripId")
                            navController.navigate(Screen.EditTrip.createRoute(tripId))
                        }
                    )
                }

                // Trip Insights Screen
                composable(
                    route = Screen.InsightsScreen.route,
                    arguments = listOf(
                        navArgument(Screen.InsightsScreen.ARG_TRIP_ID){
                            type = NavType.StringType
                        }
                    )
                ) {
                    val tripId = it.arguments?.getString(Screen.InsightsScreen.ARG_TRIP_ID)
                        ?: return@composable

                    InsightsScreen(
                        onBack = {navController.popBackStack()}
                    )
                }

                // Expenses Screen
                composable(
                    route = Screen.ExpensesScreen.route,
                    arguments = listOf(
                        navArgument(Screen.ExpensesScreen.ARG_TRIP_ID) {
                            type = NavType.StringType
                        },
                        navArgument(Screen.ExpensesScreen.ARG_USER_ID) {
                            type = NavType.StringType
                        },
                        navArgument(Screen.ExpensesScreen.ARG_MEMBER_ID) {
                            type = NavType.StringType
                        }
                    )
                ) {
                    val tripId = it.arguments?.getString(Screen.ExpensesScreen.ARG_TRIP_ID)
                        ?: return@composable
                    val userId = it.arguments?.getString(Screen.ExpensesScreen.ARG_USER_ID)
                        ?: return@composable
                    val memberId = it.arguments?.getString(Screen.ExpensesScreen.ARG_MEMBER_ID)
                        ?: return@composable

                    Log.d("NavGraph", "🧭 ExpensesScreen - tripId: $tripId")

                    ExpensesScreen(
                        currentUserId = userId,
                        currentMemberId = memberId,
                        onBack = { navController.popBackStack() },
                        onAddExpense = {
                            navController.navigate(Screen.AddExpense.createRoute(tripId))
                        },
                        onEditExpense = { expenseId ->
                            navController.navigate(
                                Screen.EditExpense.createRoute(tripId, expenseId)
                            )
                        },
                        navController = navController
                    )
                }

                // Members Screen
                composable(
                    route = Screen.MembersScreen.route,
                    arguments = listOf(
                        navArgument(Screen.MembersScreen.ARG_TRIP_ID) {
                            type = NavType.StringType
                        }
                    )
                ) {
                    val tripId = it.arguments?.getString(Screen.MembersScreen.ARG_TRIP_ID)
                        ?: return@composable

                    Log.d("NavGraph", "🧭 MembersScreen - tripId: $tripId")

                    MembersScreen(
                        tripId = tripId,
                        onBack = { navController.popBackStack() },
                        // ✅ FIX 8: Add Members from Members Screen (was missing tripId!)
                        onAddMembers = {
                            Log.d("NavGraph", "🧭 Navigating to AddMember from MembersScreen")
                            navController.navigate(Screen.AddMember.createRoute(tripId))
                        }
                    )
                }

                // Balances Screen
                composable(
                    route = Screen.BalancesScreen.route,
                    arguments = listOf(
                        navArgument(Screen.BalancesScreen.ARG_TRIP_ID) {
                            type = NavType.StringType
                        },
                        navArgument(Screen.BalancesScreen.ARG_MEMBER_ID) {
                            type = NavType.StringType
                        }
                    )
                ) {
                    val tripId = it.arguments?.getString(Screen.BalancesScreen.ARG_TRIP_ID)
                        ?: return@composable
                    val memberId = it.arguments?.getString(Screen.BalancesScreen.ARG_MEMBER_ID)
                        ?: return@composable

                    Log.d("NavGraph", "🧭 BalancesScreen - tripId: $tripId, memberId: $memberId")

                    BalancesScreen(
                        tripId = tripId,
                        currentMemberId = memberId,
                        onNavigateToHistory = {
                            navController.navigate(
                                Screen.SettlementHistory.createRoute(tripId, memberId)
                            )
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // Add Expense Screen
                composable(
                    route = Screen.AddExpense.route,
                    arguments = listOf(
                        navArgument(Screen.AddExpense.ARG_TRIP_ID) {
                            type = NavType.StringType
                        }
                    )
                ) {
                    val tripId = it.arguments?.getString(Screen.AddExpense.ARG_TRIP_ID)
                        ?: return@composable

                    Log.d("NavGraph", "🧭 AddExpenseScreen - tripId: $tripId")

                    AddExpenseScreen(
                        onNavigationBack = { navController.popBackStack() },
                        navController = navController
                    )
                }

                // Add Member Screen
                composable(
                    route = Screen.AddMember.route,
                    arguments = listOf(
                        navArgument(Screen.AddMember.ARG_TRIP_ID) {
                            type = NavType.StringType
                        }
                    )
                ) {
                    val tripId = it.arguments?.getString(Screen.AddMember.ARG_TRIP_ID)
                        ?: return@composable

                    Log.d("NavGraph", "🧭 AddMemberScreen - tripId: $tripId")

                    AddMemberScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // Edit Expense Screen
                composable(
                    route = Screen.EditExpense.route,
                    arguments = listOf(
                        navArgument(Screen.EditExpense.ARG_TRIP_ID) {
                            type = NavType.StringType
                        },
                        navArgument(Screen.EditExpense.ARG_EXPENSE_ID) {
                            type = NavType.StringType
                        }
                    )
                ) {
                    val tripId = it.arguments?.getString(Screen.EditExpense.ARG_TRIP_ID)
                        ?: return@composable
                    val expenseId = it.arguments?.getString(Screen.EditExpense.ARG_EXPENSE_ID)
                        ?: return@composable

                    Log.d("NavGraph", "🧭 EditExpenseScreen - tripId: $tripId, expenseId: $expenseId")

                    AddExpenseScreen(
                        onNavigationBack = { navController.navigateUp() },
                        navController = navController
                    )
                }

                // Settlement History Screen
                composable(
                    route = Screen.SettlementHistory.route,
                    arguments = listOf(
                        navArgument(Screen.SettlementHistory.ARG_TRIP_ID) {
                            type = NavType.StringType
                        },
                        navArgument(Screen.SettlementHistory.ARG_MEMBER_ID) {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getString(Screen.SettlementHistory.ARG_TRIP_ID)
                        ?: return@composable
                    val memberId = backStackEntry.arguments?.getString(Screen.SettlementHistory.ARG_MEMBER_ID)
                        ?: return@composable

                    Log.d("NavGraph", "🧭 SettlementHistoryScreen - tripId: $tripId, memberId: $memberId")

                    SettlementHistoryScreen(
                        tripId = tripId,
                        currentMemberId = memberId,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }

    }
}