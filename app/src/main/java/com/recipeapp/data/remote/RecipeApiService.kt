package com.recipeapp.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

// Replace 'MealResponse' with your actual data model name
interface RecipeApiService {
    @GET("api/json/v1/1/search.php") // Example for TheMealDB
    suspend fun searchMeals(
        @Query("s") query: String
    ): MealResponse
}

// Data model for the API response
data class MealResponse(
    val meals: List<Map<String, String>>? // Generic map to test if data comes through
)