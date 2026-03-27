package com.recipeapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.recipeapp.ui.home.HomeScreen
import com.recipeapp.ui.detail.MealDetailScreen
import com.recipeapp.ui.explore.ExploreScreen
import com.recipeapp.ui.onboarding.OnboardingScreen
import com.recipeapp.ui.favourite.FavouriteScreen

/**
 * Sealed class for type-safe routing throughout the app.
 * IMPORTANT: These routes must match the routes used in MainActivity's NavigationBar.
 */
sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Explore : Screen("explore")
    object Favourite : Screen("favorites") // Changed from "favourite" to match typical BottomBar logic
    object Details : Screen("meal_details/{mealId}") {
        fun createRoute(mealId: String) = "meal_details/$mealId"
    }
}

@Composable
fun RecipeNavGraph(
    navController: NavHostController,
    startWithOnboarding: Boolean,
    modifier: Modifier = Modifier,
) {
    // Determine the starting point (Onboarding or Home)
    val startDestination = if (startWithOnboarding) {
        Screen.Onboarding.route
    } else {
        Screen.Home.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // 1. Onboarding Screen
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // 2. Home Screen
        composable(Screen.Home.route) {
            HomeScreen(
                onMealClick = { id ->
                    if (!id.isNullOrEmpty()) {
                        navController.navigate(Screen.Details.createRoute(id))
                    }
                },
                onCategoryClick = { _, _ ->
                    // Navigate to Explore when a category is clicked
                    navController.navigate(Screen.Explore.route)
                },
                onSearchClick = {
                    navController.navigate(Screen.Explore.route)
                }
            )
        }

        // 3. Explore Screen (API Search results)
        composable(Screen.Explore.route) {
            ExploreScreen(
                onMealClick = { id ->
                    if (!id.isNullOrEmpty()) {
                        navController.navigate(Screen.Details.createRoute(id))
                    }
                }
            )
        }

        // 4. Favourite Screen (Local Room Database items)
        composable(Screen.Favourite.route) {
            FavouriteScreen(
                onMealClick = { id ->
                    if (!id.isNullOrEmpty()) {
                        navController.navigate(Screen.Details.createRoute(id))
                    }
                }
            )
        }

        // 5. Meal Detail Screen (API Fetch by ID)
        composable(
            route = Screen.Details.route,
            arguments = listOf(
                navArgument("mealId") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val mealId = backStackEntry.arguments?.getString("mealId")

            if (!mealId.isNullOrEmpty()) {
                MealDetailScreen(
                    mealId = mealId,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            } else {
                // If ID is null, safely return to home
                navController.popBackStack()
            }
        }
    }
}