package com.mafazer.storyappsubmission.view.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mafazer.storyappsubmission.data.Result
import com.mafazer.storyappsubmission.data.UserRepository
import kotlinx.coroutines.launch

class SignupViewModel(private val repository: UserRepository) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _signupResult = MutableLiveData<String>()
    val signupResult: LiveData<String> = _signupResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun register(name: String, email: String, password: String) = viewModelScope.launch {
        _isLoading.value = true
        val result = repository.register(name, email, password)
        _isLoading.value = false
        when (result) {
            is Result.Success -> {
                _signupResult.value = result.data
            }

            is Result.Error -> {
                _errorMessage.value = result.error
            }

            else -> {
            }
        }
    }
}