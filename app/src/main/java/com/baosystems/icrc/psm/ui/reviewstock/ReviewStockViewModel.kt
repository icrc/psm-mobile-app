package com.baosystems.icrc.psm.ui.reviewstock

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.baosystems.icrc.psm.commons.Constants
import com.baosystems.icrc.psm.commons.Constants.INTENT_EXTRA_STOCK_ENTRIES
import com.baosystems.icrc.psm.data.AppConfig
import com.baosystems.icrc.psm.data.ReviewStockData
import com.baosystems.icrc.psm.data.models.StockEntry
import com.baosystems.icrc.psm.data.models.Transaction
import com.baosystems.icrc.psm.data.persistence.UserActivity
import com.baosystems.icrc.psm.data.persistence.UserActivityRepository
import com.baosystems.icrc.psm.services.PreferenceProvider
import com.baosystems.icrc.psm.services.StockManager
import com.baosystems.icrc.psm.services.scheduler.BaseSchedulerProvider
import com.baosystems.icrc.psm.ui.base.BaseViewModel
import com.jakewharton.rxrelay2.PublishRelay
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ReviewStockViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val disposable: CompositeDisposable,
    val config: AppConfig,
    private val schedulerProvider: BaseSchedulerProvider,
    preferenceProvider: PreferenceProvider,
    private val stockManager: StockManager,
    private val userActivityRepository: UserActivityRepository
): BaseViewModel(preferenceProvider) {
    // TODO: Figure out a better way than using !!
    val data = savedState.get<ReviewStockData>(INTENT_EXTRA_STOCK_ENTRIES)!!
    val transaction = data.transaction

    private var search = MutableLiveData<String>()
    private val searchRelay = PublishRelay.create<String>()

    private val populatedItems = data.entries
    private val _reviewedItems: MutableLiveData<List<StockEntry>> = MutableLiveData(populatedItems)
    val reviewedItems: LiveData<List<StockEntry>>
        get() = _reviewedItems

    private val _commitStatus = MutableLiveData<Boolean>(false)
    val commitStatus: LiveData<Boolean>
        get() = _commitStatus

    init {
        configureSearchRelay()
        loadPopulatedItems()
    }

    private fun loadPopulatedItems() {
        search.value = ""
    }

    // TODO: Find a way to reuse this function, as the same is being used by ManageStockModel
    private fun configureSearchRelay() {
        disposable.add(
            searchRelay
                .debounce(Constants.SEARCH_QUERY_DEBOUNCE, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    {  query -> _reviewedItems.postValue(performSearch(query)) },
                    {
                        // TODO: Report the error to the user
                        it.printStackTrace()
                    })
        )
    }

    fun removeItem(item: StockEntry) = populatedItems.remove(item)

    fun updateQuantity(item: StockEntry, value: Long) {
        item.qty = value
    }

    fun getItemQuantity(item: StockEntry) = item.qty

    fun onSearchQueryChanged(query: String) = searchRelay.accept(query)

    private fun performSearch(q: String?): List<StockEntry> {
        return if (q == null || q.isEmpty())
            populatedItems.toList()
        else
            populatedItems.filter { it.name.contains(q, true) }
    }

    fun commitTransaction() {
        if (reviewedItems.value == null || reviewedItems.value?.isEmpty() == true) {
            // TODO: Report error/warning to user that there are currently no reviewed items
            Timber.d("No items to commit")
            return
        }

        // TODO: Notify observer on completion
        disposable.add(
            stockManager.saveTransaction(reviewedItems.value!!, transaction)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe({
                    _commitStatus.postValue(true)
                    logAudit(transaction)
                }, {
                    // TODO: Report error to observer
                    it.printStackTrace()
                })
        )
    }

    private fun logAudit(transaction: Transaction) {
        disposable.add(
            userActivityRepository.addActivity(
                UserActivity(
                    transaction.transactionType,
                    LocalDateTime.now(),
                    transaction.distributedTo?.displayName
                )
            ).subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe()
        )
    }
}