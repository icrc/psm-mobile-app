package com.baosystems.icrc.psm.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.baosystems.icrc.psm.utils.Sdk
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.D2Manager


class SplashViewModel(application: Application) : AndroidViewModel(application) {
    private val loggedIn: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().also {
            checkIfUserIsLoggedIn(application.applicationContext)
        }
    }

    private fun checkIfUserIsLoggedIn(context: Context) {
        D2Manager.instantiateD2(Sdk.getD2Configuration(context)!!)
            .flatMap { d2: D2 ->
                d2.userModule().isLogged
            }
            .doOnSuccess { isLogged: Boolean -> loggedIn.postValue(isLogged) }
            .doOnError { throwable: Throwable -> throwable.printStackTrace() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    fun isLoggedIn(): MutableLiveData<Boolean> {
        return this.loggedIn
    }
}