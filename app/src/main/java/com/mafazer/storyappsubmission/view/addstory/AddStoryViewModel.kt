package com.mafazer.storyappsubmission.view.addstory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mafazer.storyappsubmission.data.Result
import com.mafazer.storyappsubmission.data.UserRepository
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AddStoryViewModel(private val repository: UserRepository) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _uploadResult = MutableLiveData<Result<String>>()
    val uploadResult: LiveData<Result<String>> = _uploadResult

    fun uploadStory(
        imageMultipart: MultipartBody.Part,
        description: RequestBody,
        lat: RequestBody?,
        lon: RequestBody?
    ) {
        _uploadResult.value = Result.Loading
        viewModelScope.launch {
            try {
                val response = repository.uploadStory(imageMultipart, description, lat, lon)
                _uploadResult.value = response
            } catch (e: Exception) {
                _uploadResult.value = Result.Error(e.message ?: "Unknown error")
            }
        }
    }
}