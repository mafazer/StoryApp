package com.mafazer.storyappsubmission.view.detailstory

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.mafazer.storyappsubmission.data.Result
import com.mafazer.storyappsubmission.data.UserRepository
import com.mafazer.storyappsubmission.data.remote.responses.Story

class DetailStoryViewModel(private val repository: UserRepository) : ViewModel() {
    fun getDetailStory(id: String): LiveData<Result<Story>> = liveData {
        emit(Result.Loading)
        try {
            val story = repository.getDetailStory(id)
            emit(story)
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error"))
        }
    }
}