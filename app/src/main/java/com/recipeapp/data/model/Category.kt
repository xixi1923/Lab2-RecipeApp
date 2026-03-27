package com.recipeapp.data.model

import com.google.gson.annotations.SerializedName

data class Category(
    @SerializedName(value = "idCategory", alternate = ["id"])
    val idCategory: String,

    @SerializedName(value = "strCategory", alternate = ["category"])
    val strCategory: String,

    @SerializedName(value = "strCategoryThumb", alternate = ["category_thumb"])
    val strCategoryThumb: String, // This fixes the 'parameter not found' error

    @SerializedName(value = "strCategoryDescription", alternate = ["category_description"])
    val strCategoryDescription: String? = null
)

data class CategoryResponse(
    val categories: List<Category>? = null
)