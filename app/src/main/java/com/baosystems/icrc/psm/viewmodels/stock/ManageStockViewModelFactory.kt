package com.baosystems.icrc.psm.viewmodels.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.baosystems.icrc.psm.data.TransactionType
import com.baosystems.icrc.psm.data.models.IdentifiableModel
import com.baosystems.icrc.psm.service.StockManager
import com.baosystems.icrc.psm.service.scheduler.BaseSchedulerProvider
import io.reactivex.disposables.CompositeDisposable

class ManageStockViewModelFactory(
    private val disposable: CompositeDisposable,
    private val schedulerProvider: BaseSchedulerProvider,
    private val stockManager: StockManager,
    private val transactionType: TransactionType,
    private val facility: IdentifiableModel,
    private val transactionDate: String,
    private val distributedTo: IdentifiableModel?
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(ManageStockViewModel::class.java))
            return ManageStockViewModel(
                disposable,
                schedulerProvider,
                stockManager,
                transactionType,
                facility,
                transactionDate,
                distributedTo
            ) as T

        throw IllegalAccessException("Unknown ViewModel class")
    }
}