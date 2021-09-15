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

class HomeViewModel: PSMViewModel() {
    val transactionType: MutableLiveData<TransactionType> = MutableLiveData()
    val isDistribution: MutableLiveData<Boolean> = MutableLiveData(false)
    val facility: MutableLiveData<Facility> = MutableLiveData()

    val transactionDate: MutableLiveData<LocalDateTime> = MutableLiveData()
    val destination: MutableLiveData<Destination> = MutableLiveData()

    val facilitiesList = fetchSampleFacilities()
    val destinationsList = fetchSampleDestinations()
    val recentActivityList = fetchSampleRecentActivities()

    init {
        transactionDate.value = LocalDateTime.now()
    }

    private fun fetchSampleFacilities(): MutableLiveData<ArrayList<Facility>> {
        val repository = FacilityRepository()
        return MutableLiveData(repository.facilities)
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