package com.recipeapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipeapp.data.model.Category
import com.recipeapp.data.model.FavouriteMeal
import com.recipeapp.data.model.Meal
import com.recipeapp.data.model.toFavouriteMeal
import com.recipeapp.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI state for Home screen
data class HomeUiState(
    val isLoading: Boolean = false,
    val categories: List<Category> = emptyList(),
    val latestMeals: List<Meal> = emptyList(),
    val popularMeals: List<Meal> = emptyList(),
    val randomMeal: Meal? = null,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Favorites list from database
    val favorites: StateFlow<List<FavouriteMeal>> = repository.getAllFavourites()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadHomeData()
    }

    // Load latest recipes and categories
    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val latestResult = repository.getLatestRecipes()
            val categoriesResult = repository.getCategories()

            // Handle latest meals
            latestResult.onSuccess { allMeals ->
                val sanitizedMeals = allMeals.mapIndexed { index, meal ->
                    if (meal.idMeal.isBlank()) {
                        meal.copy(idMeal = "temp_home_${index}_${System.currentTimeMillis()}")
                    } else {
                        meal
                    }
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        latestMeals = sanitizedMeals.take(10),
                        popularMeals = sanitizedMeals.shuffled().take(10),
                        randomMeal = sanitizedMeals.randomOrNull()
                    )
                }
            }.onFailure { exception ->
                _uiState.update { it.copy(isLoading = false, error = exception.localizedMessage) }
            }

            // Handle categories
            categoriesResult.onSuccess { categories ->
                _uiState.update { it.copy(categories = categories) }
            }.onFailure { exception ->
                _uiState.update { it.copy(error = exception.localizedMessage) }
            }
        }
    }

    // Add or remove favorite
    fun toggleFavorite(meal: Meal, isFavorite: Boolean) {
        val mealId = meal.idMeal.takeIf { it.isNotBlank() } ?: return
        viewModelScope.launch {
            try {
                if (isFavorite) {
                    repository.removeFavourite(mealId)
                } else {
                    repository.addFavourite(meal)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Database Error: ${e.message}") }
            }
        }
    }

    // Clear any error in UI
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}