package com.baosystems.icrc.psm.di

import android.content.Context
import com.baosystems.icrc.psm.data.models.AppConfig
import com.baosystems.icrc.psm.services.PreferenceProvider
import com.baosystems.icrc.psm.services.SecurePreferenceProviderImpl
import com.baosystems.icrc.psm.utils.ConfigUtils
import com.baosystems.icrc.psm.utils.Sdk
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.disposables.CompositeDisposable
import org.hisp.dhis.android.core.D2

@Module
@InstallIn(ViewModelComponent::class)
object SystemModule {
    @Provides
    fun providesDisposable(): CompositeDisposable {
        return CompositeDisposable()
    }

    @Provides
    fun providesD2(@ApplicationContext appContext: Context): D2 {
        return Sdk.d2(appContext);
    }

    @Provides
    fun providesAppConfig(@ApplicationContext appContext: Context): AppConfig {
        return ConfigUtils.getAppConfig(appContext.resources)
    }

    @Provides
    fun providesPreferenceProvider(@ApplicationContext appContext: Context): PreferenceProvider {
        return SecurePreferenceProviderImpl(appContext)
    }
}