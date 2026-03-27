package com.recipeapp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.recipeapp.data.model.Category
import com.recipeapp.data.model.Meal
import com.recipeapp.ui.components.MealListItem
import com.recipeapp.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onMealClick: (String) -> Unit,
    onCategoryClick: (String, String) -> Unit,
    onSearchClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    // Collect UI state from ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Show error snackbars if API fails
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "CHEF'S PALETTE",
                        style = MaterialTheme.typography.titleSmall,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(onClick = { /* Notifications Logic */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (uiState.isLoading && uiState.categories.isEmpty()) {
                // Initial Loading State
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    // 1. Welcome Header
                    item { WelcomeHeroSection(onSearchClick) }

                    // 2. Featured Daily Section (API Random Meal)
                    uiState.randomMeal?.let { meal ->
                        item {
                            SectionHeader(title = "Featured Daily 🧑‍🍳", onSeeAllClick = {})
                            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                                val isFav = favorites.any { it.idMeal == meal.idMeal }
                                MealListItem(
                                    meal = meal,
                                    isFavorite = isFav,
                                    onFavoriteToggle = { viewModel.toggleFavorite(meal, isFav) },
                                    onClick = {
                                        // FIX: Use .let to ensure non-null ID
                                        meal.idMeal?.let { id -> onMealClick(id) }
                                    }
                                )
                            }
                            Spacer(Modifier.height(24.dp))
                        }
                    }

                    // 3. Categories Section (Horizontal Row)
                    item {
                        SectionHeader(title = "Categories 🍕", onSeeAllClick = onSearchClick)
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.categories) { category ->
                                CategoryCard(category = category) {
                                    onCategoryClick(category.strCategory, "Category")
                                }
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                    }

                    // 4. Popular Choices Section (Vertical List)
                    item {
                        SectionHeader(title = "Popular Choices 🔥", onSeeAllClick = onSearchClick)
                    }

                    if (uiState.popularMeals.isEmpty() && !uiState.isLoading) {
                        item { EmptyHomeState(onRetry = { viewModel.loadHomeData() }) }
                    } else {
                        items(uiState.popularMeals, key = { it.idMeal ?: it.strMeal }) { meal ->
                            val isFav = favorites.any { it.idMeal == meal.idMeal }
                            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                                MealListItem(
                                    meal = meal,
                                    isFavorite = isFav,
                                    onFavoriteToggle = { viewModel.toggleFavorite(meal, isFav) },
                                    onClick = {
                                        // FIX: Use .let to ensure non-null ID
                                        meal.idMeal?.let { id -> onMealClick(id) }
                                    }
                                )
                            }
                        }
                    }
                    item { Spacer(Modifier.height(20.dp)) }
                }
            }
        }
    }
}

@Composable
fun WelcomeHeroSection(onSearchClick: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text("Welcome back!", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
        Text("Ready to cook\nsomething new?", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, lineHeight = 40.sp)
        Spacer(Modifier.height(16.dp))
        Surface(
            onClick = onSearchClick,
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Text("Search recipes, ingredients...", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun CategoryCard(category: Category, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            )
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.size(70.dp)
        ) {
            AsyncImage(
                model = category.strCategoryThumb,
                contentDescription = category.strCategory,
                modifier = Modifier.padding(12.dp).clip(CircleShape),
                contentScale = ContentScale.Fit
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(text = category.strCategory, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
fun SectionHeader(title: String, onSeeAllClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        TextButton(onClick = onSeeAllClick) {
            Text("See all", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun EmptyHomeState(onRetry: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("No recipes found.", color = MaterialTheme.colorScheme.outline)
        IconButton(onClick = onRetry) { Icon(Icons.Default.Refresh, contentDescription = "Retry") }
    }
}