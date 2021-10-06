package com.baosystems.icrc.psm.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.baosystems.icrc.psm.service.*
import com.baosystems.icrc.psm.service.scheduler.BaseSchedulerProvider
import com.baosystems.icrc.psm.service.scheduler.SchedulerProviderImpl
import com.baosystems.icrc.psm.utils.Constants
import com.baosystems.icrc.psm.utils.Sdk
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.maintenance.D2Error
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SyncViewModel(application: Application) : AndroidViewModel(application) {
    val TAG = "SyncViewModel"

    val syncResult: MutableLiveData<Result> = MutableLiveData()
    val description: MutableLiveData<String> = MutableLiveData()

    // TODO: Inject SyncManager with DI
    private val syncManager: SyncManager

    private val schedulerProvider: BaseSchedulerProvider
    private val preferenceProvider: PreferenceProvider

    init {
        // TODO: Inject D2 with DI
        val d2: D2? = Sdk.d2()
        syncManager = d2?.let { SyncManagerImpl(it) }!!

        // TODO: Inject SchedulerProvider with DI
        schedulerProvider = SchedulerProviderImpl()

        // TODO: Inject PreferenceProvider with DI
        preferenceProvider = PreferenceProviderImpl(getApplication())
    }

    class Result {
        var completed: Boolean = false
        var error: String? = null

        constructor(completed: Boolean) {
            this.completed = completed
        }

        constructor(error: String) {
            this.error = error
        }
    }

    fun startSync() {
        // TODO: Change to localized error
        description.value = "Syncing metadata..."

        syncManager.metadataSync()
            .observeOn(schedulerProvider.io())
            .subscribeOn(schedulerProvider.ui())
            .doOnComplete {
                this.handleSyncCompleted()
            }
            .doOnError {
                this.handleSyncError(it)
            }
            .subscribe()
    }

    private fun handleSyncError(throwable: Throwable) {
        var error = ""
        if (throwable is D2Error) {
            // TODO: Change to localized error
            error = "Sync error: " + throwable.errorCode()
            return
        }
        // TODO: Change to localized error
        error = "Sync Error!"
        syncResult.postValue(Result(error))
    }

    private fun handleSyncCompleted() {
        description.value = "Syncing completed!"
        val syncDate = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern(Constants.LAST_SYNCED_DATETIME_FORMAT)
        )
        preferenceProvider.setValue(Constants.LAST_SYNC_DATE, syncDate)
        syncResult.postValue(Result(true))
    }
}