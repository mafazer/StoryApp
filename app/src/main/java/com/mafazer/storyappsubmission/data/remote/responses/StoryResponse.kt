package com.mafazer.storyappsubmission.data.remote.responses

import com.google.gson.annotations.SerializedName

data class StoryResponse(
    @field:SerializedName("error")
    val error: Boolean,
    @field:SerializedName("message")
    val message: String,
    @field:SerializedName("listStory")
    val listStory: List<ListStoryItem> = emptyList()
)

data class ListStoryItem(
    @field:SerializedName("id")
    val id: String,
    @field:SerializedName("name")
    val name: String,
    @field:SerializedName("description")
    val description: String,
    @field:SerializedName("photoUrl")
    val photoUrl: String,
    @field:SerializedName("createdAt")
    val createdAt: String,
    @field:SerializedName("lon")
    val lon: Double? = null,
    @field:SerializedName("lat")
    val lat: Double? = null
)

data class GeneralResponse(
    @field:SerializedName("error")
    val error: Boolean,
    @field:SerializedName("message")
    val message: String
)

