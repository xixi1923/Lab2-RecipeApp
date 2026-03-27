package com.recipeapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipeapp.data.model.Meal
import com.recipeapp.data.model.FavouriteMeal
import com.recipeapp.data.repository.MealRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for the Meal List Screen
 */
data class MealListUiState(
    val title: String = "",
    val meals: List<Meal> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MealListViewModel @Inject constructor(
    private val repository: MealRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // These keys must match your navigation route parameters
    private val filterType: String = savedStateHandle["filterType"] ?: "Category"
    private val filterValue: String = savedStateHandle["filterValue"] ?: ""

    private val _uiState = MutableStateFlow(
        MealListUiState(
            title = filterValue,
            isLoading = true
        )
    )
    val uiState: StateFlow<MealListUiState> = _uiState.asStateFlow()

    // Observe favorites from the local database
    val favorites: StateFlow<List<FavouriteMeal>> = repository.getAllFavourites()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadMeals()
    }

    /**
     * Calls the repository to fetch data from the API based on the filter
     */
    fun loadMeals() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Determine which repository method to call based on filterType
            val result = when (filterType.lowercase()) {
                "category" -> repository.getMealsByCategory(filterValue)
                "area" -> repository.getMealsByCategory(filterValue) // Fallback if getByArea isn't ready
                else -> repository.getLatestRecipes()
            }

            result.onSuccess { fetchedMeals ->
                // CRITICAL FIX: Sanitize IDs to prevent "Key already used" crash in LazyGrid
                val sanitizedMeals = fetchedMeals.mapIndexed { index, meal ->
                    if (meal.idMeal.isBlank()) {
                        meal.copy(idMeal = "temp_list_${index}_${System.currentTimeMillis()}")
                    } else {
                        meal
                    }
                }

                _uiState.update {
                    it.copy(
                        meals = sanitizedMeals,
                        isLoading = false,
                        error = if (sanitizedMeals.isEmpty()) "No recipes found for $filterValue" else null
                    )
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        error = exception.localizedMessage ?: "Failed to load recipes",
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * This is used if the MealListScreen needs to trigger a manual refresh or update
     */
    fun fetchMeals(type: String, value: String) {
        // Only reload if the data is empty or the filter parameters have changed
        if (_uiState.value.meals.isEmpty() || value != filterValue) {
            loadMeals()
        }
    }

    /**
     * Toggles favorite status in the database
     */
    fun toggleFavorite(meal: Meal, isFavorite: Boolean) {
        val mealId = meal.idMeal
        if (mealId.isBlank()) return

        viewModelScope.launch {
            try {
                if (isFavorite) {
                    repository.removeFavourite(mealId)
                } else {
                    repository.addFavourite(meal)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Could not update favorites") }
            }
        }
    }
}