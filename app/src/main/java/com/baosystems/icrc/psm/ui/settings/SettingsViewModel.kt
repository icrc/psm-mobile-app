package com.baosystems.icrc.psm.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.baosystems.icrc.psm.R
import com.baosystems.icrc.psm.data.NetworkState
import com.baosystems.icrc.psm.services.UserManager
import com.baosystems.icrc.psm.services.scheduler.BaseSchedulerProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    val disposable: CompositeDisposable,
    val schedulerProvider: BaseSchedulerProvider,
    val userManager: UserManager
): AndroidViewModel(application) {
    private val _logoutStatus: MutableLiveData<NetworkState<Boolean>> = MutableLiveData()
    val logoutStatus: LiveData<NetworkState<Boolean>>
        get() = _logoutStatus

    fun logout() {
        _logoutStatus.postValue(NetworkState.Loading)

        userManager.logout()?.let {
            disposable.add(
                it.subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(
                        { _logoutStatus.postValue(NetworkState.Success<Boolean>(true)) },
                        { error ->
                            error.printStackTrace()
                            _logoutStatus.postValue(NetworkState.Error(R.string.logout_error_message))
                        }
                    )
            )
        }
    }
}