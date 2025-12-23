package com.example.splitify.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.splitify.presentation.addmembers.AddMemberScreen
import com.example.splitify.presentation.auth.LoginScreen
import com.example.splitify.presentation.auth.SignUpScreen
import com.example.splitify.presentation.expense.AddExpenseScreen
import com.example.splitify.presentation.session.CurrentMemberIdProvider
import com.example.splitify.presentation.session.SessionViewModel
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
                    },
                    navArgument(Screen.EditExpense.ARG_EXPENSE_ID){
                        type = NavType.StringType
                    }
                )
            ){
                val tripId = it.arguments?.getString(Screen.TripDetail.ARG_TRIP_ID) ?: return@composable
                TripDetailScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onAddExpense = {
                        navController.navigate(Screen.AddExpense.createRoute(tripId))
                    },
                    onAddMember = {
                        navController.navigate(Screen.AddMember.createRoute(tripId))
                    },
                    onEditExpense = { expenseId ->
                        navController.navigate(Screen.EditExpense.createRoute(tripId, expenseId))
                    },
                    onNavigateToSettlementHistory = {
                       navController.navigate(Screen.SettlementHistory.createRoute(tripId))
                    }
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
                val tripId = it.arguments?.getString(Screen.EditExpense.ARG_TRIP_ID) ?: return@composable
                val expenseId = it.arguments?.getString(Screen.EditExpense.ARG_EXPENSE_ID) ?: return@composable

                AddExpenseScreen(
                    onNavigationBack = {navController.navigateUp()}
                )
            }

            composable(
                route = Screen.SettlementHistory.route,
                arguments = listOf(navArgument("tripId") { type = NavType.StringType })
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
                CurrentMemberIdProvider(tripId)  { currentMemberId ->
                    SettlementHistoryScreen(
                        tripId = tripId,
                        currentMemberId = currentMemberId, // Get from your auth
                        onBack = { navController.navigateUp() }
                    )
                }
            }
        }
    }

}