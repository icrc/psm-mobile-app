package com.baosystems.icrc.psm.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.baosystems.icrc.psm.data.TransactionType
import com.baosystems.icrc.psm.data.models.Destination
import com.baosystems.icrc.psm.data.models.UserActivity
import com.baosystems.icrc.psm.data.repositories.DestinationRepository
import com.baosystems.icrc.psm.data.repositories.UserActivityRepository
import com.baosystems.icrc.psm.service.*
import com.baosystems.icrc.psm.utils.Constants
import com.baosystems.icrc.psm.utils.Sdk
import io.reactivex.disposables.CompositeDisposable
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class HomeViewModel: PSMViewModel() {
    val TAG = "HomeViewModel"

    val transactionType: MutableLiveData<TransactionType> = MutableLiveData()
    val isDistribution: MutableLiveData<Boolean> = MutableLiveData(false)
    val facility: MutableLiveData<OrganisationUnit> = MutableLiveData()

    val transactionDate: MutableLiveData<LocalDateTime> = MutableLiveData()
    val destination: MutableLiveData<Destination> = MutableLiveData()

//    val facilitiesList = fetchSampleFacilities()
    var facilitiesList: MutableLiveData<List<OrganisationUnit>> = MutableLiveData()
    val destinationsList = fetchSampleDestinations()
    val recentActivityList = fetchSampleRecentActivities()

    // TODO: Inject UserManager using DI
    private val userManager: UserManager

    // TODO: Inject SchedulerProvider using DI
    private val schedulerProvider: SchedulerProvider

    // TODO: Inject CompositeDisposable with DI
    private val disposable = CompositeDisposable()

    // TODO: remove later. Temporarily used for testing
    private val metadataManager: MetadataManager

    init {
        transactionDate.value = LocalDateTime.now()

        schedulerProvider = SchedulerProviderImpl()

        // TODO: Inject D2
        val d2 = Sdk.d2()
        userManager = d2?.let { UserManagerImpl(it) }!!

        metadataManager = MetadataManagerImpl(d2)

        loadFacilities()
    }

    fun loadFacilities() {
        metadataManager.facilities()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSuccess {
                facilitiesList.value = it
                it.forEach { ou -> Log.d(TAG, "Facility: Uid: ${ou.uid()}, Name: ${ou.name()}") }
            }.doOnTerminate {
                Log.d(TAG, "Finished fetching facilities (program OUs)")
            }.subscribe()
    }

    private fun fetchSampleDestinations(): MutableLiveData<ArrayList<Destination>> {
        val repository = DestinationRepository()
        return MutableLiveData(repository.destinations)
    }

    private fun fetchSampleRecentActivities(): MutableLiveData<ArrayList<UserActivity>> {
        val repository = UserActivityRepository()
        return MutableLiveData(repository.activities)
    }

    fun selectTransaction(type: TransactionType) {
        transactionType.value = type
        isDistribution.value = type == TransactionType.DISTRIBUTION

        metadataManager.facilities()
    }

    fun setFacility(facility: OrganisationUnit) {
        this.facility.value = facility
    }

    fun setDestination(destination: Destination) {
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