package com.example.splitify.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.splitify.presentation.auth.LoginScreen
import com.example.splitify.presentation.auth.SignUpScreen
import com.example.splitify.presentation.trips.CreateTripScreen
import com.example.splitify.presentation.trips.TripsScreen


@Composable
fun SplitifyNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
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