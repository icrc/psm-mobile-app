package com.baosystems.icrc.psm.ui.managestock

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import com.baosystems.icrc.psm.commons.Constants.INTENT_EXTRA_TRANSACTION
import com.baosystems.icrc.psm.commons.Constants.SEARCH_QUERY_DEBOUNCE
import com.baosystems.icrc.psm.data.AppConfig
import com.baosystems.icrc.psm.data.ReviewStockData
import com.baosystems.icrc.psm.data.TransactionType
import com.baosystems.icrc.psm.data.models.SearchParametersModel
import com.baosystems.icrc.psm.data.models.StockEntry
import com.baosystems.icrc.psm.data.models.Transaction
import com.baosystems.icrc.psm.services.PreferenceProvider
import com.baosystems.icrc.psm.services.StockManager
import com.baosystems.icrc.psm.services.rules.RuleValidationHelper
import com.baosystems.icrc.psm.services.scheduler.BaseSchedulerProvider
import com.baosystems.icrc.psm.ui.base.BaseViewModel
import com.baosystems.icrc.psm.ui.base.ItemWatcher
import com.jakewharton.rxrelay2.PublishRelay
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import org.jetbrains.annotations.NotNull
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ManageStockViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val disposable: CompositeDisposable,
    val config: AppConfig,
    private val schedulerProvider: BaseSchedulerProvider,
    preferenceProvider: PreferenceProvider,
    private val stockManager: StockManager,
    private val ruleValidationHelper: RuleValidationHelper
): BaseViewModel(preferenceProvider) {
    // TODO: Handle cases where transaction is null. (remove transaction!!)
    val transaction = savedState.get<Transaction>(INTENT_EXTRA_TRANSACTION)!!

    private var search = MutableLiveData<SearchParametersModel>()
    private val searchRelay = PublishRelay.create<String>()
    private val entryRelay =
        PublishRelay.create<Triple<StockEntry, Long, ItemWatcher.OnQuantityValidated?>>()
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

        configureRelays()
        loadStockItems()
    }

    private fun loadStockItems() {
        search.value = SearchParametersModel(null, null, transaction.facility.uid)
    }

    fun getStockItems() = stockItems

    private fun configureRelays() {
        disposable.add(
            searchRelay
                .debounce(SEARCH_QUERY_DEBOUNCE, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { result -> search.postValue(
                        SearchParametersModel(result, null, transaction.facility.uid))
                    },
                    { it.printStackTrace() }
                )
        )

        disposable.add(
            entryRelay
                .debounce(SEARCH_QUERY_DEBOUNCE, TimeUnit.MILLISECONDS)
                .distinctUntilChanged {
                        t1, t2 -> t1.first.id == t2.first.id && t1.second == t2.second
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe({ evaluate(it.first, it.second, it.third) }, { it.printStackTrace() })
        )
    }

    fun onSearchQueryChanged(query: String) {
        searchRelay.accept(query)
    }

    fun onScanCompleted(itemCode: String) {
        search.postValue(SearchParametersModel(null, itemCode, transaction.facility.uid))
    }

    fun setItemQuantity(
        item: @NotNull StockEntry,
        qty: Long,
        callback: ItemWatcher.OnQuantityValidated?
    ) {
        entryRelay.accept(Triple(item, qty, callback))
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

    fun evaluate(item: StockEntry, qty: Long, callback: ItemWatcher.OnQuantityValidated?) {
        // TODO: Change data values to what is obtained from the repository in addition to
        //  the quantity for the transaction added
        val data = hashMapOf<String, String>()
        data.put("j3ydinp6Qp8", "156")
        data.put("oc8tn8CewiP", "-88")
        data.put("yfsEseIcEXr", "25")

        disposable.add(
            ruleValidationHelper.evaluate(config.program, transaction.facility.uid, Date(), data)
                .doOnError { it.printStackTrace() }
                .observeOn(schedulerProvider.io())
                .subscribe { ruleEffects -> callback?.validationCompleted(ruleEffects) }
        )
    }
}