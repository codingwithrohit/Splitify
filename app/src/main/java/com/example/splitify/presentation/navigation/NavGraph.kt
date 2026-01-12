package com.example.splitify.presentation.navigation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.splitify.presentation.addmembers.AddMemberScreen
import com.example.splitify.presentation.addmembers.MembersScreen
import com.example.splitify.presentation.auth.LoginScreen
import com.example.splitify.presentation.auth.SignUpScreen
import com.example.splitify.presentation.balances.BalancesScreen
import com.example.splitify.presentation.expense.AddExpenseScreen
import com.example.splitify.presentation.expense.ExpensesScreen
import com.example.splitify.presentation.insights.InsightsScreen
import com.example.splitify.presentation.settlement.SettlementHistoryScreen
import com.example.splitify.presentation.tripdetail.TripDetailScreen
import com.example.splitify.presentation.trips.CreateTripScreen
import com.example.splitify.presentation.trips.TripsScreen
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun SplitifyNavGraph(
    navController: NavHostController = rememberNavController(),
    supabase: SupabaseClient
) {
    var isCheckingAuth by remember { mutableStateOf(true) }
    var startDestination by remember { mutableStateOf(Screen.Login.route) }

    LaunchedEffect(Unit) {
        val session = supabase.auth.currentSessionOrNull()
        startDestination = if (session != null)
            Screen.Trips.route
        else
            Screen.Login.route
        isCheckingAuth = false
    }

    if (!isCheckingAuth) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            // Login Screen
            composable(route = Screen.Login.route) {
                LoginScreen(
                    onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                    onLoginSuccess = {
                        navController.navigate(Screen.Trips.route) {
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
                        navController.navigate(Screen.Trips.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            // Trips List Screen
            composable(route = Screen.Trips.route) {
                TripsScreen(
                    onCreateTripClick = {
                        navController.navigate(Screen.CreateTrip.route)
                    },
                    onTripClick = { tripId ->
                        navController.navigate(Screen.TripDetail.createRoute(tripId))
                    },
                    onLogOut = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // Create Trip Screen
            composable(route = Screen.CreateTrip.route) {
                CreateTripScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
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
            ) {
                val tripId = it.arguments?.getString(Screen.TripDetail.ARG_TRIP_ID)
                    ?: return@composable

                Log.d("NavGraph", "🧭 TripDetailScreen - tripId: $tripId")

                TripDetailScreen(
                    tripId = tripId,
                    onNavigateBack = { navController.popBackStack() },

                    // ✅ FIX 1: Add Expense
                    onAddExpense = {
                        Log.d("NavGraph", "🧭 Navigating to AddExpense with tripId: $tripId")
                        navController.navigate(Screen.AddExpense.createRoute(tripId))
                    },

                    // ✅ FIX 2: Add Members
                    onAddMembers = {
                        Log.d("NavGraph", "🧭 Navigating to AddMember with tripId: $tripId")
                        navController.navigate(Screen.AddMember.createRoute(tripId))
                    },

                    // ✅ FIX 3: Navigate to Expenses Screen
                    onNavigateToExpense = { _, userId, memberId ->
                        Log.d("NavGraph", "🧭 Navigating to ExpensesScreen")
                        navController.navigate(
                            Screen.ExpensesScreen.createRoute(tripId, userId!!, memberId!!)
                        )
                    },

                    // ✅ FIX 4: Navigate to Members Screen (was missing tripId!)
                    onNavigateToMembers = {
                        Log.d("NavGraph", "🧭 Navigating to MembersScreen with tripId: $tripId")
                        navController.navigate(Screen.MembersScreen.createRoute(tripId))
                    },

                    // ✅ FIX 5: Navigate to Balances Screen
                    onNavigateToBalances = { memberId ->
                        Log.d("NavGraph", "🧭 Navigating to BalancesScreen")
                        navController.navigate(
                            Screen.BalancesScreen.createRoute(tripId, memberId)
                        )
                    },

                    // ✅ FIX 6: Navigate to Insights (was going to CreateTrip!)
                    onNavigateToInsights = {
                        Log.d("NavGraph", "🧭 Navigating to TripInsights with tripId: $tripId")
                        navController.navigate(Screen.InsightsScreen.createRoute(tripId))
                    },

                    // ✅ FIX 7: Navigate to Settlement
                    onNavigateToSettlement = { _, memberId ->
                        Log.d("NavGraph", "🧭 Navigating to SettlementHistory")
                        navController.navigate(
                            Screen.SettlementHistory.createRoute(tripId, memberId)
                        )
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
                    }
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
                    }
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
                    onNavigationBack = { navController.popBackStack() }
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
                    onNavigationBack = { navController.navigateUp() }
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