package com.baosystems.icrc.psm.di

import android.content.Context
import androidx.work.WorkManager
import com.baosystems.icrc.psm.data.AppConfig
import com.baosystems.icrc.psm.services.*
import com.baosystems.icrc.psm.services.preferences.PreferenceProvider
import com.baosystems.icrc.psm.services.preferences.PreferenceProviderImpl
import com.baosystems.icrc.psm.services.rules.ExpressionEvaluatorImpl
import com.baosystems.icrc.psm.utils.ConfigUtils
import com.baosystems.icrc.psm.utils.Sdk
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.apache.commons.jexl2.JexlEngine
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.rules.RuleExpressionEvaluator
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
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
//        return SecurePreferenceProviderImpl(appContext)
        return PreferenceProviderImpl(appContext)
    }

    @Provides
    @Singleton
    fun providesWorkManager(@ApplicationContext appContext: Context): WorkManager {
        return WorkManager.getInstance(appContext)
    }

    @Provides
    @Singleton
    fun providesWorkManagerController(workManager: WorkManager): WorkManagerController {
        return WorkManagerControllerImpl(workManager)
    }

    @Provides
    @Singleton
    fun providesSyncManager(
        d2: D2,
        preferenceProvider: PreferenceProvider,
        workManagerController: WorkManagerController
    ): SyncManager {
        return SyncManagerImpl(d2, preferenceProvider, workManagerController)
    }

    @Provides
    @Singleton
    fun providesJexlEngine(): JexlEngine {
        return JexlEngine()
    }

    @Provides
    @Singleton
    fun providesRuleExpressionEvaluator(jexlEngine: JexlEngine): RuleExpressionEvaluator {
        return ExpressionEvaluatorImpl(jexlEngine)
    }

    @Provides
    @Singleton
    fun providesSpeechRecognitionManager(@ApplicationContext appContext: Context):
            SpeechRecognitionManager {
        return SpeechRecognitionManagerImpl(appContext)
    }
}