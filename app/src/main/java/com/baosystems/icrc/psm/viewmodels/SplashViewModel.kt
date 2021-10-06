package com.baosystems.icrc.psm.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.baosystems.icrc.psm.utils.Sdk
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.D2Manager


class SplashViewModel(application: Application) : AndroidViewModel(application) {
    private val disposable = CompositeDisposable()

    companion object {
        const val TAG = "SplashViewModel"
    }

    private val loggedIn: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().also {
            checkIfUserIsLoggedIn(application.applicationContext)
        }
    }

    private fun checkIfUserIsLoggedIn(context: Context) {
        disposable.add(
            D2Manager.instantiateD2(Sdk.getD2Configuration(context)!!)
                .flatMap { d2: D2 ->
                    d2.userModule().isLogged
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {isLogged ->
                        loggedIn.postValue(isLogged)
                    },
                    { e ->
                        // TODO: It has been observed that Line 126 in D2Manager.instantiateD2()
                        //  throws an error when the uid() method is called on a null user object,
                        //  so decide if the logic below is sufficient in such situations.
                        //  See if you can send the issue/error to a remote service
                        //  (e.g. crash reporting)
                        e.printStackTrace()
                        Log.e(TAG, "Unable to initialize session with previously logged in user")

                        loggedIn.postValue(false)
                        // TODO: To prevent D2ErrorCode.ALREADY_AUTHENTICATED errors,
                        //  force a logout in the background. There's currently no way
                        //  to force logout as the d2 reference is null. The only way
                        //  around this for now is to uninstall the app
                    }
                )
        )

    }

    fun isLoggedIn(): MutableLiveData<Boolean> {
        return this.loggedIn
    }

    fun cleanUp() {
        disposable.dispose()
    }
}