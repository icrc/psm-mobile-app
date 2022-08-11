package com.baosystems.icrc.psm.ui.managestock

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.baosystems.icrc.psm.commons.Constants
import com.baosystems.icrc.psm.commons.Constants.INTENT_EXTRA_TRANSACTION
import com.baosystems.icrc.psm.commons.Constants.QUANTITY_ENTRY_DEBOUNCE
import com.baosystems.icrc.psm.commons.Constants.SEARCH_QUERY_DEBOUNCE
import com.baosystems.icrc.psm.data.AppConfig
import com.baosystems.icrc.psm.data.OperationState
import com.baosystems.icrc.psm.data.ReviewStockData
import com.baosystems.icrc.psm.data.RowAction
import com.baosystems.icrc.psm.data.TransactionType
import com.baosystems.icrc.psm.data.models.SearchParametersModel
import com.baosystems.icrc.psm.data.models.StockEntry
import com.baosystems.icrc.psm.data.models.StockItem
import com.baosystems.icrc.psm.data.models.Transaction
import com.baosystems.icrc.psm.exceptions.InitializationException
import com.baosystems.icrc.psm.services.SpeechRecognitionManager
import com.baosystems.icrc.psm.services.StockManager
import com.baosystems.icrc.psm.services.preferences.PreferenceProvider
import com.baosystems.icrc.psm.services.rules.RuleValidationHelper
import com.baosystems.icrc.psm.services.scheduler.BaseSchedulerProvider
import com.baosystems.icrc.psm.ui.base.ItemWatcher
import com.baosystems.icrc.psm.ui.base.SpeechRecognitionAwareViewModel
import com.jakewharton.rxrelay2.PublishRelay
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.util.Collections
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ManageStockViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val disposable: CompositeDisposable,
    private val schedulerProvider: BaseSchedulerProvider,
    preferenceProvider: PreferenceProvider,
    private val stockManager: StockManager,
    private val ruleValidationHelper: RuleValidationHelper,
    speechRecognitionManager: SpeechRecognitionManager
) : SpeechRecognitionAwareViewModel(
    preferenceProvider,
    schedulerProvider,
    speechRecognitionManager
) {
    val transaction: Transaction = savedState.get<Transaction>(INTENT_EXTRA_TRANSACTION)
        ?: throw InitializationException("Transaction information is missing")

    val config: AppConfig = savedState.get<AppConfig>(Constants.INTENT_EXTRA_APP_CONFIG)
        ?: throw InitializationException("Some configuration parameters are missing")

    private val _itemsAvailableCount = MutableLiveData<Int>(0)
    private var search = MutableLiveData<SearchParametersModel>()
    private val searchRelay = PublishRelay.create<String>()
    private val entryRelay = PublishRelay.create<RowAction>()
    private val stockItems = Transformations.switchMap(search) { q ->
        _networkState.value = OperationState.Loading

        val result = stockManager.search(q, transaction.facility.uid, config)
        _itemsAvailableCount.value = result.totalCount

        _networkState.postValue(OperationState.Completed)
        result.items
    }
    private val itemsCache = linkedMapOf<String, StockEntry>()

    private val _networkState = MutableLiveData<OperationState<LiveData<PagedList<StockItem>>>>()
    val operationState: LiveData<OperationState<LiveData<PagedList<StockItem>>>>
        get() = _networkState

    init {
        if (transaction.transactionType != TransactionType.DISTRIBUTION &&
            transaction.distributedTo != null
        )
            throw UnsupportedOperationException(
                "Cannot set 'distributedTo' for non-distribution transactions"
            )

        if (transaction.transactionType == TransactionType.DISTRIBUTION &&
            transaction.distributedTo == null
        )
            throw UnsupportedOperationException("'distributedTo' is mandatory for model creation")

        speechRecognitionManager.supportNegativeNumberInput(
            transaction.transactionType == TransactionType.CORRECTION
        )

        configureRelays()
        loadStockItems()
    }

    private fun loadStockItems() {
        search.value = SearchParametersModel(null, null, transaction.facility.uid)
    }

    fun getStockItems() = stockItems

    fun getAvailableCount(): LiveData<Int> = _itemsAvailableCount

    private fun configureRelays() {
        disposable.add(
            searchRelay
                .debounce(SEARCH_QUERY_DEBOUNCE, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { result ->
                        search.value =
                            SearchParametersModel(result, null, transaction.facility.uid)
                    },
                    { it.printStackTrace() }
                )
        )

        disposable.add(
            entryRelay
                .debounce(QUANTITY_ENTRY_DEBOUNCE, TimeUnit.MILLISECONDS)
                .distinctUntilChanged { t1, t2 ->
                    t1.entry.item.id == t2.entry.item.id &&
                        t1.position == t2.position &&
                        t1.entry.qty == t2.entry.qty
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    {
                        disposable.add(
                            evaluate(
                                ruleValidationHelper,
                                it,
                                config.program,
                                transaction,
                                Date(),
                                config
                            )
                        )
                    },
                    {
                        it.printStackTrace()
                    }
                )
        )
    }

    fun onSearchQueryChanged(query: String) {
        searchRelay.accept(query)
    }

    fun onScanCompleted(itemCode: String) {
        search.postValue(SearchParametersModel(null, itemCode, transaction.facility.uid))
    }

    fun setQuantity(
        item: @NotNull StockItem,
        position: @NotNull Int,
        qty: @NotNull String,
        callback: @Nullable ItemWatcher.OnQuantityValidated?
    ) {
        entryRelay.accept(RowAction(StockEntry(item, qty), position, callback))
    }

    fun getItemQuantity(item: StockItem): String? {
        println(itemsCache)
        return itemsCache[item.id]?.qty
    }

    fun getStockOnHand(item: StockItem) = itemsCache[item.id]?.stockOnHand

    fun addItem(item: StockItem, qty: String?, stockOnHand: String?, hasError: Boolean) {
        // Remove from cache any item whose quantity has been cleared
        if (qty == null) {
            itemsCache.remove(item.id)
            return
        }

        itemsCache[item.id] = StockEntry(item, qty, stockOnHand, hasError)
    }

    fun removeItemFromCache(item: StockItem) = itemsCache.remove(item.id) != null

    fun hasError(item: StockItem) = itemsCache[item.id]?.hasError ?: false

    fun canReview(): Boolean = itemsCache.size > 0 && itemsCache.none { it.value.hasError }

    private fun getPopulatedEntries() = Collections.synchronizedList(itemsCache.values.toList())

    fun getData(): ReviewStockData = ReviewStockData(transaction, getPopulatedEntries())

    fun getItemCount(): Int = itemsCache.size
}