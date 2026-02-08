package com.example.splitify.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CardTravel
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Trips : BottomNavItem(
        route = "trips_tab",
        title = "Trips",
        selectedIcon = Icons.Filled.CardTravel,
        unselectedIcon = Icons.Outlined.CardTravel
    )

    object Profile : BottomNavItem(
        route = "profile_tab",
        title = "Profile",
        selectedIcon = Icons.Filled.AccountCircle,
        unselectedIcon = Icons.Outlined.AccountCircle
    )
}

val bottomNavItems = listOf(
    BottomNavItem.Trips,
    BottomNavItem.Profile
)