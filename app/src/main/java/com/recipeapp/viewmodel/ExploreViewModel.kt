package com.recipeapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipeapp.data.model.FavouriteMeal
import com.recipeapp.data.model.Meal
import com.recipeapp.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExploreUiState(
    val meals: List<Meal> = emptyList(),
    val isLoading: Boolean = false,
    val selectedCategory: String = "All",
    val error: String? = null
)

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val repository: RecipeRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _remoteMeals = MutableStateFlow<List<Meal>>(emptyList())

    val uiState: StateFlow<ExploreUiState> = combine(
        _remoteMeals,
        _selectedCategory,
        _isLoading,
        _error
    ) { meals, category, loading, error ->
        ExploreUiState(
            meals = meals,
            isLoading = loading,
            selectedCategory = category,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExploreUiState(isLoading = true)
    )

    val favorites: StateFlow<List<FavouriteMeal>> = repository.getAllFavourites()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        fetchInitialRecipes()
        setupSearchDebounce()
    }

    private fun fetchInitialRecipes() {
        fetchData { repository.getLatestRecipes() }
    }

    @OptIn(FlowPreview::class)
    private fun setupSearchDebounce() {
        viewModelScope.launch {
            _searchQuery
                .debounce(500)
                .filter { it.isNotBlank() }
                .distinctUntilChanged()
                .collect { query ->
                    _selectedCategory.value = "All"
                    fetchData { repository.searchMeals(query) }
                }
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
        if (newQuery.isBlank()) {
            if (_selectedCategory.value == "All") {
                fetchInitialRecipes()
            } else {
                fetchData { repository.getMealsByCategory(_selectedCategory.value) }
            }
        }
    }

    fun onCategorySelected(category: String) {
        if (_selectedCategory.value == category) return
        _selectedCategory.value = category
        _searchQuery.value = ""
        _error.value = null
        if (category == "All") {
            fetchInitialRecipes()
        } else {
            fetchData { repository.getMealsByCategory(category) }
        }
    }

    private fun fetchData(call: suspend () -> Result<List<Meal>>) {
        viewModelScope.launch {
            _isLoading.value = true
            call().onSuccess { meals ->
                // FIX: Sanitize IDs to prevent "Key already used" crash.
                // If idMeal is empty, we assign a temporary unique one.
                val sanitizedMeals = meals.mapIndexed { index, meal ->
                    if (meal.idMeal.isBlank()) {
                        meal.copy(idMeal = "temp_id_${index}_${System.currentTimeMillis()}")
                    } else {
                        meal
                    }
                }
                _remoteMeals.value = sanitizedMeals
                _error.value = null
            }.onFailure { e ->
                _error.value = e.localizedMessage ?: "Failed to load recipes"
            }
            _isLoading.value = false
        }
    }

    fun toggleFavorite(meal: Meal, isFavorite: Boolean) {
        viewModelScope.launch {
            try {
                if (isFavorite) {
                    // FIX: Ensure ID is not blank before calling repository
                    if (meal.idMeal.isNotBlank()) {
                        repository.removeFavourite(meal.idMeal)
                    }
                } else {
                    repository.addFavourite(meal)
                }
            } catch (e: Exception) {
                _error.value = "Failed to update favorites"
            }
        }
    }
}