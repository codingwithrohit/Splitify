package com.example.splitify.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.splitify.presentation.auth.LoginScreen
import com.example.splitify.presentation.auth.SignUpScreen
import com.example.splitify.presentation.trips.CreateTripScreen
import com.example.splitify.presentation.trips.TripsScreen
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth


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

            // TODO: Add more screens here later
            // - Trip Detail
            // - Add Expense
            // - Add Members
            // - etc.
        }
    }

}