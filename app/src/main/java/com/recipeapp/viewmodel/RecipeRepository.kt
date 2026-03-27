package com.recipeapp.data.repository

import com.recipeapp.data.api.MealApiService
import com.recipeapp.data.db.FavouriteMealDao
import com.recipeapp.data.model.Category
import com.recipeapp.data.model.FavouriteMeal
import com.recipeapp.data.model.Meal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRepository @Inject constructor(
    private val apiService: MealApiService,
    private val favouriteMealDao: FavouriteMealDao,
) {

    fun getAllFavourites(): Flow<List<FavouriteMeal>> =
        favouriteMealDao.getAllFavourites()

    fun getFavouriteById(id: String): Flow<FavouriteMeal?> =
        favouriteMealDao.getFavouriteById(id)

    suspend fun addFavourite(meal: Meal) {
        val mealId = meal.idMeal ?: return
        val fav = FavouriteMeal(
            idMeal = mealId,
            strMeal = meal.strMeal,
            strMealThumb = meal.imageUrl ?: "",
            strCategory = meal.strCategory ?: "",
            strArea = meal.strArea ?: "",
            strInstructions = meal.strInstructions ?: ""
        )
        favouriteMealDao.insert(fav)
    }

    suspend fun removeFavourite(id: String) {
        favouriteMealDao.deleteById(id)
    }

    suspend fun isFavourite(id: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext favouriteMealDao.getFavouriteById(id).firstOrNull() != null
    }

    private suspend fun <T, R> safeApiCall(
        call: suspend () -> Response<T>,
        transform: (T) -> R,
    ): Result<R> {
        return try {
            val response = withContext(Dispatchers.IO) { call() }
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(transform(body))
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("API Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllMeals(): Result<List<Meal>> = safeApiCall(
        call = { apiService.getAllMeals() },
        transform = { it }
    )

    suspend fun getMealById(id: String): Result<Meal?> = safeApiCall(
        call = { apiService.getMealDetails(id) },
        transform = { meals -> meals.firstOrNull() }
    )

    suspend fun getLatestRecipes(): Result<List<Meal>> = safeApiCall(
        call = { apiService.getAllMeals() },
        transform = { it.take(10) }
    )

    suspend fun getPopularRecipes(): Result<List<Meal>> = safeApiCall(
        call = { apiService.getMealsByCategory("Seafood") },
        transform = { it.take(10) }
    )

    suspend fun getMealsByCategory(category: String): Result<List<Meal>> = safeApiCall(
        call = { apiService.getMealsByCategory(category) },
        transform = { it }
    )

    suspend fun searchMeals(query: String): Result<List<Meal>> = safeApiCall(
        call = { apiService.searchMeals(query) },
        transform = { it }
    )

    suspend fun getCategories(): Result<List<Category>> = safeApiCall(
        call = { apiService.getCategories() },
        transform = { it }
    )
}
