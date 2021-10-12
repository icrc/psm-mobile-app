package com.baosystems.icrc.psm.viewmodels.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.baosystems.icrc.psm.data.TransactionType
import com.baosystems.icrc.psm.data.models.IdentifiableModel
import com.baosystems.icrc.psm.viewmodels.ManageStockViewModel

class ManageStockViewModelFactory(
    private val transactionType: TransactionType,
    private val facility: IdentifiableModel,
    private val transactionDate: String,
    private val distributedTo: IdentifiableModel?
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ManageStockViewModel(
            transactionType,
            facility,
            transactionDate,
            distributedTo
        ) as T
    }
}