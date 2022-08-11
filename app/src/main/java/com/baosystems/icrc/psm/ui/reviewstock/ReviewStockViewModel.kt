package com.baosystems.icrc.psm.ui.reviewstock

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import com.baosystems.icrc.psm.commons.Constants
import com.baosystems.icrc.psm.commons.Constants.INTENT_EXTRA_STOCK_ENTRIES
import com.baosystems.icrc.psm.data.AppConfig
import com.baosystems.icrc.psm.data.ReviewStockData
import com.baosystems.icrc.psm.data.RowAction
import com.baosystems.icrc.psm.data.TransactionType
import com.baosystems.icrc.psm.data.models.StockEntry
import com.baosystems.icrc.psm.data.models.Transaction
import com.baosystems.icrc.psm.data.persistence.UserActivity
import com.baosystems.icrc.psm.data.persistence.UserActivityRepository
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
import timber.log.Timber
import java.time.LocalDateTime
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ReviewStockViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val disposable: CompositeDisposable,
    private val schedulerProvider: BaseSchedulerProvider,
    preferenceProvider: PreferenceProvider,
    private val stockManager: StockManager,
    private val userActivityRepository: UserActivityRepository,
    private val ruleValidationHelper: RuleValidationHelper,
    speechRecognitionManager: SpeechRecognitionManager
) : SpeechRecognitionAwareViewModel(
    preferenceProvider,
    schedulerProvider,
    speechRecognitionManager
) {
    // TODO: Figure out a better way than using !!
    val data = savedState.get<ReviewStockData>(INTENT_EXTRA_STOCK_ENTRIES)!!
    val transaction = data.transaction

    val config: AppConfig = savedState.get<AppConfig>(Constants.INTENT_EXTRA_APP_CONFIG)
        ?: throw InitializationException("Some configuration parameters are missing")

    private var search = MutableLiveData<String>()
    private val searchRelay = PublishRelay.create<String>()
    private val entryRelay = PublishRelay.create<RowAction>()

    private val _reviewedItems: MutableLiveData<List<StockEntry>> = MutableLiveData(data.items)
    val reviewedItems: LiveData<List<StockEntry>>
        get() = _reviewedItems

    private val _reviewedItemsCount = Transformations.map(_reviewedItems) { it.size }

    private val _commitStatus = MutableLiveData(false)
    val commitStatus: LiveData<Boolean>
        get() = _commitStatus

    init {
        speechRecognitionManager.supportNegativeNumberInput(
            transaction.transactionType == TransactionType.CORRECTION
        )

        configureRelays()
        loadPopulatedItems()
    }

    private fun loadPopulatedItems() {
        search.value = ""
    }

    // TODO: Find a way to reuse this function, as the same is being used by ManageStockModel
    private fun configureRelays() {
        disposable.add(
            searchRelay
                .debounce(Constants.SEARCH_QUERY_DEBOUNCE, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { query -> _reviewedItems.postValue(performSearch(query)) },
                    {
                        it.printStackTrace()
                    })
        )

        disposable.add(
            entryRelay
                .debounce(Constants.QUANTITY_ENTRY_DEBOUNCE, TimeUnit.MILLISECONDS)
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

    fun removeItem(entry: StockEntry) {
        _reviewedItems.value?.apply {
            _reviewedItems.value = this.filter { it.item.id != entry.item.id }
        }
    }

    fun updateItem(
        entry: StockEntry, qty: String?, stockOnHand: String?,
        hasError: Boolean = false
    ) {
        _reviewedItems.value?.let { items ->
            val itemIndex = items.indexOfFirst { it.item.id == entry.item.id }

            if (itemIndex >= 0) {
                val newEntry = entry.copy(qty = qty, hasError = hasError)
                if (!hasError) {
                    newEntry.stockOnHand = stockOnHand
                }

                val newList = items.toMutableList()
                newList[itemIndex] = newEntry
                _reviewedItems.postValue(newList.toList())
            }
        }
    }

    fun setQuantity(
        item: @NotNull StockEntry,
        position: @NotNull Int,
        qty: @Nullable String,
        callback: @Nullable ItemWatcher.OnQuantityValidated?
    ) {
        entryRelay.accept(RowAction(item.copy(qty = qty), position, callback))
    }

    fun getItemQuantity(item: StockEntry) = item.qty

    fun getItemStockOnHand(item: StockEntry) = item.stockOnHand

    fun onSearchQueryChanged(query: String) = searchRelay.accept(query)

    private fun performSearch(q: String?): List<StockEntry> {
        return if (q == null || q.isEmpty())
            data.items
        else
            data.items.filter { it.item.name.contains(q, true) }
    }

    fun commitTransaction() {
        if (reviewedItems.value == null || reviewedItems.value?.isEmpty() == true) {
            Timber.w("No items to commit")
            return
        }

        disposable.add(
            stockManager.saveTransaction(reviewedItems.value!!, transaction, config)
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

    /**
     * Stock entries can be committed if there are items in the list,
     * and none of the entries have errors
     */
    fun canCommit(): Boolean {
        val items = _reviewedItems.value
        return items?.size ?: 0 > 0 && (items?.none { it.hasError } ?: false)
    }

    fun getReviewedItemsCount() = _reviewedItemsCount
}