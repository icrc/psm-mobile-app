package com.baosystems.icrc.psm.viewmodels.splash

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.baosystems.icrc.psm.service.PreferenceProvider
import com.baosystems.icrc.psm.service.scheduler.BaseSchedulerProvider
import io.reactivex.disposables.CompositeDisposable

class SplashViewModelFactory(
    private val application: Application,
    private val disposable: CompositeDisposable,
    private val schedulerProvider: BaseSchedulerProvider,
    private val preferenceProvider: PreferenceProvider
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SplashViewModel(
            application,
            disposable,
            schedulerProvider,
            preferenceProvider
        ) as T
    }
}