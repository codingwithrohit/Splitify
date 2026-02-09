package com.example.splitify.presentation.navigation

import kotlin.math.exp

sealed class Screen(val route: String) {

    //Auth Screens
    data object Login: Screen("login")
    data object SignUp: Screen("signup")
    object Main : Screen("main")
    data object Trips: Screen("trips")
    // Profile Routes
    object Profile : Screen("profile")
    object AccountSettings : Screen("profile/account_settings")
    object AppSettings : Screen("profile/app_settings")
    object About : Screen("profile/about")
    object Developer : Screen("profile/developer")

    data object JoinTrip: Screen("join_trip")
    data object CreateTrip: Screen("create_trip")

    data object EditTrip: Screen("edit_trip/{tripId}"){
        fun createRoute(tripId: String) = "edit_trip/$tripId"
        const val ARG_TRIP_ID = "tripId"
    }

    data object TripDetail: Screen("trip_detail/{tripId}"){
        fun createRoute(tripId: String) = "trip_detail/$tripId"

        const val ARG_TRIP_ID = "tripId"
    }

    data object ExpensesScreen: Screen("expenses_screen/{tripId}/{userId}/{memberId}"){
        fun createRoute(tripId: String, userId: String, memberId: String) =
            "expenses_screen/$tripId/$userId/$memberId"
        const val ARG_TRIP_ID = "tripId"
        const val ARG_USER_ID = "userId"
        const val ARG_MEMBER_ID = "memberId"
    }

    data object InsightsScreen: Screen("insights_screen/{tripId}"){
        fun createRoute(tripId: String) = "insights_screen/$tripId"
        const val ARG_TRIP_ID = "tripId"
    }

    data object MembersScreen: Screen("members_screen/{tripId}") {
        fun createRoute(tripId: String) = "members_screen/$tripId"
        const val ARG_TRIP_ID = "tripId"
    }
    data object BalancesScreen : Screen("balances_screen/{tripId}/{memberId}") {
        fun createRoute(tripId: String, memberId: String) = "balances_screen/$tripId/$memberId"
        const val ARG_TRIP_ID = "tripId"
        const val ARG_MEMBER_ID = "memberId"
    }

    data object AddExpense: Screen("add_expense/{tripId}"){
        fun createRoute(tripId: String) = "add_expense/${tripId}"
        const val ARG_TRIP_ID = "tripId"
    }

    data object AddMember: Screen("add_member/{tripId}"){
        fun createRoute(tripId: String) = "add_member/${tripId}"
        const val ARG_TRIP_ID = "tripId"

    }

    data object SettlementHistory: Screen("settlement_history/{tripId}/{memberId}"){
        fun createRoute(tripId: String, memberId: String) = "settlement_history/${tripId}/{$memberId}"
        const val ARG_TRIP_ID = "tripId"
        const val ARG_MEMBER_ID = "memberId"
    }

    data object EditExpense: Screen("edit_expense/{tripId}/{expenseId}"){
        fun createRoute(tripId: String, expenseId: String) = "edit_expense/${tripId}/${expenseId}"
        const val ARG_TRIP_ID = "tripId"
        const val ARG_EXPENSE_ID = "expenseId"
    }
}