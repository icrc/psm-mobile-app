package com.baosystems.icrc.psm.ui.sync

import android.util.Pair
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.baosystems.icrc.psm.R
import com.baosystems.icrc.psm.data.AppConfig
import com.baosystems.icrc.psm.data.NetworkState
import com.baosystems.icrc.psm.services.PreferenceProvider
import com.baosystems.icrc.psm.services.SyncManager
import com.baosystems.icrc.psm.services.scheduler.BaseSchedulerProvider
import com.baosystems.icrc.psm.utils.Constants
import com.baosystems.icrc.psm.utils.Constants.SYNC_COMPLETED_DELAY
import com.baosystems.icrc.psm.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    val disposable: CompositeDisposable,
    val config: AppConfig,
    val schedulerProvider: BaseSchedulerProvider,
    val preferenceProvider: PreferenceProvider,
    val syncManager: SyncManager
) : ViewModel() {
    private val _syncStatus: MutableLiveData<NetworkState<Boolean>> = MutableLiveData()
    val syncStatus: LiveData<NetworkState<Boolean>>
        get() = _syncStatus

    fun startSync() {
        _syncStatus.value = NetworkState.Loading // reset the value

        disposable.add(
            Observable.zip(
                syncManager.metadataSync(),
                syncManager.dataSync(config.program),
                { t1, t2 -> Pair(t1, t2) }
            )
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .doOnComplete { handleSyncCompleted() }
                .subscribe({ }, { handleSyncError(it) })
        )
    }

    private fun handleSyncError(throwable: Throwable) {
        _syncStatus.postValue(NetworkState.Error(R.string.sync_error))
        throwable.printStackTrace()
    }

    private fun handleSyncCompleted() {
        updateLastSync()

        _syncStatus.postValue(NetworkState.Success<Boolean>(false))

        // Mark as completed
        disposable.add(
            Observable.timer(SYNC_COMPLETED_DELAY, TimeUnit.SECONDS, schedulerProvider.io())
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe {
                    _syncStatus.postValue(NetworkState.Success<Boolean>(true))
                }
        )
    }

    private fun updateLastSync() {
        val syncDate = LocalDateTime.now().format(DateUtils.getDateTimePattern())
        preferenceProvider.setValue(Constants.LAST_SYNC_DATE, syncDate)
    }
}