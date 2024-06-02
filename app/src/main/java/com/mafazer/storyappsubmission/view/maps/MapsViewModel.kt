package com.mafazer.storyappsubmission.view.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.mafazer.storyappsubmission.data.UserRepository
import com.mafazer.storyappsubmission.data.remote.responses.StoryResponse
import com.mafazer.storyappsubmission.data.Result

class MapsViewModel(private val repository: UserRepository) : ViewModel() {
    fun getStoriesWithLocation(): LiveData<Result<StoryResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = repository.getStoriesWithLocation()
            emit(response)
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error"))
        }
    }
}