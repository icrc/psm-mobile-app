package com.baosystems.icrc.psm.viewmodels.review

import com.baosystems.icrc.psm.data.models.ReviewStockData
import com.baosystems.icrc.psm.data.models.StockEntry
import com.baosystems.icrc.psm.service.StockManager
import com.baosystems.icrc.psm.service.scheduler.BaseSchedulerProvider
import com.baosystems.icrc.psm.viewmodels.PSMViewModel
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

class ReviewStockViewModel(
    private val disposable: CompositeDisposable,
    private val schedulerProvider: BaseSchedulerProvider,
    private val stockManager: StockManager,
    private val data: ReviewStockData,
): PSMViewModel() {
    val stockItems = data.entries
    val transaction = data.transaction

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
    }

    fun removeItem(item: StockEntry) {
        stockItems.remove(item)
        Timber.d("Stock list after deletion: %s", stockItems)
    }

    fun updateQuantity(item: StockEntry, value: Long) {
//        stockItems.re
    }

    fun getItemQuantity(item: StockEntry) = item.qty
}