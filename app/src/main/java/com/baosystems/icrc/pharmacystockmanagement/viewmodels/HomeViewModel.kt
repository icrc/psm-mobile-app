package com.baosystems.icrc.pharmacystockmanagement.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.baosystems.icrc.pharmacystockmanagement.data.TransactionType
import com.baosystems.icrc.pharmacystockmanagement.data.models.Destination
import com.baosystems.icrc.pharmacystockmanagement.data.models.Facility
import com.baosystems.icrc.pharmacystockmanagement.data.models.UserActivity
import com.baosystems.icrc.pharmacystockmanagement.data.repositories.DestinationRepository
import com.baosystems.icrc.pharmacystockmanagement.data.repositories.FacilityRepository
import com.baosystems.icrc.pharmacystockmanagement.data.repositories.UserActivityRepository
import com.baosystems.icrc.pharmacystockmanagement.utils.Constants
import com.baosystems.icrc.pharmacystockmanagement.utils.humanReadable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class HomeViewModel: ViewModel() {
    val transactionType: MutableLiveData<TransactionType> = MutableLiveData()
    val isDistribution: MutableLiveData<Boolean> = MutableLiveData(false)
    val facility: MutableLiveData<Facility> = MutableLiveData()

    val transactionDate: MutableLiveData<LocalDateTime> = MutableLiveData()
    val destination: MutableLiveData<Destination> = MutableLiveData()

    val lastSyncDate: MutableLiveData<String> = MutableLiveData()

    val facilitiesList = fetchSampleFacilities()
    val destinationsList = fetchSampleDestinations()
    val recentActivityList = fetchSampleRecentActivities()

    init {
        transactionDate.value = LocalDateTime.now()

        // TODO: Change to real implementation (fetch the last sync date from the repository)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        lastSyncDate.value = LocalDateTime.parse("2021-09-30 14:32:33", formatter).humanReadable()
    }

    private fun fetchSampleFacilities(): MutableLiveData<ArrayList<Facility>> {
        val repository = FacilityRepository()
        repository.addSampleFacilities()
        return MutableLiveData(repository.facilities)
    }

    private fun fetchSampleDestinations(): MutableLiveData<ArrayList<Destination>> {
        val repository = DestinationRepository()
        repository.addSampleDestinations()
        return MutableLiveData(repository.destinations)
    }

    private fun fetchSampleRecentActivities(): MutableLiveData<ArrayList<UserActivity>> {
        val repository = UserActivityRepository()
        repository.addSampleActivities()
        return MutableLiveData(repository.activities)
    }

    fun selectTransaction(type: TransactionType) {
        transactionType.value = type
        isDistribution.value = type == TransactionType.DISTRIBUTION
    }

    fun setFacility(facility: Facility) {
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
}