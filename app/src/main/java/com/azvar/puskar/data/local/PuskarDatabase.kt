package com.azvar.puskar.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        JobEntity::class,
        BookmarkEntity::class,
        EventEntity::class,
        EventReminderEntity::class,
    ],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        // v2 adds the events tables. Room generates the migration from the exported schemas,
        // so users upgrading from v1 keep their bookmarks.
        AutoMigration(from = 1, to = 2),
    ],
)
abstract class PuskarDatabase : RoomDatabase() {
    abstract fun jobDao(): JobDao
    abstract fun eventDao(): EventDao
}
