package com.baosystems.icrc.psm.viewmodels.splash

import android.app.Application
import android.content.Context
import android.content.res.Resources
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.baosystems.icrc.psm.service.PreferenceProvider
import com.baosystems.icrc.psm.service.scheduler.BaseSchedulerProvider
import com.baosystems.icrc.psm.utils.ConfigUtils
import com.baosystems.icrc.psm.utils.Constants
import com.baosystems.icrc.psm.utils.Sdk
import io.reactivex.disposables.CompositeDisposable
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.D2Manager
import timber.log.Timber
import java.io.IOException


class SplashViewModel(
    application: Application,
    private val disposable: CompositeDisposable,
    private val schedulerProvider: BaseSchedulerProvider,
    private val preferenceProvider: PreferenceProvider
) : AndroidViewModel(application) {

    private val _loggedIn: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().also {
            checkIfUserIsLoggedIn(application.applicationContext)
        }
    }
    val loggedIn: LiveData<Boolean> = _loggedIn

    private val _configurationIsValid: Boolean
    val configurationIsValid: Boolean
        get() = _configurationIsValid

    init {
        _configurationIsValid = isConfigurationInPlace(application.resources)
    }

    private fun checkIfUserIsLoggedIn(context: Context) {
        disposable.add(
            D2Manager.instantiateD2(Sdk.getD2Configuration(context))
                .flatMap { d2: D2 ->
                    d2.userModule().isLogged
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .doOnSuccess {
                    Timber.d("Login check completed: Status = $it")
                }
                .subscribe(
                    {isLogged ->
                        _loggedIn.postValue(isLogged)
                    },
                    { e ->
                        // TODO: It has been observed that Line 126 in D2Manager.instantiateD2()
                        //  throws an error when the uid() method is called on a null user object,
                        //  so decide if the logic below is sufficient in such situations.
                        //  See if you can send the issue/error to a remote service
                        //  (e.g. crash reporting)
                        e.printStackTrace()
                        Timber.e("Unable to initialize session with previously logged in user")

                        _loggedIn.postValue(false)
                        // TODO: To prevent D2ErrorCode.ALREADY_AUTHENTICATED errors,
                        //  force a logout in the background. There's currently no way
                        //  to force logout as the d2 reference is null. The only way
                        //  around this for now is to uninstall the app
                    }
                )
        )

    }

//    fun isLoggedIn(): LiveData<Boolean> {
//        return this.loggedIn
//    }

    fun hasSyncedMetadata(): Boolean = preferenceProvider.contains(Constants.LAST_SYNC_DATE)

    /**
     * Verify if the parameters the application requires to function is in place.
     * The required properties are program id, item code id, item value id, and stock on hand id
     */
    private fun isConfigurationInPlace(res: Resources): Boolean {
        var conditionsMet = false
        val configurationKeys = listOf("program", "item_code", "item_value", "stock_on_hand")

        try {
            val props = ConfigUtils.loadConfigFile(res)
            conditionsMet = configurationKeys.all {
                val found = !ConfigUtils.getConfigValue(props, it).isNullOrEmpty()
                if (!found)
                    Timber.w("The configuration for '$it' is missing")

                found
            }
        } catch (ioe: IOException) {
            Timber.e(ioe)
        }

        return conditionsMet
    }
}