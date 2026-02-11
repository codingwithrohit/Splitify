package com.example.splitify.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
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
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.splitify.data.local.SessionManager
import com.example.splitify.domain.repository.AuthRepository
import com.example.splitify.presentation.profile.AboutScreen
import com.example.splitify.presentation.profile.AccountSettingsScreen
import com.example.splitify.presentation.profile.AppSettingsScreen
import com.example.splitify.presentation.profile.DeveloperScreen
import com.example.splitify.presentation.profile.ProfileScreen
import com.example.splitify.presentation.theme.NeutralColors
import com.example.splitify.presentation.theme.PrimaryColors
import com.example.splitify.presentation.trips.TripsScreen
import com.example.splitify.presentation.trips.TripsTopBar


@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun MainScreen(
    onLogOut: () -> Unit,
    onNavigateToTripDetail: (String) -> Unit,
    onNavigateToCreateTrip: () -> Unit,
    onNavigateToJoinTrip: () -> Unit,
    sessionManager: SessionManager,
    authRepository: AuthRepository,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        BottomNavItem.Trips.route,
        Screen.Profile.route
    )

    Scaffold(
        topBar = {
            when (currentRoute) {
                BottomNavItem.Trips.route -> {
                    TripsTopBar(onJoinTripClick = onNavigateToJoinTrip)
                }
                else -> {}
            }
        },
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController = navController)
            }
        },
        floatingActionButton = {
            if (currentRoute == BottomNavItem.Trips.route) {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToCreateTrip,
                    icon = { Icon(Icons.Default.Add, "Add trip") },
                    text = { Text("New Trip", fontWeight = FontWeight.Bold) },
                    containerColor = PrimaryColors.Primary600,
                    contentColor = Color.White
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Trips.route,
            route = "main_graph",
            modifier = Modifier
                .fillMaxSize()
        ) {

            composable(BottomNavItem.Trips.route) {
                TripsScreen(
                    modifier = Modifier.padding(paddingValues),
                    onCreateTripClick = onNavigateToCreateTrip,
                    onTripClick = onNavigateToTripDetail,
                    onJoinTripClick = onNavigateToJoinTrip,
                    onLogOut = onLogOut
                )
            }

            navigation(
                startDestination = Screen.Profile.route,
                route = BottomNavItem.Profile.route
            ) {
                composable(Screen.Profile.route) {
                    ProfileScreen(
                        onNavigateToAccountSettings = {
                            navController.navigate(Screen.AccountSettings.route)
                        },
                        onNavigateToAppSettings = {
                            navController.navigate(Screen.AppSettings.route)
                        },
                        onNavigateToAbout = {
                            navController.navigate(Screen.About.route)
                        },
                        onNavigateToDeveloper = {
                            navController.navigate(Screen.Developer.route)
                        },
                        onLogOut = onLogOut,
                        sessionManager = sessionManager
                    )
                }

                composable(Screen.AccountSettings.route) {
                    AccountSettingsScreen(
                        onBack = { navController.navigateUp() },
                        onLogOut = onLogOut
                    )
                }

                composable(Screen.AppSettings.route) {
                    AppSettingsScreen(
                        onBack = { navController.navigateUp() }
                    )
                }

                composable(Screen.About.route) {
                    AboutScreen(
                        onBack = { navController.navigateUp() }
                    )
                }

                composable(Screen.Developer.route) {
                    DeveloperScreen(
                        onBack = { navController.navigateUp() }
                    )
                }
            }
        }
    }
}




@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        windowInsets = WindowInsets(0),
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
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