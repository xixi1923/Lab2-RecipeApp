package com.recipeapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.recipeapp.data.model.FavouriteMeal

@Database(
    entities = [FavouriteMeal::class],
    version = 2, // Incremented to 2 to ensure schema updates are recognized
    exportSchema = false
)
abstract class RecipeDatabase : RoomDatabase() {

    abstract fun favouriteMealDao(): FavouriteMealDao

    companion object {
        // The name of the database file stored on the device
        const val DATABASE_NAME = "recipe_app_db"
    }
}