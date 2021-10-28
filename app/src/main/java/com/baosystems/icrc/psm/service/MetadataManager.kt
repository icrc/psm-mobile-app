package com.baosystems.icrc.psm.service

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import io.reactivex.Single
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance

interface MetadataManager {
    fun stockManagementProgram(): Single<Program?>
    fun facilities(): Single<MutableList<OrganisationUnit>>
    fun destinations(): Single<List<Option>>

    /**
     * Get the list of stock items
     *
     * @param search The query string (optional)
     * @param ou The organisation unit under consideration (optional)
     * @param program The program (optional)
     * @param attribute The attribute to be used to order the results in ascending order (optional)
     *
     * @return LiveData containing a paged list of the matching stock items
     */
    // TODO: stock items is not metadata, so it should probably to move to a proper location
    fun queryStock(search: String?, ou: String?, program: String?, attribute: String?):
        LiveData<PagedList<TrackedEntityInstance>>
}