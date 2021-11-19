package com.baosystems.icrc.psm.ui.managestock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.baosystems.icrc.psm.data.models.AppConfig
import com.baosystems.icrc.psm.data.models.Transaction
import com.baosystems.icrc.psm.services.StockManager
import com.baosystems.icrc.psm.services.scheduler.BaseSchedulerProvider
import io.reactivex.disposables.CompositeDisposable

class ManageStockViewModelFactory(
    private val disposable: CompositeDisposable,
    private val schedulerProvider: BaseSchedulerProvider,
    private val stockManager: StockManager,
    private val config: AppConfig,
    private val transaction: Transaction
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(ManageStockViewModel::class.java))
            return ManageStockViewModel(
                disposable,
                schedulerProvider,
                stockManager,
                config,
                transaction
            ) as T

        throw IllegalAccessException("Unknown ViewModel class")
    }
}