package com.baosystems.icrc.psm.ui.managestock

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import com.baosystems.icrc.psm.data.AppConfig
import com.baosystems.icrc.psm.data.ReviewStockData
import com.baosystems.icrc.psm.data.TransactionType
import com.baosystems.icrc.psm.data.models.SearchParametersModel
import com.baosystems.icrc.psm.data.models.StockEntry
import com.baosystems.icrc.psm.data.models.Transaction
import com.baosystems.icrc.psm.services.StockManager
import com.baosystems.icrc.psm.services.scheduler.BaseSchedulerProvider
import com.baosystems.icrc.psm.ui.base.BaseViewModel
import com.baosystems.icrc.psm.utils.Constants.INTENT_EXTRA_TRANSACTION
import com.baosystems.icrc.psm.utils.Constants.SEARCH_QUERY_DEBOUNCE
import com.jakewharton.rxrelay2.PublishRelay
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ManageStockViewModel @Inject constructor(
    savedState: SavedStateHandle,
    val disposable: CompositeDisposable,
    val config: AppConfig,
    val schedulerProvider: BaseSchedulerProvider,
    stockManager: StockManager,
): BaseViewModel() {
    // TODO: Handle cases where transaction is null. (remove transaction!!)
    val transaction = savedState.get<Transaction>(INTENT_EXTRA_TRANSACTION)!!

    private var search = MutableLiveData<SearchParametersModel>()
    private val searchRelay = PublishRelay.create<String>()
    private val itemsCache = linkedMapOf<StockEntry, Long>()
    private val stockItems = Transformations.switchMap(search) { q ->
        stockManager.search(q, transaction.facility.uid)
    }

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
        search.value = SearchParametersModel(null, null, transaction.facility.uid)
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
                        search.postValue(
                            SearchParametersModel(result, null, transaction.facility.uid)
                        )
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

    fun onScanCompleted(itemCode: String) {
        search.postValue(SearchParametersModel(null, itemCode, transaction.facility.uid))
    }

    fun setItemQuantity(item: StockEntry, qty: Long) {
        itemsCache[item] = qty
    }

    fun getItemQuantity(item: StockEntry) = itemsCache[item]

    private fun getPopulatedEntries(): MutableList<StockEntry> {
        itemsCache.entries.forEach {
            it.key.qty = it.value
        }

        return itemsCache.keys.toMutableList()
    }

    fun getData(): ReviewStockData = ReviewStockData(transaction, getPopulatedEntries())
}