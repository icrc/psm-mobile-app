package com.baosystems.icrc.psm.services

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.baosystems.icrc.psm.data.models.AppConfig
import com.baosystems.icrc.psm.data.models.SearchParametersModel
import com.baosystems.icrc.psm.data.models.StockEntry
import com.baosystems.icrc.psm.utils.AttributeHelper
import com.baosystems.icrc.psm.utils.Constants
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitMode
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import java.util.*

class StockManagerImpl(
    private val d2: D2
): StockManager {

    override fun search(
        query: SearchParametersModel,
        config: AppConfig,
        ou: String?
    ): LiveData<PagedList<StockEntry>> {
        var teiRepository = d2.trackedEntityModule().trackedEntityInstanceQuery()

        if (!ou.isNullOrEmpty())
            teiRepository.byOrgUnits()
                .eq(ou)
                .byOrgUnitMode()
                .eq(OrganisationUnitMode.SELECTED)
                .also { teiRepository = it }

        teiRepository.byProgram()
            .eq(config.program)
            .also { teiRepository = it }

        if (!query.name.isNullOrEmpty()) {
            teiRepository
                .byQuery()
                .like(query.name).also { teiRepository = it }
        }

        if (!query.code.isNullOrEmpty()) {
            teiRepository
                .byQuery()
                .eq(query.code)
                .also { teiRepository = it }
        }

        teiRepository.orderByAttribute(config.itemName)
            .eq(RepositoryScope.OrderByDirection.ASC)
            .also { teiRepository = it }

        val dataSource: DataSource<TrackedEntityInstance, StockEntry> = teiRepository.dataSource
            .mapByPage(this::filterDeleted)
            .mapByPage { transform(it, config) }

        // TODO: Make the pageSize dynamic once you're able to determine
        return LivePagedListBuilder(object : DataSource.Factory<TrackedEntityInstance, StockEntry>() {
            override fun create(): DataSource<TrackedEntityInstance, StockEntry> {
                return dataSource
            }
        }, Constants.ITEM_PAGE_SIZE).build()

    }

    private fun transform(teis: List<TrackedEntityInstance>, config: AppConfig): List<StockEntry> {
        return teis.map { tei ->
            StockEntry(
                tei.uid(),
                AttributeHelper.teiAttributeValueByAttributeUid(tei, config.itemName) ?: "",
                getStockOnHand(tei, config.stockOnHand) ?: ""
            )
        }
    }

    private fun getStockOnHand(tei: TrackedEntityInstance, stockOnHandUid: String): String? {
        val events = d2.eventModule()
            .events()
            .byTrackedEntityInstanceUids(Collections.singletonList(tei.uid()))
            .byDataValue(stockOnHandUid).like("")
            .byDeleted().isFalse
            .withTrackedEntityDataValues()
            .blockingGet()

        events.forEach { event ->
            event.trackedEntityDataValues()?.forEach { dataValue ->
                dataValue.dataElement().let { dv ->
                    if (dv.equals(stockOnHandUid)) {
                        return dataValue.value()
                    }
                }
            }
        }

        return null
    }

    private fun filterDeleted(list: MutableList<TrackedEntityInstance>):
            List<TrackedEntityInstance> {

        val iterator = list.iterator()
        while (iterator.hasNext()) {
            val tei = iterator.next()
            if (tei.deleted() != null && tei.deleted()!!) iterator.remove()
        }

        return list
    }
}