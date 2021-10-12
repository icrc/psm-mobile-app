package com.baosystems.icrc.psm.viewmodels.login

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.baosystems.icrc.psm.service.PreferenceProvider
import com.baosystems.icrc.psm.service.UserManager
import com.baosystems.icrc.psm.service.scheduler.BaseSchedulerProvider

class LoginViewModelFactory(
    private val application: Application,
    private val schedulerProvider: BaseSchedulerProvider,
    private val preferenceProvider: PreferenceProvider,
    private val userManager: UserManager
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        LoginViewModel(application, schedulerProvider, preferenceProvider, userManager) as T
}