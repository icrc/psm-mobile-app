package com.baosystems.icrc.psm.ui.settings

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.baosystems.icrc.psm.R
import com.baosystems.icrc.psm.commons.Constants.INSTANT_DATA_SYNC
import com.baosystems.icrc.psm.data.OperationState
import com.baosystems.icrc.psm.services.SyncManager
import com.baosystems.icrc.psm.services.UserManager
import com.baosystems.icrc.psm.services.preferences.PreferenceProvider
import com.baosystems.icrc.psm.services.scheduler.BaseSchedulerProvider
import com.baosystems.icrc.psm.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val disposable: CompositeDisposable,
    private val schedulerProvider: BaseSchedulerProvider,
    private val preferenceProvider: PreferenceProvider,
    private val userManager: UserManager,
    private val syncManager: SyncManager
): BaseViewModel(preferenceProvider, schedulerProvider) {
    private val _logoutStatus: MutableLiveData<OperationState<Boolean>> = MutableLiveData()
    val logoutStatus: LiveData<OperationState<Boolean>>
        get() = _logoutStatus

    private val _loggedInUser: MutableLiveData<String> = MutableLiveData()
    val loggedInUser: LiveData<String>
        get() = _loggedInUser

    init {
        disposable.add(
            userManager.userName()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    {   username -> username?.let { _loggedInUser.value = it } },
                    { it.printStackTrace() }
                )
        )
    }

    fun logout() {
        _logoutStatus.postValue(OperationState.Loading)

        userManager.logout()?.let {
            disposable.add(
                it.subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(
                        { _logoutStatus.postValue(OperationState.Success<Boolean>(true)) },
                        { error ->
                            error.printStackTrace()
                            _logoutStatus.postValue(OperationState.Error(R.string.logout_error_message))
                        }
                    )
            )
        }
    }

    fun syncData() {
        syncManager.dataSync()
    }

    fun getSyncDataStatus() = syncManager.getSyncStatus(INSTANT_DATA_SYNC)
    fun preferenceDataStore(context: Context) = preferenceProvider.preferenceDataStore(context)
}