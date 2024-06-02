package com.mafazer.storyappsubmission.view.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mafazer.storyappsubmission.data.Result
import com.mafazer.storyappsubmission.data.UserRepository
import com.mafazer.storyappsubmission.data.pref.UserModel
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: UserRepository) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _loginResult = MutableLiveData<UserModel>()
    val loginResult: LiveData<UserModel> = _loginResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun login(email: String, password: String) = viewModelScope.launch {
        _isLoading.value = true
        val result = repository.login(email, password)
        _isLoading.value = false
        when (result) {
            is Result.Success -> {
                val user = UserModel(email, result.data.loginResult.token, true)
                repository.saveSession(user)
                _loginResult.value = user
            }

            is Result.Error -> {
                _errorMessage.value = result.error
            }

            else -> {
            }
        }
    }
}