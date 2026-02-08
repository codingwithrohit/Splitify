package com.example.splitify.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.splitify.data.local.SessionManager
import com.example.splitify.domain.repository.AuthRepository
import com.example.splitify.presentation.theme.NeutralColors
import com.example.splitify.presentation.theme.PrimaryColors
import com.example.splitify.presentation.trips.TripsScreen

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun MainScreen(
    onLogOut: () -> Unit,
    onNavigateToTripDetail: (String) -> Unit,
    onNavigateToCreateTrip: () -> Unit,
    onNavigateToJoinTrip: () -> Unit,
    sessionManager: SessionManager,
    authRepository: AuthRepository
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Trips.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(BottomNavItem.Trips.route) {
                TripsScreen(
                    onCreateTripClick = onNavigateToCreateTrip,
                    onTripClick = onNavigateToTripDetail,
                    onJoinTripClick = onNavigateToJoinTrip,
                    onLogOut = onLogOut
                )
            }

//            composable(BottomNavItem.Profile.route) {
//                ProfileScreen(
//                    onLogOut = onLogOut,
//                    sessionManager = sessionManager,
//                    authRepository = authRepository
//                )
//            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any {
                it.route == item.route
            } == true

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryColors.Primary600,
                    selectedTextColor = PrimaryColors.Primary600,
                    unselectedIconColor = NeutralColors.Neutral500,
                    unselectedTextColor = NeutralColors.Neutral500,
                    indicatorColor = PrimaryColors.Primary50
                )
            )
        }
    }
}