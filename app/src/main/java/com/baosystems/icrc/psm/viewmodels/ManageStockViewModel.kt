package com.baosystems.icrc.psm.viewmodels

import androidx.lifecycle.MutableLiveData
import com.baosystems.icrc.psm.data.models.StockItem
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import java.time.LocalDateTime

class ManageStockViewModel(
//    val date: LocalDateTime,
//    var distributedTo: Destination
): PSMViewModel() {
    var search: String? = null
    var facility: OrganisationUnit? = null
    var date: LocalDateTime? = null
    var distributedTo: Option? = null

    val stockItems = MutableLiveData<StockItem>()

    fun getItems(search: String) {
//        stockItems.value =
    }
}