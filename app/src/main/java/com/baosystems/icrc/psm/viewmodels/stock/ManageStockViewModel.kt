package com.baosystems.icrc.psm.viewmodels.stock

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.baosystems.icrc.psm.data.TransactionType
import com.baosystems.icrc.psm.data.models.IdentifiableModel
import com.baosystems.icrc.psm.service.MetadataManager
import com.baosystems.icrc.psm.service.scheduler.BaseSchedulerProvider
import com.baosystems.icrc.psm.viewmodels.PSMViewModel
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import timber.log.Timber
import java.util.concurrent.TimeUnit

class ManageStockViewModel(
    private val disposable: CompositeDisposable,
    private val schedulerProvider: BaseSchedulerProvider,
    val metadataManager: MetadataManager,
    var transactionType: TransactionType,
    var facility: IdentifiableModel,
    var transactionDate: String,
    var distributedTo: IdentifiableModel?
): PSMViewModel() {
    private var search = MutableLiveData<String>()
    private val searchRelay = PublishRelay.create<String>()
    private val _stockItems = Transformations.switchMap(search, metadataManager::queryStock)

    init {
        if (transactionType != TransactionType.DISTRIBUTION && distributedTo != null)
            throw UnsupportedOperationException(
                "Cannot set 'distributedTo' for non-distribution transactions")

        if (transactionType == TransactionType.DISTRIBUTION && distributedTo == null)
            throw UnsupportedOperationException("'distributedTo' is mandatory for model creation")

        configureSearchRelay()
        initStockItems()
    }

    private fun initStockItems() {
        Timber.d("initStockItems(): search value = ${search.value}")
        searchRelay.accept(search.value ?: "")
    }

    fun getStockItems(): LiveData<PagedList<TrackedEntityInstance>> {
        return _stockItems
    }

    private fun configureSearchRelay() {
        // TODO: Fix the issue with the disposable not being run sometimes on first run
        disposable.add(
            searchRelay
                .debounce(300, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { result ->
                        Timber.d("Distinct: $result")
                        search.postValue(result)
                    },
                    {
                        it.printStackTrace()
                        Timber.w(it, "Unable to fetch search results")
                    })
        )
    }

    fun onSearchQueryChanged(query: String) {
        searchRelay.accept(query)
    }
}