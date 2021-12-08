package com.baosystems.icrc.psm.ui.sync

import android.util.Pair
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.baosystems.icrc.psm.R
import com.baosystems.icrc.psm.data.AppConfig
import com.baosystems.icrc.psm.data.NetworkState
import com.baosystems.icrc.psm.data.SyncType
import com.baosystems.icrc.psm.services.PreferenceProvider
import com.baosystems.icrc.psm.services.SyncManager
import com.baosystems.icrc.psm.services.scheduler.BaseSchedulerProvider
import com.baosystems.icrc.psm.utils.Constants
import com.baosystems.icrc.psm.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
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

    private val SYNC_COMPLETED_DELAY = 1L

    private val _syncStatus: MutableLiveData<NetworkState<Boolean>> = MutableLiveData()
    val syncStatus: LiveData<NetworkState<Boolean>>
        get() = _syncStatus

    fun startSync() {
        _syncStatus.value = NetworkState.Loading // reset the value

        Timber.i("Downloading metadata and data...")
//        disposable.add(
//            sync(
//                { syncManager.metadataSync() },
//                {
//                    _syncStatus.postValue(NetworkState.Progress(SyncType.Metadata,
//                        (it.percentage()?.toInt() ?: 0).coerceAtMost(100)
//                    ))
//                },
//                {
//                    Timber.d("Sync completed 2! Is complete? " + it.isComplete +
//                            " Percentage: " + it.percentage())
//                    _syncStatus.postValue(NetworkState.Success<Int>(R.string.metadata_sync_progress))
//                },
//                {
//                    _syncStatus.postValue(NetworkState.Error(R.string.metadata_sync_error))
////                    syncStatus.postValue(Result(error, R.drawable.ic_outline_error_36))
//                    it.printStackTrace()
//                },
//
//            )
//        )

        disposable.add(
            Observable.zip(
                syncManager.metadataSync(),
                syncManager.dataSync(config.program),
                { t1, t2 -> Pair(t1, t2) }
            )
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .doOnNext{
                    // TODO: The progress currently seems to be buggy, and it occasionally
                    //  returns a value more than 100%, so the workaround below was created.
                    //  Can't also rely on d2Progress.isCompleted as it seems not to change
                    //  to true after completion
//                        val percent = Math.min(it.percentage()?.toInt() ?: 0, 100)
//                        Timber.d("Progress: " + percent)
//
//                        syncResult.postValue(Result(percent))
//
//                        // TODO: Change to localized error
//                        description.postValue("Syncing metadata ($percent%)...")

                    val one = (it.first.percentage()?.toInt() ?: 0).coerceAtMost(100)
                    val two = (it.second.percentage()?.toInt() ?: 0).coerceAtMost(100)
                    Timber.d("Metadata: %s, Data: %s", one.toString(), two.toString())

//                    _syncStatus.postValue(NetworkState.Progress(SyncType.Metadata,
////                        (it.first.percentage()?.toInt() ?: 0).coerceAtMost(100)
//                        one
//                    ))
                    _syncStatus.postValue(NetworkState.Progress(SyncType.Data,
//                        (it.second.percentage()?.toInt() ?: 0).coerceAtMost(100)
                        two
                    ))
                }
                .doOnComplete {
                    Timber.i("Finished downloading (meta)data!")
                    handleSyncCompleted()
                }
                .subscribe(
                    {
                        // TODO: Use the percentage returned on the progress indicator
                        //  (N.B: It sometimes exceeds 100%, so account for that)

                        Timber.d("Metadata sync completed! Is complete? " +
                                it.first.isComplete + " Percentage: " + it.first.percentage())
                        Timber.d("Data sync completed! Is complete? " +
                                it.second.isComplete + " Percentage: " + it.second.percentage())
                    }, {
                        handleSyncError(it)
                    }
                )
        )
    }

//    private fun sync(
//        observableFn: () -> Observable<D2Progress>,
//        onNextFn: (progress: D2Progress) -> Unit,
//        onSubscribeFn: (progress: D2Progress) -> Unit,
//        onErrorFn: (t: Throwable) -> Unit,
//        onCompleteFn: () -> Unit
//    ): Disposable {
//        return observableFn()
//            .subscribeOn(schedulerProvider.io())
//            .observeOn(schedulerProvider.ui())
//            .doOnNext{ onNextFn(it) }
//            .doOnComplete { onCompleteFn() }
//            .subscribe({ onSubscribeFn(it) }, { onErrorFn(it) })
//    }

//    private fun syncMetadata() {
//        _syncStatus.postValue(NetworkState.Progress(SyncType.Metadata, 0))
//
//        disposable.add(
//            syncManager.metadataSync()
//                .subscribeOn(schedulerProvider.io())
//                .observeOn(schedulerProvider.ui()).doOnNext{
//                    // TODO: The progress currently seems to be buggy, and it occasionally
//                    //  returns a value more than 100%, so the workaround below was created.
//                    //  Can't also rely on d2Progress.isCompleted as it seems not to change
//                    //  to true after completion
//                    val percent = Math.min(it.percentage()?.toInt() ?: 0, 100)
//                    Timber.d("Progress: " + percent)
//
//                    // TODO: Change to localized error
//                    description.postValue("Syncing metadata ($percent%)...")
//                    _syncStatus.postValue(NetworkState.Progress(SyncType.Metadata, percent))
//                }
//                .doOnError {
////                    Log.e(TAG, it.localizedMessage)
//                    // TODO: Notify the user of any sync errors
//
//
//                }
//                .doOnComplete {
//                    this.handleSyncCompleted()
//                }
//                .subscribe({
//                    // TODO: Use the percentage returned on the progress indicator
//                    //  (N.B: It sometimes exceeds 100%, so account for that)
//
//                    Timber.d("Sync completed 2! Is complete? " + it.isComplete + " Percentage: " + it.percentage())
////                           throw Exception("Temporarily thrown for testing")
//                    _syncStatus.postValue()
//                }, { this.handleSyncError(it) })
//        )
//    }

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