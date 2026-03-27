package com.recipeapp.data.api

import com.recipeapp.data.model.Category
import com.recipeapp.data.model.Meal
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MealApiService {

    /**
     * Fetch all meals.
     * Note: If your API returns { "meals": [...] }, the return type must be MealResponse.
     * If it returns a raw list [...], keep it as List<Meal>.
     * Based on standard Vercel MealDB clones, List<Meal> is used here.
     */
    @GET("meals")
    suspend fun getAllMeals(): Response<List<Meal>>

    /**
     * Fetch meal details by ID.
     */
    @GET("meals")
    suspend fun getMealDetails(@Query("id") id: String): Response<List<Meal>>

    /**
     * Search meals by name.
     */
    @GET("meals")
    suspend fun searchMeals(
        @Query("meal_like") query: String
    ): Response<List<Meal>>

    /**
     * Filter meals by category name.
     */
    @GET("meals")
    suspend fun getMealsByCategory(
        @Query("category") category: String
    ): Response<List<Meal>>

    /**
     * Fetch the list of meal categories.
     */
    @GET("categories")
    suspend fun getCategories(): Response<List<Category>>
}