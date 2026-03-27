package com.recipeapp.ui.meallist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.recipeapp.viewmodel.MealListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealListScreen(
    filterType: String,
    filterValue: String,
    onMealClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: MealListViewModel = hiltViewModel()
) {
    // Collecting state safely with lifecycle awareness
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Trigger data fetch when screen parameters change
    LaunchedEffect(filterType, filterValue) {
        viewModel.fetchMeals(filterType, filterValue)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = filterValue,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.error ?: "An unexpected error occurred",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.fetchMeals(filterType, filterValue) }) {
                            Text("Retry")
                        }
                    }
                }
                uiState.meals.isEmpty() -> {
                    Text(
                        text = "No meals found",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = uiState.meals,
                            key = { it.idMeal } // Use idMeal as unique key for grid performance
                        ) { meal ->
                            MealItem(
                                name = meal.strMeal,
                                imageUrl = meal.imageUrl, // Uses the helper property from Meal.kt
                                onClick = { onMealClick(meal.idMeal) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MealItem(
    name: String,
    imageUrl: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentScale = ContentScale.Crop
            )
            Text(
                text = name,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2, // Allow 2 lines for longer names
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold,
                minLines = 2 // Keep cards consistent height
            )
        }
    }
}