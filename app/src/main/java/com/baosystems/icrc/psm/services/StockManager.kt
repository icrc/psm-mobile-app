package com.baosystems.icrc.psm.services

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.baosystems.icrc.psm.data.models.AppConfig
import com.baosystems.icrc.psm.data.models.SearchParametersModel
import com.baosystems.icrc.psm.data.models.StockEntry

interface StockManager {
    /**
     * Get the list of stock items
     *
     * @param query The query object which comprises the search query, OU and other parameters
     * @param config The application configuration
     * @param ou The organisation unit under consideration (optional)
     *
     * @return LiveData containing a paged list of the matching stock items
     */

    fun search(query: SearchParametersModel, config: AppConfig, ou: String?):
            LiveData<PagedList<StockEntry>>
}