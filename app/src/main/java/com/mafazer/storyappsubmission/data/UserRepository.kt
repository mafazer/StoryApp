package com.mafazer.storyappsubmission.data

import com.mafazer.storyappsubmission.data.local.StoryRemoteMediator
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.mafazer.storyappsubmission.data.local.DbListStoryItem
import com.mafazer.storyappsubmission.data.local.StoryDatabase
import com.mafazer.storyappsubmission.data.pref.UserModel
import com.mafazer.storyappsubmission.data.pref.UserPreference
import com.mafazer.storyappsubmission.data.remote.ApiService
import com.mafazer.storyappsubmission.data.remote.responses.ErrorResponse
import com.mafazer.storyappsubmission.data.remote.responses.LoginResponse
import com.mafazer.storyappsubmission.data.remote.responses.Story
import com.mafazer.storyappsubmission.data.remote.responses.StoryResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException

class UserRepository private constructor(
    private val userPreference: UserPreference,
    private val apiService: ApiService,
    private val database: StoryDatabase
) {

    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun register(name: String, email: String, password: String): Result<String> {
        return try {
            val response = apiService.register(name, email, password)
            if (response.error) {
                Result.Error(response.message)
            } else {
                Result.Success(response.message)
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorMessage = try {
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                errorResponse.message ?: "Unknown error"
            } catch (e: JsonSyntaxException) {
                "Unknown error"
            }
            Result.Error(errorMessage)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(email, password)
            if (response.error) {
                Result.Error(response.message)
            } else {
                Result.Success(response)
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorMessage = try {
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                errorResponse.message ?: "Unknown error"
            } catch (e: JsonSyntaxException) {
                "Unknown error"
            }
            Result.Error(errorMessage)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun getDetailStory(id: String): Result<Story> {
        return try {
            val response = apiService.getDetailStory(id)
            if (response.error) {
                Result.Error(response.message)
            } else {
                Result.Success(response.story)
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorMessage = try {
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                errorResponse.message ?: "Unknown error"
            } catch (e: JsonSyntaxException) {
                "Unknown error"
            }
            Result.Error(errorMessage)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun uploadStory(
        imageMultipart: MultipartBody.Part,
        description: RequestBody,
        lat: RequestBody?,
        lon: RequestBody?
    ): Result<String> {
        return try {
            val response = apiService.uploadStory(imageMultipart, description, lat, lon)
            if (response.error) {
                Result.Error(response.message)
            } else {
                Result.Success(response.message)
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorMessage = try {
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                errorResponse.message ?: "Unknown error"
            } catch (e: JsonSyntaxException) {
                "Unknown error"
            }
            Result.Error(errorMessage)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun getStoriesWithLocation(): Result<StoryResponse> {
        return try {
            val response = apiService.getStoriesWithLocation()
            if (response.error) {
                Result.Error(response.message)
            } else {
                Result.Success(response)
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getStoriesPaged(): Flow<PagingData<DbListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            remoteMediator = StoryRemoteMediator(database, apiService),
            pagingSourceFactory = { database.storyDao().getAllStories() }
        ).flow
            .catch { _ ->
                emit(PagingData.empty())
            }
    }

    suspend fun logout() {
        userPreference.logout()
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(
            userPreference: UserPreference,
            apiService: ApiService,
            database: StoryDatabase
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(userPreference, apiService, database)
            }.also { instance = it }
    }
}