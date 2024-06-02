package com.mafazer.storyappsubmission.di

import android.content.Context
import androidx.room.Room
import com.mafazer.storyappsubmission.data.UserRepository
import com.mafazer.storyappsubmission.data.local.StoryDatabase
import com.mafazer.storyappsubmission.data.pref.UserPreference
import com.mafazer.storyappsubmission.data.pref.dataStore
import com.mafazer.storyappsubmission.data.remote.ApiConfig

object Injection {

    private fun provideDatabase(context: Context): StoryDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            StoryDatabase::class.java,
            "story_database"
        ).build()
    }

    fun provideRepository(context: Context): UserRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService(context)
        val database = provideDatabase(context)
        return UserRepository.getInstance(pref, apiService, database)
    }
}