package com.baosystems.icrc.psm.data.models

import androidx.lifecycle.LiveData
import androidx.paging.PagedList

data class SearchResult(
    val items: LiveData<PagedList<StockItem>>,
    val totalCount: Int
)
