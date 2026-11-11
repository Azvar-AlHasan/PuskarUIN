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
    version = 3,
    exportSchema = true,
    autoMigrations = [
        // v2 adds the events tables. Room generates the migration from the exported schemas,
        // so users upgrading from v1 keep their bookmarks.
        AutoMigration(from = 1, to = 2),
        // v3 adds company details, benefits, salary and posted date to jobs.
        AutoMigration(from = 2, to = 3),
    ],
)
abstract class PuskarDatabase : RoomDatabase() {
    abstract fun jobDao(): JobDao
    abstract fun eventDao(): EventDao
}
