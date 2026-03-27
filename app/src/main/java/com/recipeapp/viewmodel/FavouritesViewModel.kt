package com.recipeapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipeapp.data.model.FavouriteMeal
import com.recipeapp.data.repository.MealRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * State class to hold the UI data for the Favorites screen.
 */
data class FavouriteUiState(
    val favouriteMeals: List<FavouriteMeal> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FavouriteViewModel @Inject constructor(
    private val repository: MealRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Used to force a refresh if necessary (though Room Flow updates automatically)
    private val _refreshTrigger = MutableStateFlow(0)

    /**
     * The UI State flow.
     * flatMapLatest: Switches to the newest Flow from the repository whenever the trigger changes.
     * combine: Merges the database results with the user's search query in real-time.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<FavouriteUiState> = _refreshTrigger
        .flatMapLatest {
            repository.getAllFavourites()
        }
        .combine(_searchQuery) { meals, query ->
            val filteredMeals = if (query.isBlank()) {
                meals
            } else {
                meals.filter {
                    it.strMeal.contains(query, ignoreCase = true) ||
                            it.strCategory?.contains(query, ignoreCase = true) == true
                }
            }
            FavouriteUiState(
                favouriteMeals = filteredMeals,
                isLoading = false,
                error = null
            )
        }
        .catch { e ->
            emit(FavouriteUiState(error = e.localizedMessage ?: "Database Error", isLoading = false))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FavouriteUiState(isLoading = true)
        )

    /**
     * Updates the search query to filter favorite meals.
     */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    /**
     * Deletes a meal from the local Room database by its ID.
     */
    fun removeFromFavorites(mealId: String) {
        viewModelScope.launch {
            try {
                repository.removeFavourite(mealId)
            } catch (e: Exception) {
                // Error handling is handled by the .catch operator in uiState
            }
        }
    }

    /**
     * Call this function to manually refresh the list if needed.
     */
    fun refresh() {
        _refreshTrigger.value += 1
    }
}