package com.baosystems.icrc.psm.viewmodels.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.baosystems.icrc.psm.service.MetadataManager
import com.baosystems.icrc.psm.service.UserManager
import com.baosystems.icrc.psm.service.scheduler.BaseSchedulerProvider
import io.reactivex.disposables.CompositeDisposable

class HomeViewModelFactory(
    private val disposable: CompositeDisposable,
    private val schedulerProvider: BaseSchedulerProvider,
    private val metadataManager: MetadataManager,
    private val userManager: UserManager
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return HomeViewModel(
            disposable,
            schedulerProvider,
            metadataManager,
            userManager
        ) as T
    }
}