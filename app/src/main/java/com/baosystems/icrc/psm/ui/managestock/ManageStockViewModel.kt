package com.baosystems.icrc.psm.ui.managestock

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import com.baosystems.icrc.psm.commons.Constants.INTENT_EXTRA_TRANSACTION
import com.baosystems.icrc.psm.commons.Constants.QUANTITY_ENTRY_DEBOUNCE
import com.baosystems.icrc.psm.commons.Constants.SEARCH_QUERY_DEBOUNCE
import com.baosystems.icrc.psm.data.AppConfig
import com.baosystems.icrc.psm.data.ReviewStockData
import com.baosystems.icrc.psm.data.RowAction
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
import org.jetbrains.annotations.Nullable
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
    private val entryRelay = PublishRelay.create<RowAction>()
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
                .debounce(QUANTITY_ENTRY_DEBOUNCE, TimeUnit.MILLISECONDS)
                .distinctUntilChanged { t1, t2 ->
                    t1.item.id == t2.item.id && t1.position == t2.position && t1.qty == t2.qty
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe({ evaluate(it) }, { it.printStackTrace() })
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
        position: @NotNull Int,
        qty: @NotNull String,
        callback: @Nullable ItemWatcher.OnQuantityValidated?
    ) {
        entryRelay.accept(RowAction(item, position, qty, callback))
    }

    fun getItemQuantity(item: StockItem) = itemsCache[item.id]?.qty

    fun getStockOnHand(item: StockItem) = itemsCache[item.id]?.stockOnHand

    fun addItem(item: StockItem, qty: String?, stockOnHand: String?, hasError: Boolean) {
        // Remove from cache any item whose quantity has been cleared
        if (qty == null) {
            itemsCache.remove(item.id)
            return
        }

        val numQty = try {
            qty.toLong()
        } catch (e: Exception) {
            0
        }
        itemsCache[item.id] = StockEntry(item, numQty, stockOnHand, hasError)
    }

    fun removeItemFromCache(item: StockItem) = itemsCache.remove(item.id) != null

    fun hasError(item: StockItem) = itemsCache[item.id]?.hasError ?: false

    fun canReview(): Boolean = itemsCache.size > 0 && itemsCache.filter { it.value.hasError }.isEmpty()

    private fun getPopulatedEntries(): MutableList<StockEntry> {
        return itemsCache.values.toMutableList()
    }

    fun getData(): ReviewStockData = ReviewStockData(transaction, getPopulatedEntries())

    /**
     * Evaluates the quantity assigned to the StockItem
     *
     * @param action The row action that comprises the item, adapter position, quantity and
     * callback invoked when the validation completes
     */
    private fun evaluate(action: RowAction) {
            disposable.add(
                ruleValidationHelper.evaluate(action.item, action.qty, Date(), config.program, transaction)
                    .doOnError { it.printStackTrace() }
                    .observeOn(schedulerProvider.io())
                    .subscribeOn(schedulerProvider.ui())
                    .subscribe { ruleEffects ->
                        action.callback?.validationCompleted(ruleEffects)
                    }
            )
    }
}