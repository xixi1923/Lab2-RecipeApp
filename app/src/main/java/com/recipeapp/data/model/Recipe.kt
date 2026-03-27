package com.recipeapp.data.model

data class Recipe(
    val id: Int,
    val title: String,
    val image: String,
    val summary: String? = null
)

data class RecipeResponse(
    val results: List<Recipe>
)