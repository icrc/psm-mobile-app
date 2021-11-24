package com.baosystems.icrc.psm.di

import android.content.Context
import com.baosystems.icrc.psm.data.AppConfig
import com.baosystems.icrc.psm.services.PreferenceProvider
import com.baosystems.icrc.psm.services.SecurePreferenceProviderImpl
import com.baosystems.icrc.psm.utils.ConfigUtils
import com.baosystems.icrc.psm.utils.Sdk
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.hisp.dhis.android.core.D2
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CoreModule {
    @Provides
    @Singleton
    fun providesAppConfig(@ApplicationContext appContext: Context): AppConfig {
        return ConfigUtils.getAppConfig(appContext.resources)
    }

    @Provides
    @Singleton
    fun providesD2(@ApplicationContext appContext: Context): D2 {
        return Sdk.d2(appContext)
    }

    @Provides
    @Singleton
    fun providesPreferenceProvider(@ApplicationContext appContext: Context): PreferenceProvider {
        return SecurePreferenceProviderImpl(appContext)
    }
}