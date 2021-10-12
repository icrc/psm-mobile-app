package com.baosystems.icrc.psm.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.baosystems.icrc.psm.data.TransactionType
import com.baosystems.icrc.psm.data.models.UserActivity
import com.baosystems.icrc.psm.data.models.UserIntent
import com.baosystems.icrc.psm.data.repositories.UserActivityRepository
import com.baosystems.icrc.psm.exceptions.UserIntentParcelCreationException
import com.baosystems.icrc.psm.service.MetadataManager
import com.baosystems.icrc.psm.service.UserManager
import com.baosystems.icrc.psm.service.scheduler.BaseSchedulerProvider
import com.baosystems.icrc.psm.utils.Constants
import com.baosystems.icrc.psm.utils.ParcelUtils
import com.baosystems.icrc.psm.utils.humanReadableDate
import io.reactivex.disposables.CompositeDisposable
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import java.io.FileInputStream
import java.io.InputStream
import java.lang.UnsupportedOperationException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class HomeViewModel(
    private val schedulerProvider: BaseSchedulerProvider,
    private val metadataManager: MetadataManager,
    private val userManager: UserManager
): PSMViewModel() {
    val TAG = "HomeViewModel"

    var program: Program? = null

    private val _transactionType =  MutableLiveData<TransactionType>()
    val transactionType: MutableLiveData<TransactionType>
        get() = _transactionType

    val isDistribution: MutableLiveData<Boolean> = MutableLiveData(false)

    private val _facility: MutableLiveData<OrganisationUnit> = MutableLiveData()
    val facility: MutableLiveData<OrganisationUnit>
        get() = _facility

    private val _transactionDate: MutableLiveData<LocalDateTime> = MutableLiveData(null)
    val transactionDate: MutableLiveData<LocalDateTime>
        get() = _transactionDate

    private val _destination: MutableLiveData<Option> = MutableLiveData(null)
    val destination: MutableLiveData<Option>
        get() = _destination

    private val _facilities = MutableLiveData<List<OrganisationUnit>>()
    val facilities: MutableLiveData<List<OrganisationUnit>>
        get() =  _facilities

    private val _destinations = MutableLiveData<List<Option>>()
    val destinationsList: MutableLiveData<List<Option>>
        get() = _destinations

    val recentActivityList = fetchSampleRecentActivities()

    // TODO: Inject CompositeDisposable with DI
    private val disposable = CompositeDisposable()

    init {
        _transactionDate.value = LocalDateTime.now()

        loadFacilities()
        loadDestinations()
    }

    fun loadTestStockItems(q: String?): LiveData<PagedList<TrackedEntityInstance>> {
        return metadataManager.queryStock(q)
//        disposable.add(
//            metadataManager.queryStock("")
//                .subscribeOn(schedulerProvider.io())
//                .observeOn(schedulerProvider.ui())
//                .doOnSuccess {
//                    Log.d(TAG, "Successfully fetched TEIs!")
//                }
//                .doOnError {
//                    Log.e(TAG, "Unable to fetch the available TEIs: ${it.localizedMessage}")
//                }
//                .subscribe({ results ->
//                    Log.d(TAG, "TEI count: ${results.size}")
//                    results.forEach { tei ->
//                        Log.d(TAG, tei.toString())
//                        Log.d(TAG, tei.)
//                    }
//                }, { e ->
//                    Log.e(TAG, e.localizedMessage)
//                    e.printStackTrace()
//                })
//        )
    }

    private fun loadDestinations() {
        // TODO: Handle situations where the list of destinations cannot be loaded
        disposable.add(
            metadataManager.destinations()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .doOnSuccess {
                    _destinations.postValue(it)
                    Log.d(TAG, "Successfully fetched the available optionsets")
                }.doOnError {
                    Log.e(TAG, "Unable to fetch the available optionsets: ${it.localizedMessage}")
                }.subscribe({
                    it.forEach { option -> println("Option: ${option.uid()} - ${option.name()}") }
                }, {
                    Log.e(TAG, "Unable to load destinations: ${it.localizedMessage}")
                })
        )
    }

    private fun loadFacilities() {
        disposable.add(
            metadataManager.facilities()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
//                .doOnSuccess {
//                    _facilities.postValue(it)
//                    it.forEach { ou -> Log.d(TAG, "Facility: Uid: ${ou.uid()}, Name: ${ou.name()}") }
//
//                    if (it.size == 1) _facility.postValue(it[0])
//                }
//                .doOnError {
//                    // TODO: Notify the user of an error in case the facilities cannot be fetched
//                }
                .doOnTerminate {
                    // TODO: Remove later (temporarily used for debugging)
                    Log.d(TAG, "Finished fetching facilities (program OUs)")
                }
                .subscribe(
                    {
                        _facilities.postValue(it)
                        it.forEach { ou -> Log.d(TAG,
                            "Facility: Uid: ${ou.uid()}, Name: ${ou.name()}") }

                        if (it.size == 1) _facility.postValue(it[0])
                    }, {
                        // TODO: Handle errors that occur while loading the facilities
                    }
                )
        )
    }

    private fun fetchSampleRecentActivities(): MutableLiveData<ArrayList<UserActivity>> {
        val repository = UserActivityRepository()
        return MutableLiveData(repository.activities)
    }

    fun selectTransaction(type: TransactionType) {
        transactionType.value = type
        isDistribution.value = type == TransactionType.DISTRIBUTION
    }

    fun setFacility(facility: OrganisationUnit) {
        _facility.value = facility
    }

    fun setDestination(destination: Option) {
        if (isDistribution.value == false)
            throw UnsupportedOperationException(
                "Cannot set 'distributed to' for non-distribution transactions")

        _destination.value = destination
    }

    fun setTransactionDate(dateTime: LocalDateTime) {
        _transactionDate.value = dateTime
    }

    fun getFriendlyTransactionDate() = _transactionDate.value?.format(
        DateTimeFormatter.ofPattern(Constants.TRANSACTION_DATE_FORMAT)) ?: ""

    // TODO: Remove later. Temporarily used to logout
    fun logout() {
        userManager.logout()?.let {
            disposable.add(
                it.subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .doOnComplete {
                        Log.d(TAG, "Logged out successfully")
                    }
                    .subscribe()
            )
        }
    }


    // TODO: Navigate to manage stock
    fun navigateToManageStock() {

    }

    fun readyManageStock(): Boolean {
        Log.d(TAG, "Selected transaction: ${transactionType.value}")
        Log.d(TAG, "Selected facility: ${facility.value}")
        Log.d(TAG, "Selected date: ${transactionDate.value}")
        Log.d(TAG, "Selected distributed to: ${destination.value}")

        if (transactionType.value == null) return false

        if (isDistribution.value == true) {
            return !(_destination.value == null
                    || _facility.value == null
                    || _transactionDate.value == null)
        }

        return _facility.value != null && _transactionDate.value != null
    }

    fun getData(): UserIntent {
        if (transactionType.value == null)
            throw UserIntentParcelCreationException(
                "Unable to create parcel with empty transaction type")

        if (facility.value == null)
            throw UserIntentParcelCreationException(
                "Unable to create parcel with empty facility")

        if (transactionDate.value == null)
            throw UserIntentParcelCreationException(
                "Unable to create parcel with empty transaction date")

        return UserIntent(
            transactionType.value!!,
            ParcelUtils.facilityToIdentifiableModelParcel(facility.value!!),
            transactionDate.value!!.humanReadableDate(),
            destination.value?.let { ParcelUtils.distributedTo_ToIdentifiableModelParcel(it) }
        )
    }
}