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
import com.example.splitify.presentation.settlement.SettlementHistoryScreen
import com.example.splitify.presentation.tripdetail.TripDetailScreen
import com.example.splitify.presentation.tripdetail.TripInsights
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
        startDestination = if(session != null)
            Screen.Trips.route
        else
            Screen.Login.route
        isCheckingAuth = false
    }

    if(!isCheckingAuth){
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            //Login Screen
            composable(route = Screen.Login.route){
                LoginScreen(
                    onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                    onLoginSuccess = { navController.navigate(Screen.Trips.route){
                        popUpTo(Screen.Login.route){ inclusive = true }
                    } }
                )
            }
            //SignUp Screen
            composable(route = Screen.SignUp.route){
                SignUpScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSignUpSuccess = { navController.navigate(Screen.Trips.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    } }
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
                        navController.navigate(Screen.Login.route){
                            popUpTo(0){inclusive=true}
                        }
                    }
                )
            }

            // Create Trip Screen
            composable(route = Screen.CreateTrip.route) {
                CreateTripScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onTripCreated = {
                        // Navigate back to trips list
                        navController.popBackStack()
                    }
                )
            }

            // Trip Detail Screen
            composable(
                route = Screen.TripDetail.route,
                arguments = listOf(
                    navArgument(Screen.TripDetail.ARG_TRIP_ID){
                        type = NavType.StringType
                    }
                )
            ){
                val tripId = it.arguments?.getString(Screen.TripDetail.ARG_TRIP_ID) ?: return@composable
                TripDetailScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onAddExpense = {
                        navController.navigate(Screen.AddExpense.route)
                    },
                    onAddMembers = {
                        navController.navigate(Screen.AddMember.route)
                    },
                    onNavigateToSettlement = {tripId, memberId ->
                        navController.navigate(
                            Screen.SettlementHistory.createRoute(tripId, memberId)
                        )
                    },
                    onNavigateToExpense = {tripId, userId, memberId ->
                        navController.navigate(
                            Screen.ExpensesScreen.createRoute(tripId!!, userId!!, memberId!!)
                        )
                    },
                    onNavigateToMembers = {
                        navController.navigate(Screen.MembersScreen.route)
                    },
                    onNavigateToBalances = {memberId ->
                        navController.navigate(
                            Screen.BalancesScreen.createRoute(tripId, memberId)
                        )
                    },
                    tripId = tripId,
                    onNavigateToInsights = {
                        navController.navigate(Screen.CreateTrip.route)
                    }

                )
            }

            composable(
                route = Screen.TripInsights.route,
                arguments = listOf(
                    navArgument(Screen.TripInsights.ARG_TRIP_ID){
                        type = NavType.StringType
                    }
                )
            ){
                TripInsights()
            }

            composable(
                route = Screen.ExpensesScreen.route,
                arguments = listOf(
                    navArgument(Screen.ExpensesScreen.ARG_TRIP_ID){
                        type = NavType.StringType
                    },
                    navArgument(Screen.ExpensesScreen.ARG_USER_ID){
                        type = NavType.StringType
                    },
                    navArgument(Screen.ExpensesScreen.ARG_MEMBER_ID){
                        type = NavType.StringType
                    }
                )
            ){
                val tripId = it.arguments?.getString(Screen.ExpensesScreen.ARG_TRIP_ID) ?: return@composable
                val userId = it.arguments?.getString(Screen.ExpensesScreen.ARG_USER_ID) ?: return@composable
                val memberId = it.arguments?.getString(Screen.ExpensesScreen.ARG_MEMBER_ID) ?: return@composable
                ExpensesScreen(
                    onEditExpense = {
                        navController.navigate(Screen.EditExpense.route)
                    },
                    onAddExpense = {
                        navController.navigate(Screen.AddExpense.route)
                    },
                    onBack = {
                        navController.popBackStack()
                    },
                    currentMemberId = memberId,
                    currentUserId = userId
                )
            }
            composable(route = Screen.MembersScreen.route,
                arguments = listOf(
                    navArgument(Screen.MembersScreen.ARG_TRIP_ID){
                        type = NavType.StringType
                    }
                )
            ){
                val tripId = it.arguments?.getString(Screen.MembersScreen.ARG_TRIP_ID) ?: return@composable
                MembersScreen(
                    tripId = tripId,
                    onBack = { navController.popBackStack() },
                    onAddMembers = {navController.navigate(Screen.AddMember.route)},
                )
            }
            composable(route = Screen.BalancesScreen.route,
                arguments = listOf(
                    navArgument(Screen.BalancesScreen.ARG_TRIP_ID){
                        type = NavType.StringType
                    },
                    navArgument(Screen.BalancesScreen.ARG_MEMBER_ID){
                        type = NavType.StringType
                    }
                )
            ){
                val tripId = it.arguments?.getString(Screen.BalancesScreen.ARG_TRIP_ID) ?: return@composable
                val memberId = it.arguments?.getString(Screen.BalancesScreen.ARG_MEMBER_ID) ?: return@composable
                BalancesScreen(
                    tripId = tripId,
                    onNavigateToHistory = {navController.navigate(Screen.SettlementHistory.route)},
                    currentMemberId = memberId
                )
            }

            composable(
                route = Screen.AddExpense.route,
                arguments = listOf(
                    navArgument(Screen.AddExpense.ARG_TRIP_ID){
                        type = NavType.StringType
                    }
                )
            ){
                AddExpenseScreen(
                    onNavigationBack = {navController.popBackStack()},
                )
            }

            composable(
                route = Screen.AddMember.route,
                arguments = listOf(
                    navArgument(Screen.AddMember.ARG_TRIP_ID){
                        type = NavType.StringType
                    }
                )
            ){
                AddMemberScreen(
                    onNavigateBack = {navController.popBackStack()}
                )
            }

            composable(
                route = Screen.EditExpense.route,
                arguments = listOf(
                    navArgument(Screen.EditExpense.ARG_TRIP_ID){
                        type = NavType.StringType
                    },
                    navArgument(Screen.EditExpense.ARG_EXPENSE_ID){
                        type = NavType.StringType
                    }
                )
            ){
                val args = it.arguments
                Log.d("NAV_EDIT", "args = $args")

                val tripId = args?.getString(Screen.EditExpense.ARG_TRIP_ID)
                val expenseId = args?.getString(Screen.EditExpense.ARG_EXPENSE_ID)

                Log.d("NAV_EDIT", "tripId=$tripId expenseId=$expenseId")

                AddExpenseScreen(
                    onNavigationBack = { navController.navigateUp() }
                )

            }

            composable(
                route = Screen.SettlementHistory.route,
                arguments = listOf(
                    navArgument(Screen.SettlementHistory.ARG_TRIP_ID) {
                        type = NavType.StringType
                    },
                    navArgument(Screen.SettlementHistory.ARG_MEMBER_ID){
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString(Screen.SettlementHistory.ARG_TRIP_ID) ?: return@composable
                val memberId = backStackEntry.arguments?.getString(Screen.SettlementHistory.ARG_MEMBER_ID) ?: return@composable
                SettlementHistoryScreen(
                    tripId = tripId,
                    onBack = {navController.popBackStack()},
                    currentMemberId = memberId
                )
            }
        }
    }

}