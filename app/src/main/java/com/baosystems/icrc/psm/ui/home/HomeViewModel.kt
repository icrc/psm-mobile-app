package com.baosystems.icrc.psm.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.baosystems.icrc.psm.R
import com.baosystems.icrc.psm.commons.Constants.USER_ACTIVITY_COUNT
import com.baosystems.icrc.psm.data.AppConfig
import com.baosystems.icrc.psm.data.NetworkState
import com.baosystems.icrc.psm.data.TransactionType
import com.baosystems.icrc.psm.data.models.Transaction
import com.baosystems.icrc.psm.data.persistence.UserActivity
import com.baosystems.icrc.psm.data.persistence.UserActivityRepository
import com.baosystems.icrc.psm.exceptions.UserIntentParcelCreationException
import com.baosystems.icrc.psm.services.MetadataManager
import com.baosystems.icrc.psm.services.PreferenceProvider
import com.baosystems.icrc.psm.services.scheduler.BaseSchedulerProvider
import com.baosystems.icrc.psm.ui.base.BaseViewModel
import com.baosystems.icrc.psm.utils.ParcelUtils
import com.baosystems.icrc.psm.utils.humanReadableDate
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    val disposable: CompositeDisposable,
    val config: AppConfig,
    private val schedulerProvider: BaseSchedulerProvider,
    preferenceProvider: PreferenceProvider,
    private val metadataManager: MetadataManager,
    private val userActivityRepository: UserActivityRepository
): BaseViewModel(preferenceProvider) {
    // TODO: Move all the properties below into a singular object
    var program: Program? = null

    private val _transactionType =  MutableLiveData<TransactionType>()
    val transactionType: LiveData<TransactionType>
        get() = _transactionType

    private val _isDistribution: MutableLiveData<Boolean> = MutableLiveData(false)
    val isDistribution: LiveData<Boolean>
        get() = _isDistribution

    private val _facility: MutableLiveData<OrganisationUnit> = MutableLiveData()
    val facility: LiveData<OrganisationUnit>
        get() = _facility

    private val _transactionDate: MutableLiveData<LocalDateTime> =
        MutableLiveData(LocalDateTime.now())
    val transactionDate: LiveData<LocalDateTime>
        get() = _transactionDate

    private val _destination: MutableLiveData<Option> = MutableLiveData(null)
    private val destination: LiveData<Option>
        get() = _destination

    private val _facilities = MutableLiveData<NetworkState<List<OrganisationUnit>>>()
    val facilities: LiveData<NetworkState<List<OrganisationUnit>>>
        get() =  _facilities

    private val _destinations = MutableLiveData<NetworkState<List<Option>>>()
    val destinationsList: LiveData<NetworkState<List<Option>>>
        get() = _destinations

    private val _recentActivities: MutableLiveData<NetworkState<List<UserActivity>>> = MutableLiveData()
    val recentActivities: LiveData<NetworkState<List<UserActivity>>>
        get() = _recentActivities

    init {
        loadFacilities()
        loadDestinations()
        loadRecentActivities()
    }

    private fun loadDestinations() {
        _destinations.postValue(NetworkState.Loading)

        disposable.add(
            metadataManager.destinations()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { _destinations.postValue(NetworkState.Success<List<Option>>(it)) },
                    {
                        it.printStackTrace()
                        _destinations.postValue(NetworkState.Error(R.string.destinations_load_error))
                    })
        )
    }

    private fun loadFacilities() {
        _facilities.postValue(NetworkState.Loading)

        disposable.add(
            metadataManager.facilities(config.program)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    {
                        _facilities.postValue(NetworkState.Success(it))

                        if (it.size == 1) _facility.postValue(it[0])
                    }, {
                        it.printStackTrace()
                        _facilities.postValue(NetworkState.Error(R.string.facilities_load_error))
                    }
                )
        )
    }

    private fun loadRecentActivities() {
        _recentActivities.postValue(NetworkState.Loading)

        disposable.add(
            userActivityRepository.getRecentActivities(USER_ACTIVITY_COUNT)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { _recentActivities.postValue(NetworkState.Success(it)) },
                    {
                        it.printStackTrace()
                        _recentActivities.postValue(
                            NetworkState.Error(R.string.recent_activities_load_error))
                    }
                )
        )
    }

    fun selectTransaction(type: TransactionType) {
        _transactionType.value = type
        _isDistribution.value = type == TransactionType.DISTRIBUTION

        // Distributed to cannot only be set for DISTRIBUTION,
        // so ensure you clear it for others if it has been set
        if (type != TransactionType.DISTRIBUTION) {
            _destination.value = null
        }
    }

    fun setFacility(facility: OrganisationUnit) {
        _facility.value = facility
    }

    fun setDestination(destination: Option?) {
        if (isDistribution.value == false)
            throw UnsupportedOperationException(
                "Cannot set 'distributed to' for non-distribution transactions")

        _destination.value = destination
    }

    fun readyManageStock(): Boolean {
        if (transactionType.value == null) return false

        if (isDistribution.value == true) {
            return !(_destination.value == null
                    || _facility.value == null
                    || transactionDate.value == null)
        }

        return _facility.value != null && transactionDate.value != null
    }

    fun getData(): Transaction {
        if (transactionType.value == null)
            throw UserIntentParcelCreationException(
                "Unable to create parcel with empty transaction type")

        if (facility.value == null)
            throw UserIntentParcelCreationException(
                "Unable to create parcel with empty facility")

        if (transactionDate.value == null)
            throw UserIntentParcelCreationException(
                "Unable to create parcel with empty transaction date")

        return Transaction(
            transactionType.value!!,
            ParcelUtils.facilityToIdentifiableModelParcel(facility.value!!),
            transactionDate.value!!.humanReadableDate(),
            destination.value?.let { ParcelUtils.distributedTo_ToIdentifiableModelParcel(it) }
        )
    }

    fun setTransactionDate(epoch: Long) {
        _transactionDate.value = Instant.ofEpochMilli(epoch)
            .atZone(ZoneId.systemDefault()
            )
            .toLocalDateTime()
    }
}