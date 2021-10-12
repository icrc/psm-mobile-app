package com.baosystems.icrc.psm.viewmodels.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.baosystems.icrc.psm.service.PreferenceProvider
import com.baosystems.icrc.psm.service.SyncManager
import com.baosystems.icrc.psm.service.scheduler.BaseSchedulerProvider
import com.baosystems.icrc.psm.viewmodels.SyncViewModel

class SyncViewModelFactory(
    private val schedulerProvider: BaseSchedulerProvider,
    private val preferenceProvider: PreferenceProvider,
    private val syncManager: SyncManager,
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SyncViewModel(
            schedulerProvider,
            preferenceProvider,
            syncManager
        ) as T
    }
}