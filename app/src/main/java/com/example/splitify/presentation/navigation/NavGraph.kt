package com.example.splitify.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.splitify.presentation.trips.CreateTripScreen
import com.example.splitify.presentation.trips.TripsScreen


@Composable
fun SplitifyNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Trips.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Trips List Screen
        composable(route = Screen.Trips.route) {
            TripsScreen(
                onCreateTripClick = {
                    navController.navigate(Screen.CreateTrip.route)
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