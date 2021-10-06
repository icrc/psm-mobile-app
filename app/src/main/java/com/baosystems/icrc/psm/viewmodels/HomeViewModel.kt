package com.baosystems.icrc.psm.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.baosystems.icrc.psm.data.TransactionType
import com.baosystems.icrc.psm.data.models.UserActivity
import com.baosystems.icrc.psm.data.repositories.UserActivityRepository
import com.baosystems.icrc.psm.service.MetadataManager
import com.baosystems.icrc.psm.service.UserManager
import com.baosystems.icrc.psm.service.scheduler.BaseSchedulerProvider
import com.baosystems.icrc.psm.utils.Constants
import io.reactivex.disposables.CompositeDisposable
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class HomeViewModel(
    private val schedulerProvider: BaseSchedulerProvider,
    private val metadataManager: MetadataManager,
    private val userManager: UserManager
): PSMViewModel() {
    val TAG = "HomeViewModel"

    private val _transactionType =  MutableLiveData<TransactionType>()
    val transactionType: MutableLiveData<TransactionType>
        get() = _transactionType

    val isDistribution: MutableLiveData<Boolean> = MutableLiveData(false)
    val facility: MutableLiveData<OrganisationUnit> = MutableLiveData()

    val transactionDate: MutableLiveData<LocalDateTime> = MutableLiveData()
    val destination: MutableLiveData<Option> = MutableLiveData()

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
        transactionDate.value = LocalDateTime.now()

        loadFacilities()
        loadDestinations()
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
                .doOnSuccess {
                    _facilities.postValue(it)
                    it.forEach { ou -> Log.d(TAG, "Facility: Uid: ${ou.uid()}, Name: ${ou.name()}") }

                    if (it.size == 1) facility.postValue(it[0])
                }.doOnError {
                    // TODO: Notify the user of an error in case the facilities cannot be fetched
                }.doOnTerminate {
                    // TODO: Remove later (temporarily used for debugging)
                    Log.d(TAG, "Finished fetching facilities (program OUs)")
                }.subscribe()
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
        this.facility.value = facility
    }

    fun setDestination(destination: Option) {
        this.destination.value = destination
    }

    fun setTransactionDate(dateTime: LocalDateTime) {
        transactionDate.value = dateTime
    }

    fun getFriendlyTransactionDate() = transactionDate.value?.format(
        DateTimeFormatter.ofPattern(Constants.TRANSACTION_DATE_FORMAT)) ?: ""

//    fun isDistribution() = transactionType.value == TransactionType.DISTRIBUTION

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
}