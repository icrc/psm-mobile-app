package com.baosystems.icrc.psm.viewmodels.stock

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.baosystems.icrc.psm.data.TransactionType
import com.baosystems.icrc.psm.data.models.AppConfig
import com.baosystems.icrc.psm.data.models.ReviewStockData
import com.baosystems.icrc.psm.data.models.StockEntry
import com.baosystems.icrc.psm.data.models.Transaction
import com.baosystems.icrc.psm.service.StockManager
import com.baosystems.icrc.psm.service.scheduler.BaseSchedulerProvider
import com.baosystems.icrc.psm.utils.AttributeHelper
import com.baosystems.icrc.psm.utils.Constants.SEARCH_QUERY_DEBOUNCE
import com.baosystems.icrc.psm.viewmodels.PSMViewModel
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.disposables.CompositeDisposable
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import timber.log.Timber
import java.util.concurrent.TimeUnit

class ManageStockViewModel(
    private val disposable: CompositeDisposable,
    private val schedulerProvider: BaseSchedulerProvider,
    stockManager: StockManager,
    private val config: AppConfig,
    val transaction: Transaction
): PSMViewModel() {
    private var search = MutableLiveData<String>()
    private val searchRelay = PublishRelay.create<String>()
    private val stockItems = Transformations.switchMap(search) { q ->
        stockManager.search(q, transaction.facility.uid, config.program, config.itemValue)
    }
    private val entries = linkedMapOf<TrackedEntityInstance, Long>()

    init {
        if (transaction.transactionType != TransactionType.DISTRIBUTION &&
            transaction.distributedTo != null)
            throw UnsupportedOperationException(
                "Cannot set 'distributedTo' for non-distribution transactions")

        if (transaction.transactionType == TransactionType.DISTRIBUTION &&
            transaction.distributedTo == null)
            throw UnsupportedOperationException("'distributedTo' is mandatory for model creation")

        configureSearchRelay()
        loadStockItems()
    }

    private fun loadStockItems() {
        search.value = ""
    }

    fun getStockItems() = stockItems

    private fun configureSearchRelay() {
        disposable.add(
            searchRelay
                .debounce(SEARCH_QUERY_DEBOUNCE, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { result ->
                        Timber.d("Distinct: $result")
                        search.postValue(result)
                    },
                    {
                        // TODO: Report the error to the user
                        it.printStackTrace()
                        Timber.w(it, "Unable to fetch search results")
                    })
        )
    }

    fun onSearchQueryChanged(query: String) {
        searchRelay.accept(query)
    }

    fun setItemQuantity(item: TrackedEntityInstance, qty: Long) {
        entries[item] = qty
    }

    fun getItemQuantity(item: TrackedEntityInstance) = entries[item]

    private fun getPopulatedEntries(): MutableList<StockEntry> = entries.map {
        val tei = it.key
        StockEntry(
            tei.uid(),
            AttributeHelper.teiAttributeValueByAttributeUid(tei, "MBczRWvfM46") ?: "",
            it.value
        )
    }.toMutableList()

    fun getData(): ReviewStockData = ReviewStockData(transaction, getPopulatedEntries())
}