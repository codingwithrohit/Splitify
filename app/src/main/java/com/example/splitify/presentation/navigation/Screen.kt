package com.example.splitify.presentation.navigation

import kotlin.math.exp

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

    data object AddMember: Screen("add_member/{tripId}"){
        fun createRoute(tripId: String) = "add_member/${tripId}"
        const val ARG_TRIP_ID = "tripId"

    }

    data object SettlementHistory: Screen("settlement_history/{tripId}"){
        fun createRoute(tripId: String?) = "settlement_history/${tripId}"
        const val ARG_TRIP_ID = "tripId"
    }

    data object EditExpense: Screen("edit_expense/{tripId}/{expenseId}"){
        fun createRoute(tripId: String, expenseId: String) = "edit_expense/${tripId}/${expenseId}"
        const val ARG_TRIP_ID = "tripId"
        const val ARG_EXPENSE_ID = "expenseId"
    }
}