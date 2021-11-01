package com.baosystems.icrc.psm.service

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import io.reactivex.Single
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance

interface StockManager {
    /**
     * Get the list of stock items
     *
     * @param query The query string (optional)
     * @param ou The organisation unit under consideration (optional)
     * @param program The program (optional)
     * @param attribute The attribute to be used to order the results in ascending order (optional)
     *
     * @return LiveData containing a paged list of the matching stock items
     */
    fun search(query: String?, ou: String?, program: String?, attribute: String?):
            LiveData<PagedList<TrackedEntityInstance>>

    fun loadItems(uids: Collection<String>): Single<MutableList<TrackedEntityInstance>>

    fun lookup(uid: String): TrackedEntityInstance?
}