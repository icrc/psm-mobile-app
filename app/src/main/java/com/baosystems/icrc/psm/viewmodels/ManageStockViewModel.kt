package com.baosystems.icrc.psm.viewmodels

import androidx.lifecycle.MutableLiveData
import com.baosystems.icrc.psm.data.TransactionType
import com.baosystems.icrc.psm.data.models.IdentifiableModel
import com.baosystems.icrc.psm.data.models.StockItem

class ManageStockViewModel(
    var transactionType: TransactionType,
    var facility: IdentifiableModel,
    var transactionDate: String,
    var distributedTo: IdentifiableModel
): PSMViewModel() {
    var search: String? = null

    val stockItems = MutableLiveData<StockItem>()

    fun getItems(search: String) {
//        stockItems.value =
    }
}