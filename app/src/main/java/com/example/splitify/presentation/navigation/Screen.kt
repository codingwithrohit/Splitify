package com.example.splitify.presentation.navigation

sealed class Screen(val route: String) {

    //Auth Screens
    data object Login: Screen("login")
    data object SignUp: Screen("signup")

    data object Trips: Screen("trips")
    data object CreateTrip: Screen("create_trip")

    data object TripDetail: Screen("trip_detail/{tripId}"){
        fun createRoute(tripId: String) = "trip_detail/$tripId"

        const val ARG_TRIP_ID = "tripId"
    }

    data object AddExpense: Screen("add_expense/{tripId}"){
        fun createRoute(tripId: String) = "add_expense/${tripId}"

        const val ARG_TRIP_ID = "tripId"
    }
}