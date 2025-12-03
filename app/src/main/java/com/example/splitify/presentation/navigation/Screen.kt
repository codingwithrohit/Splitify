package com.example.splitify.presentation.navigation

sealed class Screen(val route: String) {

    //Auth Screens
    data object Login: Screen("login")
    data object SignUp: Screen("signup")

    data object Trips: Screen("trips")
    data object CreateTrip: Screen("create_trip")
    data object TripDetail: Screen("trip_detail/{tripId}"){
        fun createRoute(tripId: String) = "trip_detail/$tripId"
    }
}