package com.baosystems.icrc.psm.ui.managestock

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import com.baosystems.icrc.psm.commons.Constants.INTENT_EXTRA_TRANSACTION
import com.baosystems.icrc.psm.commons.Constants.SEARCH_QUERY_DEBOUNCE
import com.baosystems.icrc.psm.data.AppConfig
import com.baosystems.icrc.psm.data.NullableTriple
import com.baosystems.icrc.psm.data.ReviewStockData
import com.baosystems.icrc.psm.data.TransactionType
import com.baosystems.icrc.psm.data.models.SearchParametersModel
import com.baosystems.icrc.psm.data.models.StockEntry
import com.baosystems.icrc.psm.data.models.StockItem
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
        PublishRelay.create<NullableTriple<StockItem, Long?, ItemWatcher.OnQuantityValidated?>>()
    private val stockItems = Transformations.switchMap(search) { q ->
        stockManager.search(q, transaction.facility.uid)
    }
    private val itemsCache = linkedMapOf<String, StockEntry>()

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
                        t1, t2 -> t1.first!!.id == t2.first!!.id && t1.second == t2.second
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe({ evaluate(it.first!!, it.second, it.third) }, { it.printStackTrace() })
        )
    }

    fun onSearchQueryChanged(query: String) {
        searchRelay.accept(query)
    }

    fun onScanCompleted(itemCode: String) {
        search.postValue(SearchParametersModel(null, itemCode, transaction.facility.uid))
    }

    fun setItemQuantity(
        item: @NotNull StockItem,
        qty: Long?,
        callback: ItemWatcher.OnQuantityValidated?
    ) {
        if (qty == null) {
            itemsCache.remove(item.id)
            return
        }

        entryRelay.accept(NullableTriple(item, qty, callback))

        // TODO: Accept the quantity entered if valid
        itemsCache[item.id] = StockEntry(item, qty)
    }

    fun getItemQuantity(item: StockItem) = itemsCache[item.id]?.qty

    fun getStockOnHand(item: StockItem) = itemsCache[item.id]?.stockOnHand

    fun updateStockOnHand(item: StockItem, value: String) {
        itemsCache[item.id]?.stockOnHand = value
    }

    private fun getPopulatedEntries(): MutableList<StockEntry> {
        return itemsCache.values.toMutableList()
    }

    fun getData(): ReviewStockData = ReviewStockData(transaction, getPopulatedEntries())

    private fun evaluate(item: StockItem, qty: Long?, callback: ItemWatcher.OnQuantityValidated?) {
        disposable.add(
            ruleValidationHelper.evaluate(item, qty, Date(), config.program, transaction)
                .doOnError { it.printStackTrace() }
                .observeOn(schedulerProvider.io())
                .subscribeOn(schedulerProvider.ui())
                .subscribe { ruleEffects ->
                    callback?.validationCompleted(ruleEffects)
                }
        )
    }
}