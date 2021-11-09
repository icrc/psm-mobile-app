package com.baosystems.icrc.psm.viewmodels.splash

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.baosystems.icrc.psm.service.PreferenceProvider
import com.baosystems.icrc.psm.service.scheduler.BaseSchedulerProvider
import io.reactivex.disposables.CompositeDisposable
import java.util.*

class SplashViewModelFactory(
    private val application: Application,
    private val disposable: CompositeDisposable,
    private val schedulerProvider: BaseSchedulerProvider,
    private val preferenceProvider: PreferenceProvider,
    private val configProps: Properties
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(SplashViewModel::class.java))
            return SplashViewModel(
                application,
                disposable,
                schedulerProvider,
                preferenceProvider,
                configProps
            ) as T

        throw IllegalAccessException("Unknown ViewModel class")
    }
}