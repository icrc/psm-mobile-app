package com.baosystems.icrc.psm.viewmodels.stock

import androidx.lifecycle.MutableLiveData
import com.baosystems.icrc.psm.data.TransactionType
import com.baosystems.icrc.psm.data.models.IdentifiableModel
import com.baosystems.icrc.psm.service.MetadataManager
import com.baosystems.icrc.psm.viewmodels.PSMViewModel

class ManageStockViewModel(
    metadataManager: MetadataManager,
    var transactionType: TransactionType,
    var facility: IdentifiableModel,
    var transactionDate: String,
    var distributedTo: IdentifiableModel?
): PSMViewModel() {
    var search = MutableLiveData<String>()

    val stockItems = metadataManager.queryStock("")

    init {
        if (transactionType != TransactionType.DISTRIBUTION && distributedTo != null)
            throw UnsupportedOperationException(
                "Cannot set 'distributedTo' for non-distribution transactions")

        if (transactionType == TransactionType.DISTRIBUTION && distributedTo == null)
            throw UnsupportedOperationException("'distributedTo' is mandatory for model creation")
    }

//    fun getItems(search: String) {
////        stockItems.value =
//    }
//
//    fun setSearchTerm(q: String) {
//        search.value = q
//    }
//
    fun onQueryChanged(searchQuery: String) {

    }
}