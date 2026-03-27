package com.recipeapp.data.model

import com.google.gson.annotations.SerializedName

data class Meal(
    @SerializedName(value = "idMeal", alternate = ["id"])
    val idMeal: String,

    @SerializedName(value = "strMeal", alternate = ["meal"])
    val strMeal: String,

    @SerializedName(value = "strMealThumb", alternate = ["meal_thumb"])
    val strMealThumb: String,

    @SerializedName(value = "strCategory", alternate = ["category"])
    val strCategory: String? = null,

    @SerializedName(value = "strArea", alternate = ["area"])
    val strArea: String? = null,

    @SerializedName(value = "strInstructions", alternate = ["instructions"])
    val strInstructions: String? = null,

    @SerializedName(value = "strYoutube", alternate = ["youtube"])
    val strYoutube: String? = null,

    @SerializedName("ingredients")
    val ingredientItems: List<IngredientItem>? = null,

    @SerializedName("strIngredient1") val strIngredient1: String? = null,
    @SerializedName("strIngredient2") val strIngredient2: String? = null,
    @SerializedName("strIngredient3") val strIngredient3: String? = null,
    @SerializedName("strIngredient4") val strIngredient4: String? = null,
    @SerializedName("strIngredient5") val strIngredient5: String? = null,

    @SerializedName("strMeasure1") val strMeasure1: String? = null,
    @SerializedName("strMeasure2") val strMeasure2: String? = null,
    @SerializedName("strMeasure3") val strMeasure3: String? = null,
    @SerializedName("strMeasure4") val strMeasure4: String? = null,
    @SerializedName("strMeasure5") val strMeasure5: String? = null
) {
    // Helper property for UI compatibility
    val imageUrl: String get() = strMealThumb

    // Safely get ingredients with measures
    @JvmName("getIngredientsList") // This renames the function at the bytecode level
    fun getIngredients(): List<Pair<String, String>> {
        val structuredIngredients = ingredientItems
            ?.mapNotNull { item ->
                item.ingredient?.takeIf { it.isNotBlank() }?.let { name ->
                    name to (item.measure ?: "")
                }
            }
            .orEmpty()

        if (structuredIngredients.isNotEmpty()) {
            return structuredIngredients
        }

        val fallbackIngredients = listOf(strIngredient1, strIngredient2, strIngredient3, strIngredient4, strIngredient5)
        val measures = listOf(strMeasure1, strMeasure2, strMeasure3, strMeasure4, strMeasure5)

        return fallbackIngredients.zip(measures)
            .mapNotNull { (ingredient, measure) ->
                ingredient?.takeIf { it.isNotBlank() }?.let { it to (measure ?: "") }
            }
    }
}

data class IngredientItem(
    @SerializedName("ingredient")
    val ingredient: String? = null,
    @SerializedName("measure")
    val measure: String? = null,
)

// Extension to convert Meal to FavouriteMeal for database
fun Meal.toFavouriteMeal(): FavouriteMeal {
    return FavouriteMeal(
        idMeal = this.idMeal,
        strMeal = this.strMeal,
        strMealThumb = this.strMealThumb,
        strCategory = this.strCategory ?: "",
        strArea = this.strArea ?: "",
        strInstructions = this.strInstructions ?: "",
        strYoutube = this.strYoutube,
        strIngredient1 = this.strIngredient1,
        strIngredient2 = this.strIngredient2,
        strIngredient3 = this.strIngredient3,
        strIngredient4 = this.strIngredient4,
        strIngredient5 = this.strIngredient5,
        strMeasure1 = this.strMeasure1,
        strMeasure2 = this.strMeasure2,
        strMeasure3 = this.strMeasure3,
        strMeasure4 = this.strMeasure4,
        strMeasure5 = this.strMeasure5
    )
}