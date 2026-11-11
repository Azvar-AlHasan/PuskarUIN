package com.azvar.puskar.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [JobEntity::class, BookmarkEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class PuskarDatabase : RoomDatabase() {
    abstract fun jobDao(): JobDao
}
