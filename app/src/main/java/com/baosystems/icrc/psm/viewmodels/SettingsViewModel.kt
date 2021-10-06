package com.baosystems.icrc.psm.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.baosystems.icrc.psm.service.scheduler.BaseSchedulerProvider
import com.baosystems.icrc.psm.service.scheduler.SchedulerProviderImpl
import com.baosystems.icrc.psm.service.UserManager
import com.baosystems.icrc.psm.service.UserManagerImpl
import com.baosystems.icrc.psm.utils.ActivityManager
import com.baosystems.icrc.psm.utils.Sdk
import com.baosystems.icrc.psm.views.activities.LoginActivity
import io.reactivex.disposables.CompositeDisposable

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    // TODO: Inject UserManager with DI
    private val userManager: UserManager

    // TODO: Inject CompositeDisposable with DI
    private val disposable = CompositeDisposable()

    // TODO: Inject SchedulerProvider using DI
    private val schedulerProvider: BaseSchedulerProvider

    init {
        val d2 = Sdk.d2()
        userManager = d2?.let { UserManagerImpl(it) }!!

        schedulerProvider = SchedulerProviderImpl()
    }

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