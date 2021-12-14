package com.baosystems.icrc.psm.services

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.baosystems.icrc.psm.data.models.SearchParametersModel
import com.baosystems.icrc.psm.data.models.StockEntry
import com.baosystems.icrc.psm.data.models.StockItem
import com.baosystems.icrc.psm.data.models.Transaction
import io.reactivex.Single

interface StockManager {
    /**
     * Get the list of stock items
     *
     * @param query The query object which comprises the search query, OU and other parameters
     * @param ou The organisation unit under consideration (optional)
     *
     * @return LiveData containing a paged list of the matching stock items
     */

    fun search(query: SearchParametersModel, ou: String?):
            LiveData<PagedList<StockItem>>

    fun saveTransaction(items: List<StockEntry>, transaction: Transaction):
            Single<Unit>
}