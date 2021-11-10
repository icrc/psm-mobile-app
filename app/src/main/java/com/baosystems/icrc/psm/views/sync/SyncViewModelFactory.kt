package com.baosystems.icrc.psm.views.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.baosystems.icrc.psm.service.PreferenceProvider
import com.baosystems.icrc.psm.service.SyncManager
import com.baosystems.icrc.psm.service.scheduler.BaseSchedulerProvider
import io.reactivex.disposables.CompositeDisposable

class SyncViewModelFactory(
    private val disposable: CompositeDisposable,
    private val schedulerProvider: BaseSchedulerProvider,
    private val preferenceProvider: PreferenceProvider,
    private val syncManager: SyncManager,
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SyncViewModel(
            disposable,
            schedulerProvider,
            preferenceProvider,
            syncManager
        ) as T
    }
}