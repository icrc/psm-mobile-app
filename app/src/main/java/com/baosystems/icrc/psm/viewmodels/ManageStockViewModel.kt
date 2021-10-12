package com.baosystems.icrc.psm.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.baosystems.icrc.psm.data.TransactionType
import com.baosystems.icrc.psm.data.models.IdentifiableModel
import com.baosystems.icrc.psm.data.models.StockItem
import org.hisp.dhis.android.core.attribute.AttributeValue
import java.lang.UnsupportedOperationException

class ManageStockViewModel(
    var transactionType: TransactionType,
    var facility: IdentifiableModel,
    var transactionDate: String,
    var distributedTo: IdentifiableModel?
): PSMViewModel() {
    var search = MutableLiveData<String?>()

    val stockItems = MutableLiveData<PagedList<AttributeValue>>()

    init {
        if (transactionType != TransactionType.DISTRIBUTION && distributedTo != null)
            throw UnsupportedOperationException(
                "Cannot set 'distributedTo' for non-distribution transactions")

        if (transactionType == TransactionType.DISTRIBUTION && distributedTo == null)
            throw UnsupportedOperationException("'distributedTo' is mandatory for model creation")
    }

    fun getItems(search: String) {
//        stockItems.value =
    }

    fun setSearchTerm(q: String?) {
        search.value = q
    }

    fun queryStock(q: String) {

    }
}