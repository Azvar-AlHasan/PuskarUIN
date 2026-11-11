package com.azvar.puskar.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.azvar.puskar.data.local.EventDao
import com.azvar.puskar.data.local.JobDao
import com.azvar.puskar.data.local.PuskarDatabase
import com.azvar.puskar.data.reminder.EventReminderScheduler
import com.azvar.puskar.data.reminder.WorkManagerEventReminderScheduler
import com.azvar.puskar.data.remote.EventRemoteDataSource
import com.azvar.puskar.data.remote.IoDispatcher
import com.azvar.puskar.data.remote.JobRemoteDataSource
import com.azvar.puskar.data.remote.demo.DemoEventDataSource
import com.azvar.puskar.data.remote.demo.DemoJobDataSource
import com.azvar.puskar.data.repository.EventRepository
import com.azvar.puskar.data.repository.JobRepository
import com.azvar.puskar.data.repository.OfflineFirstEventRepository
import com.azvar.puskar.data.repository.OfflineFirstJobRepository
import com.azvar.puskar.data.settings.DataStoreSettingsRepository
import com.azvar.puskar.data.settings.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PuskarDatabase =
        Room.databaseBuilder(context, PuskarDatabase::class.java, "puskar.db").build()

    @Provides
    fun provideJobDao(db: PuskarDatabase): JobDao = db.jobDao()

    @Provides
    fun provideEventDao(db: PuskarDatabase): EventDao = db.eventDao()

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager = WorkManager.getInstance(context)

    /** Injected instead of calling now() directly, so time-based logic is testable. */
    @Provides
    fun provideClock(): Clock = Clock.systemDefaultZone()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindJobRepository(impl: OfflineFirstJobRepository): JobRepository

    @Binds
    @Singleton
    abstract fun bindEventRepository(impl: OfflineFirstEventRepository): EventRepository

    /** Data sources: bind a backend implementation here to replace the bundled demo dataset. */
    @Binds
    abstract fun bindJobRemoteDataSource(impl: DemoJobDataSource): JobRemoteDataSource

    @Binds
    abstract fun bindEventRemoteDataSource(impl: DemoEventDataSource): EventRemoteDataSource

    @Binds
    abstract fun bindSettingsRepository(impl: DataStoreSettingsRepository): SettingsRepository

    @Binds
    abstract fun bindEventReminderScheduler(impl: WorkManagerEventReminderScheduler): EventReminderScheduler
}
