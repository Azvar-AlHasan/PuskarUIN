package com.azvar.puskar.di

import android.content.Context
import androidx.room.Room
import com.azvar.puskar.data.local.JobDao
import com.azvar.puskar.data.local.PuskarDatabase
import com.azvar.puskar.data.remote.AssetJobRemoteDataSource
import com.azvar.puskar.data.remote.IoDispatcher
import com.azvar.puskar.data.remote.JobRemoteDataSource
import com.azvar.puskar.data.repository.JobRepository
import com.azvar.puskar.data.repository.OfflineFirstJobRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PuskarDatabase =
        Room.databaseBuilder(context, PuskarDatabase::class.java, "puskar.db").build()

    @Provides
    fun provideJobDao(db: PuskarDatabase): JobDao = db.jobDao()

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindJobRepository(impl: OfflineFirstJobRepository): JobRepository

    /** Swap this binding for a Firestore implementation in the next milestone. */
    @Binds
    abstract fun bindJobRemoteDataSource(impl: AssetJobRemoteDataSource): JobRemoteDataSource
}
