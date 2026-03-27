package com.recipeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.recipeapp.ui.navigation.RecipeNavGraph
import com.recipeapp.ui.theme.RecipeAppTheme
import com.recipeapp.viewmodel.OnboardingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RecipeAppTheme {
                val navController = rememberNavController()
                val onboardingViewModel: OnboardingViewModel = hiltViewModel()

                // collectAsStateWithLifecycle ensures state is managed safely with the UI lifecycle
                val hasSeenOnboarding by onboardingViewModel.hasSeenOnboarding.collectAsStateWithLifecycle(
                    initialValue = null
                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (hasSeenOnboarding) {
                        null -> LoadingScreen() // Wait for DataStore initialization
                        true -> {
                            // User finished onboarding: Show main app shell with Bottom Bar
                            MainScaffold(
                                navController = navController,
                                startWithOnboarding = false
                            )
                        }
                        false -> {
                            // First time user: Show Onboarding flow
                            RecipeNavGraph(
                                navController = navController,
                                startWithOnboarding = true
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainScaffold(
    navController: NavHostController,
    startWithOnboarding: Boolean,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Navigation items for the Bottom Bar
    val items = listOf(
        BottomNavItem(route = "home", label = "Home", icon = Icons.Default.Home),
        BottomNavItem(route = "explore", label = "Explore", icon = Icons.Default.Explore),
        BottomNavItem(route = "favorites", label = "Favorites", icon = Icons.Default.Favorite)
    )

    // Only show bottom bar if current screen is one of the main tabs
    val showBottomBar = items.any { it.route == currentDestination?.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    items.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            // FIX: Ensure label is never null to prevent NullPointerException in Text()
                            label = { Text(text = item.label) },
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(item.route) {
                                        // Pop up to the start destination to avoid building up a large stack
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // innerPadding prevents content from being hidden behind the Bottom Bar
        Box(modifier = Modifier.padding(innerPadding)) {
            RecipeNavGraph(
                navController = navController,
                startWithOnboarding = startWithOnboarding
            )
        }
    }
}

/**
 * Data class representing a tab in the bottom navigation bar.
 */
data class BottomNavItem(
    val route: String,
    val label: String, // Must not be null
    val icon: ImageVector
)

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}