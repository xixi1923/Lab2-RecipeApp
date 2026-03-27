package com.recipeapp.data.repository

import com.recipeapp.data.api.MealApiService
import com.recipeapp.data.db.FavouriteMealDao
import com.recipeapp.data.model.Category
import com.recipeapp.data.model.FavouriteMeal
import com.recipeapp.data.model.Meal
import com.recipeapp.data.model.toFavouriteMeal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealRepository @Inject constructor(
    private val api: MealApiService,
    private val dao: FavouriteMealDao,
) {

    /**
     * Searches meals using the API.
     */
    suspend fun searchMeals(query: String): Result<List<Meal>> = withContext(Dispatchers.IO) {
        try {
            val response = api.searchMeals(query)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("API Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches details for a single meal by its ID.
     */
    suspend fun getMealById(id: String): Result<Meal?> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMealDetails(id)
            if (response.isSuccessful) {
                // API returns a list, extract the first item
                val meal = response.body()?.firstOrNull()
                Result.success(meal)
            } else {
                Result.failure(Exception("API Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets meals for the Home Screen "Featured Daily" / "Latest" section.
     */
    suspend fun getLatestRecipes(): Result<List<Meal>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAllMeals()
            if (response.isSuccessful) {
                Result.success(response.body()?.take(10) ?: emptyList())
            } else {
                Result.failure(Exception("API Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets meals for the "Popular Choices" section.
     */
    suspend fun getPopularRecipes(): Result<List<Meal>> = withContext(Dispatchers.IO) {
        try {
            // Using "Seafood" as a default popular category filter
            val response = api.getMealsByCategory("Seafood")
            if (response.isSuccessful) {
                Result.success(response.body()?.take(10) ?: emptyList())
            } else {
                Result.failure(Exception("Popular API Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches meals belonging to a specific category.
     */
    suspend fun getMealsByCategory(category: String): Result<List<Meal>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMealsByCategory(category)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Category Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches all available meal categories for the horizontal category list.
     * FIXED: Removed the incorrect TensorFlow import.
     */
    suspend fun getCategories(): Result<List<Category>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getCategories()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Categories Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- DATABASE OPERATIONS ---

    fun getAllFavourites(): Flow<List<FavouriteMeal>> = dao.getAllFavourites()

    suspend fun addFavourite(meal: Meal) = withContext(Dispatchers.IO) {
        dao.insert(meal.toFavouriteMeal())
    }

    suspend fun removeFavourite(mealId: String) = withContext(Dispatchers.IO) {
        dao.deleteById(mealId)
    }

    suspend fun isFavourite(id: String): Boolean = withContext(Dispatchers.IO) {
        dao.isFavourite(id)
    }

    suspend fun toggleFavourite(meal: Meal) = withContext(Dispatchers.IO) {
        if (dao.isFavourite(meal.idMeal)) {
            dao.deleteById(meal.idMeal)
        } else {
            dao.insert(meal.toFavouriteMeal())
        }
    }
}