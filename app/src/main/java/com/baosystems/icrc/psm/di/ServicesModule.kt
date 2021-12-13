package com.baosystems.icrc.psm.di

import com.baosystems.icrc.psm.services.*
import com.baosystems.icrc.psm.services.rules.RuleValidationHelper
import com.baosystems.icrc.psm.services.rules.RuleValidationHelperImpl
import com.baosystems.icrc.psm.services.scheduler.BaseSchedulerProvider
import com.baosystems.icrc.psm.services.scheduler.SchedulerProviderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class ServicesModule {
    @Binds
    abstract fun providesSchedulerProvider(impl: SchedulerProviderImpl): BaseSchedulerProvider

    @Binds
    abstract fun provideMetadataManager(impl: MetadataManagerImpl): MetadataManager

    @Binds
    abstract fun provideUserManager(impl: UserManagerImpl): UserManager

    @Binds
    abstract fun provideStockManager(impl: StockManagerImpl): StockManager

    @Binds
    abstract fun provideProgramRuleValidationHelper(
        impl: RuleValidationHelperImpl
    ): RuleValidationHelper
}