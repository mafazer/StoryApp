package com.mafazer.storyappsubmission.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DbListStoryItem::class, RemoteKeys::class],
    version = 1,
    exportSchema = false
)
abstract class StoryDatabase : RoomDatabase() {
    abstract fun storyDao(): StoryDao
    abstract fun remoteKeysDao(): RemoteKeysDao
}