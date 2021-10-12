package com.baosystems.icrc.psm.viewmodels.sync

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.baosystems.icrc.psm.R
import com.baosystems.icrc.psm.service.PreferenceProvider
import com.baosystems.icrc.psm.service.SyncManager
import com.baosystems.icrc.psm.service.scheduler.BaseSchedulerProvider
import com.baosystems.icrc.psm.utils.Constants
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import org.hisp.dhis.android.core.maintenance.D2Error
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class SyncViewModel(
    private val disposable: CompositeDisposable,
    private val schedulerProvider: BaseSchedulerProvider,
    private val preferenceProvider: PreferenceProvider,
    private val syncManager: SyncManager
) : ViewModel() {
    private val TAG = "SyncViewModel"
    private val SYNC_COMPLETED_DELAY = 1L

//    private val disposable = CompositeDisposable()

    val syncResult: MutableLiveData<Result> = MutableLiveData()
    val description: MutableLiveData<String> = MutableLiveData()
    val syncCompleted: MutableLiveData<Boolean> = MutableLiveData(false)

    // TODO: Inject SyncManager with DI

    init {
//        syncManager = d2?.let { SyncManagerImpl(it) }!!

        // TODO: Inject SchedulerProvider with DI
//        schedulerProvider = SchedulerProviderImpl()

        // TODO: Inject PreferenceProvider with DI
//        preferenceProvider = PreferenceProviderImpl(getApplication())
    }

    class Result {
        var completed: Boolean = false
        var error: String? = null
        var progress: Int = 0
        var drawableRes: Int? = null

        constructor(completed: Boolean, drawableRes: Int) {
            this.completed = completed
            this.drawableRes = drawableRes
        }

        constructor(error: String, drawableRes: Int) {
            this.error = error
            this.drawableRes = drawableRes
        }

        constructor(progress: Int) {
            this.progress = progress
        }
    }

    fun startSync() {
        syncResult.value = null // reset the value

        // TODO: Change to localized error
        description.value = "Syncing metadata..."

        // TODO: Dispose the disposable during cleanup
        disposable.add(
            syncManager.metadataSync()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui()).doOnNext{
                    // The progress currently seems to be buggy, and it occasionally
                    // returns a value more than 100%, so the workaround below was created.
                    // Can't also rely on d2Progress.isCompleted as it seems not to change
                    // to true after completion
                    val percent = Math.min(it.percentage()?.toInt() ?: 0, 100)
                    Log.d(TAG, "Progress: $percent")

                    syncResult.postValue(Result(percent))

                    // TODO: Change to localized error
                    description.postValue("Syncing metadata ($percent%)...")
                }
                .doOnError {
//                    Log.e(TAG, it.localizedMessage)
                    // TODO: Notify the user of any sync errors

                    it.printStackTrace()
                }
                .doOnComplete {
                    this.handleSyncCompleted()
                }
                .subscribe({
                    // TODO: Use the percentage returned on the progress indicator
                    //  (N.B: It sometimes exceeds 100%, so account for that)

                    Log.d(TAG, "Sync completed 2! Is complete? ${it.isComplete} Percentage: ${it.percentage()}")
//                           throw Exception("Temporarily thrown for testing")
                }, {
                    this.handleSyncError(it)
                })
        )
    }

    private fun handleSyncError(throwable: Throwable) {
        val error = if (throwable is D2Error) {
            // TODO: Change to localized error
            "Sync error: " + throwable.errorCode()
        } else {
            // TODO: Change to localized error
            "Sync Error!"
        }

        syncResult.postValue(Result(error, R.drawable.ic_outline_error_36))
        throwable.printStackTrace()
    }

    private fun handleSyncCompleted() {
        description.postValue("Syncing completed!")

        val syncDate = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern(Constants.LAST_SYNCED_DATETIME_FORMAT)
        )
        preferenceProvider.setValue(Constants.LAST_SYNC_DATE, syncDate)
        syncResult.postValue(Result(true, R.drawable.ic_outline_check_circle_36))

        // Mark as completed
        disposable.add(
            Observable.timer(SYNC_COMPLETED_DELAY, TimeUnit.SECONDS, schedulerProvider.io())
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe {
                    syncCompleted.postValue(true)
                }
        )
    }

    fun hasErrored(): Boolean {
        return syncResult.value?.error != null
    }
}