package com.baosystems.icrc.psm.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.baosystems.icrc.psm.services.UserManager
import com.baosystems.icrc.psm.services.scheduler.BaseSchedulerProvider
import com.baosystems.icrc.psm.ui.login.LoginActivity
import com.baosystems.icrc.psm.utils.ActivityManager
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

    fun logout() {
        userManager.logout()?.let {
            disposable.add(
                it.subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(
                        { navigateToLogin() },
                        { it -> it.printStackTrace() }
                    )
            )
        }
    }

    private fun navigateToLogin() {
        ActivityManager.startActivity(getApplication(),
            LoginActivity.getLoginActivityIntent(getApplication()), true)
    }
}