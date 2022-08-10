package com.baosystems.icrc.psm.services

import com.baosystems.icrc.psm.data.AppConfig
import com.baosystems.icrc.psm.data.models.SearchParametersModel
import com.baosystems.icrc.psm.data.models.SearchResult
import com.baosystems.icrc.psm.data.models.StockEntry
import com.baosystems.icrc.psm.data.models.Transaction
import io.reactivex.Single

interface StockManager {
    /**
     * Get the list of stock items
     *
     * @param query The query object which comprises the search query, OU and other parameters
     * @param ou The organisation unit under consideration (optional)
     *
     * @return The search result containing the livedata of paged list of the matching stock items
     * and total count of matched items
     */

    fun search(query: SearchParametersModel, ou: String?, config: AppConfig): SearchResult

    fun saveTransaction(items: List<StockEntry>, transaction: Transaction, appConfig: AppConfig):
        Single<Unit>
}