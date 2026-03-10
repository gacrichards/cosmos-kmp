package com.gacrichards.cosmos.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gacrichards.cosmos.ui.archive.ArchiveScreen
import com.gacrichards.cosmos.ui.detail.MediaDetailScreen
import com.gacrichards.cosmos.ui.epic.EpicScreen
import com.gacrichards.cosmos.ui.today.TodayScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

private const val ROUTE_TODAY = "today"
private const val ROUTE_ARCHIVE = "archive"
private const val ROUTE_EARTH = "earth"
private const val ROUTE_MEDIA_DETAIL = "media_detail/{date}"
private val BOTTOM_NAV_ROUTES = setOf(ROUTE_TODAY, ROUTE_ARCHIVE, ROUTE_EARTH)

@Composable
fun CosmosApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { NavIcon("✦") },
                    label = { Text("Today") },
                    selected = currentRoute == ROUTE_TODAY,
                    onClick = {
                        navController.navigate(ROUTE_TODAY) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
                NavigationBarItem(
                    icon = { NavIcon("⊞") },
                    label = { Text("Archive") },
                    selected = currentRoute == ROUTE_ARCHIVE,
                    onClick = {
                        navController.navigate(ROUTE_ARCHIVE) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
                NavigationBarItem(
                    icon = { NavIcon("◎") },
                    label = { Text("Earth") },
                    selected = currentRoute == ROUTE_EARTH,
                    onClick = {
                        navController.navigate(ROUTE_EARTH) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ROUTE_TODAY,
        ) {
            composable(ROUTE_TODAY) {
                TodayScreen(
                    viewModel = koinViewModel(),
                    contentPadding = innerPadding,
                    onImageClick = { date -> navController.navigate("media_detail/$date") },
                )
            }
            composable(ROUTE_ARCHIVE) {
                ArchiveScreen(
                    viewModel = koinViewModel(),
                    onApodClick = { date -> navController.navigate("media_detail/$date") },
                    modifier = Modifier.padding(innerPadding),
                )
            }
            composable(ROUTE_EARTH) {
                EpicScreen(
                    viewModel = koinViewModel(),
                    modifier = Modifier.padding(innerPadding),
                )
            }
            composable(
                route = ROUTE_MEDIA_DETAIL,
                arguments = listOf(navArgument("date") { type = NavType.StringType }),
            ) { backStack ->
                val date = backStack.arguments?.getString("date") ?: return@composable
                MediaDetailScreen(
                    viewModel = koinViewModel(parameters = { parametersOf(date) }),
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

@Composable
private fun NavIcon(symbol: String) {
    Box(contentAlignment = Alignment.Center) {
        Text(text = symbol, fontSize = 18.sp)
    }
}
