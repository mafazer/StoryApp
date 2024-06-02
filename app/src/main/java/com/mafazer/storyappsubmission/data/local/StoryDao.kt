package com.mafazer.storyappsubmission.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stories: List<DbListStoryItem>)

    @Query("SELECT * FROM story")
    fun getAllStories(): PagingSource<Int, DbListStoryItem>

    @Query("DELETE FROM story")
    suspend fun deleteAll()
}