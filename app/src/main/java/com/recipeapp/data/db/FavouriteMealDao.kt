package com.recipeapp.data.db

import androidx.room.*
import com.recipeapp.data.model.FavouriteMeal
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteMealDao {

    @Query("SELECT * FROM favourite_meals")
    fun getAllFavourites(): Flow<List<FavouriteMeal>>    // FIXED: Removed the typo 'FRO<caret>M'
    @Query("SELECT * FROM favourite_meals WHERE idMeal = :id")
    fun getFavouriteById(id: String): Flow<FavouriteMeal?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meal: FavouriteMeal)

    @Query("DELETE FROM favourite_meals WHERE idMeal = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT EXISTS(SELECT * FROM favourite_meals WHERE idMeal = :id)")
    suspend fun isFavourite(id: String): Boolean
}