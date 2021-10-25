package com.baosystems.icrc.psm.viewmodels.stock

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.baosystems.icrc.psm.data.TransactionType
import com.baosystems.icrc.psm.data.models.IdentifiableModel
import com.baosystems.icrc.psm.service.MetadataManager
import com.baosystems.icrc.psm.viewmodels.PSMViewModel
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import timber.log.Timber

class ManageStockViewModel(
    val metadataManager: MetadataManager,
    var transactionType: TransactionType,
    var facility: IdentifiableModel,
    var transactionDate: String,
    var distributedTo: IdentifiableModel?
): PSMViewModel() {
    var search = MutableLiveData<String>()

    init {
        if (transactionType != TransactionType.DISTRIBUTION && distributedTo != null)
            throw UnsupportedOperationException(
                "Cannot set 'distributedTo' for non-distribution transactions")

        if (transactionType == TransactionType.DISTRIBUTION && distributedTo == null)
            throw UnsupportedOperationException("'distributedTo' is mandatory for model creation")
    }

//    fun getStockItems(): LiveData<PagedList<TrackedEntityInstance>> =
//        metadataManager.queryStock(search.value)

    fun getStockItems(): LiveData<PagedList<TrackedEntityInstance>> {
        Timber.d("getStockItems(): search = %s", search.value)

        return metadataManager.queryStock(search.value)
    }
}