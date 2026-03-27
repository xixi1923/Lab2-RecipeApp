package com.recipeapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipeapp.data.model.Meal
import com.recipeapp.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MealUiState {
    data object Loading : MealUiState()
    data class Success(val meal: Meal) : MealUiState()
    data class Error(val message: String) : MealUiState()
}

@HiltViewModel
class MealDetailViewModel @Inject constructor(
    private val repository: RecipeRepository
) : ViewModel() {

    private val _mealState = MutableStateFlow<MealUiState>(MealUiState.Loading)
    val mealState: StateFlow<MealUiState> = _mealState.asStateFlow()

    private val _isFavourite = MutableStateFlow(false)
    val isFavourite: StateFlow<Boolean> = _isFavourite.asStateFlow()

    private var currentMeal: Meal? = null

    /**
     * Loads meal details and synchronizes with the local database.
     */
    fun loadMeal(id: String) {
        // Only skip if already loaded AND the state is Success
        if (currentMeal?.idMeal == id && _mealState.value is MealUiState.Success) return

        viewModelScope.launch {
            _mealState.value = MealUiState.Loading

            // Start observing favorite status immediately so UI updates
            // even if the Network call is slow or failing.
            observeFavouriteStatus(id)

            repository.getMealById(id).onSuccess { meal ->
                if (meal != null) {
                    currentMeal = meal
                    _mealState.value = MealUiState.Success(meal)
                } else {
                    _mealState.value = MealUiState.Error("Meal details not available.")
                }
            }.onFailure { e ->
                _mealState.value = MealUiState.Error(
                    e.localizedMessage ?: "Failed to connect to server"
                )
            }
        }
    }

    /**
     * Observes the favorite status of the meal in real-time from the database.
     */
    private fun observeFavouriteStatus(id: String) {
        viewModelScope.launch {
            repository.getFavouriteById(id)
                .distinctUntilChanged()
                .collect { fav ->
                    _isFavourite.value = fav != null
                    // If we found it in DB but currentMeal is null,
                    // we could technically map FavouriteMeal back to Meal here if needed.
                }
        }
    }

    /**
     * Toggles the favorite status of the current meal.
     */
    fun toggleFavourite() {
        // Safely check for meal and ID existence
        val meal = currentMeal ?: return
        val mealId = meal.idMeal

        if (mealId.isBlank()) return

        viewModelScope.launch {
            try {
                if (_isFavourite.value) {
                    repository.removeFavourite(mealId)
                } else {
                    repository.addFavourite(meal)
                }
            } catch (e: Exception) {
                // You could optionally emit an error state here
            }
        }
    }
}