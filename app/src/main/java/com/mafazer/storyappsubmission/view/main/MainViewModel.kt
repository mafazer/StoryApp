package com.mafazer.storyappsubmission.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.mafazer.storyappsubmission.data.UserRepository
import com.mafazer.storyappsubmission.data.pref.UserModel
import kotlinx.coroutines.launch
import com.mafazer.storyappsubmission.data.local.DbListStoryItem

class MainViewModel(private val repository: UserRepository) : ViewModel() {
    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    val stories: LiveData<PagingData<DbListStoryItem>> =
        repository.getStoriesPaged().cachedIn(viewModelScope).asLiveData()
}