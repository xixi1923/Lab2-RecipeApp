package com.recipeapp.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.recipeapp.data.db.FavouriteMealDao
import com.recipeapp.data.db.RecipeDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// DataStore delegation
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "recipe_prefs")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFavouriteDao(db: RecipeDatabase): FavouriteMealDao {
        return db.favouriteMealDao()
    }

    // --- DATASTORE PROVIDER ---

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RecipeDatabase {
        return Room.databaseBuilder(
            context,
            RecipeDatabase::class.java,
            "recipe_db"
        )
            .fallbackToDestructiveMigration() // Add this to clear old/broken database files
            .build()
    }
}