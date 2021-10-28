package com.baosystems.icrc.psm.service

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.baosystems.icrc.psm.utils.Constants
import io.reactivex.Single
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitMode
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance

class StockManagerImpl(
    private val d2: D2
): StockManager {

    override fun search(
        query: String?,
        ou: String?,
        program: String?,
        attribute: String?
    ): LiveData<PagedList<TrackedEntityInstance>> {
        var teiRepository = d2.trackedEntityModule().trackedEntityInstanceQuery()

        if (!ou.isNullOrEmpty())
            teiRepository.byOrgUnits()
                .eq(ou)
                .byOrgUnitMode()
                .eq(OrganisationUnitMode.SELECTED)
                .also { teiRepository = it }

        if (!program.isNullOrEmpty())
            teiRepository.byProgram()
                .eq(program)
                .also { teiRepository = it }

        if (!query.isNullOrEmpty()) {
            teiRepository
                .byQuery()
                .like(query).also { teiRepository = it }
        }

        if (!attribute.isNullOrEmpty()) {
            teiRepository.orderByAttribute(attribute)
                .eq(RepositoryScope.OrderByDirection.ASC)
                .also { teiRepository = it }
        }

        // TODO: Make the pageSize dynamic once you're able to determine
        return teiRepository.getPaged(Constants.ITEM_PAGE_SIZE)
    }

    override fun loadItems(uids: Collection<String>): Single<MutableList<TrackedEntityInstance>> =
        d2.trackedEntityModule().trackedEntityInstances().byUid().`in`(uids).get()
}