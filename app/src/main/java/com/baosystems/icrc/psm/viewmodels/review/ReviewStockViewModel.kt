package com.baosystems.icrc.psm.viewmodels.review

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.baosystems.icrc.psm.data.models.ReviewStockData
import com.baosystems.icrc.psm.data.models.StockEntry
import com.baosystems.icrc.psm.service.StockManager
import com.baosystems.icrc.psm.service.scheduler.BaseSchedulerProvider
import com.baosystems.icrc.psm.utils.Constants
import com.baosystems.icrc.psm.viewmodels.PSMViewModel
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.util.concurrent.TimeUnit

class ReviewStockViewModel(
    private val disposable: CompositeDisposable,
    private val schedulerProvider: BaseSchedulerProvider,
    private val stockManager: StockManager,
    private val data: ReviewStockData,
): PSMViewModel() {
    val transaction = data.transaction

    private var search = MutableLiveData<String>()
    private val searchRelay = PublishRelay.create<String>()
    private val stockItems = Transformations.switchMap(search) { q ->
        MutableLiveData(data.entries.toList())
    }

    init {
        // TODO: Load the stock items in question using the uids passed
//        disposable.add(
//            stockManager.loadItems(data.entries)
//                .subscribeOn(schedulerProvider.io())
//                .observeOn(schedulerProvider.ui())
//                .subscribe(
//                    { teis ->
//                        val items = listOf<StockEntry>()
//                        // TODO: Add the actual value later
////                        teis.forEach { tei ->
////                            items.
////                        }
//                    },
//                    {
//                        // TODO: Report the error to the user
//                        Timber.w(it, "Unable to load stock items")
//                        it.printStackTrace()
//                    }
//                )
//        )
        configureSearchRelay()
    }

    // TODO: Find a way to reuse this function, as the same is being used by ManageStockMModel
    private fun configureSearchRelay() {
        disposable.add(
            searchRelay
                .debounce(Constants.SEARCH_QUERY_DEBOUNCE, TimeUnit.MILLISECONDS)
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

    fun getStockItems() = stockItems

    fun removeItem(item: StockEntry) {
//        stockItems.
//        stockItems..remove(item)
        Timber.d("Stock list after deletion: %s", stockItems)
    }

    fun updateQuantity(item: StockEntry, value: Long) {
//        stockItems.re
    }

    fun getItemQuantity(item: StockEntry) = item.qty
}