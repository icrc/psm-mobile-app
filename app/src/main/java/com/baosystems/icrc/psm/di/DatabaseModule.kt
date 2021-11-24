package com.baosystems.icrc.psm.di

import android.content.Context
import com.baosystems.icrc.psm.data.persistence.AppDatabase
import com.baosystems.icrc.psm.data.persistence.UserActivityDao
import com.baosystems.icrc.psm.data.persistence.UserActivityRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {
    @Provides
    @Singleton
    fun providesAppDatabase(@ApplicationContext appContext: Context) = AppDatabase.getInstance(appContext)

    @Provides
    fun providesUserActivityDao(appDatabase: AppDatabase): UserActivityDao {
        return appDatabase.userActivityDao()
    }

    @Provides
    fun providesUserActivityRepository(userActivityDao: UserActivityDao): UserActivityRepository {
        return UserActivityRepository(userActivityDao)
    }
}