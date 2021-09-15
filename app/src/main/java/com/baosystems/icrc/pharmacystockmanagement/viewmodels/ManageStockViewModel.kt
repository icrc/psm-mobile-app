package com.baosystems.icrc.pharmacystockmanagement.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.baosystems.icrc.pharmacystockmanagement.data.models.Destination
import com.baosystems.icrc.pharmacystockmanagement.data.models.Facility
import com.baosystems.icrc.pharmacystockmanagement.data.models.StockItem
import java.time.LocalDateTime

class ManageStockViewModel(
//    val facility: Facility,
//    val date: LocalDateTime,
//    var distributedTo: Destination
): PSMViewModel() {
    var search: String? = null
    var facility: Facility? = null
    var date: LocalDateTime? = null
    var distributedTo: Destination? = null

    val stockItems = MutableLiveData<StockItem>()

    fun getItems(search: String) {
//        stockItems.value =
    }
}