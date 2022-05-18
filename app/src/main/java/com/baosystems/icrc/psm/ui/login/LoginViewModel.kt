package com.baosystems.icrc.psm.ui.login

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.baosystems.icrc.psm.BuildConfig
import com.baosystems.icrc.psm.R
import com.baosystems.icrc.psm.commons.Constants
import com.baosystems.icrc.psm.data.OperationState
import com.baosystems.icrc.psm.services.OpenIdProvider
import com.baosystems.icrc.psm.services.UserManager
import com.baosystems.icrc.psm.services.preferences.PreferenceProvider
import com.baosystems.icrc.psm.services.scheduler.BaseSchedulerProvider
import com.baosystems.icrc.psm.ui.base.BaseViewModel
import com.baosystems.icrc.psm.utils.Sdk
import com.baosystems.icrc.psm.utils.isConfigComplete
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import org.hisp.dhis.android.core.user.User
import org.hisp.dhis.android.core.user.openid.IntentWithRequestCode
import org.hisp.dhis.android.core.user.openid.OpenIDConnectConfig
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val disposable: CompositeDisposable,
    private val schedulerProvider: BaseSchedulerProvider,
    private val preferenceProvider: PreferenceProvider,
    private val userManager: UserManager,
    openIdProvider: OpenIdProvider
) : BaseViewModel(preferenceProvider, schedulerProvider) {
    val username: MutableLiveData<String> = MutableLiveData()
    val password: MutableLiveData<String> = MutableLiveData()

    val serverUrl = BuildConfig.SERVER_URL
    val loginInProgress: MutableLiveData<Boolean> = MutableLiveData(false)
    val canLogin: MutableLiveData<Boolean> = MutableLiveData(false)

    private val loginResult: MutableLiveData<Result> = MutableLiveData()
    private var openIdConfig: OpenIDConnectConfig?

    private val _openIdResult: MutableLiveData<OperationState<IntentWithRequestCode>?> =
        MutableLiveData(null)
    val openIdResult: LiveData<OperationState<IntentWithRequestCode>?> = _openIdResult

    init {
        disposable.add(
            loadUserCredentials().subscribe()
        )

        openIdConfig = openIdProvider.loadProvider()
    }

    class Result {
        var user: User? = null
        var error: Int? = null

        constructor(user: User?) {
            this.user = user
        }

        constructor(error: Int?) {
            this.error = error
        }
    }

    fun login() {
        val loginUser = username.value
        val loginPwd = password.value

        if (loginUser == null || loginPwd == null) {
          return
        }

        loginInProgress.value = true
        updateLoginStatus()

        disposable.add(
            userManager
                .login(loginUser, loginPwd, serverUrl)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .doOnComplete {
                    disposable.add(saveUserCredentials().subscribe())
                }
                .doOnTerminate {
                    // Perform any cleanups after the background login process is done
                    loginInProgress.value = false
                    updateLoginStatus()
                }
                .subscribe(
                    { u -> this.handleLoginResponse(u) },
                    { throwable -> this.handleError(throwable) }
                )
        )
    }

    fun openIdLogin() {
        // Cannot proceed if the OpenID config object is empty
        if (openIdConfig == null) {
            Timber.e("Unable to load the OpenID configuration file")
            _openIdResult.value = OperationState.Error(R.string.openid_config_load_error)
            return
        }

        // Check if the OpenID configuration properties have all been set
        if (!openIdConfig!!.isConfigComplete()) {
            Timber.e("One or more required OpenID configuration properties have not been set")

            _openIdResult.value = OperationState.Error(R.string.unexpected_openid_authentication_error)
            return
        }

        disposable.add(
            userManager.login(openIdConfig!!)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { handleOpenIdLoginResponse(it) },
                    { throwable -> this.handleError(throwable) }
                )
        )
    }

    private fun handleOpenIdLoginResponse(intentWithRequestCode: IntentWithRequestCode?) {
        if (intentWithRequestCode != null) {
            _openIdResult.value = OperationState.Success(intentWithRequestCode)
        } else {
            Timber.e("The intent with request code for OpenID authentication callback is empty")

            _openIdResult.value = OperationState.Error(
                R.string.unexpected_openid_authentication_error)
        }
    }

    fun handleOpenIdAuthResponseData(data: Intent, requestCode: Int) {
        loginInProgress.value = true

        disposable.add(
            userManager.handleAuthData(BuildConfig.SERVER_URL, data, requestCode)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .doOnTerminate {
                    loginInProgress.value = false
                }
                .subscribe(
                    { u -> this.handleLoginResponse(u) },
                    { throwable -> this.handleError(throwable) }
                )
        )
    }

    private fun handleError(throwable: Throwable?) {
        throwable?.printStackTrace()

        loginResult.postValue(
            Result(throwable?.let { Sdk.getFriendlyErrorMessage(throwable) })
        )
    }

    private fun handleLoginResponse(user: User?) {
        if (user != null) {
            Timber.d(user.name() + " is logged in")
            loginResult.postValue(Result(user))
        } else {
            Timber.e("User is logged in but the User object is null")
            loginResult.postValue(Result(R.string.error_unexpected))
        }
    }

    fun loginDataChanged() {
        updateLoginStatus()
    }

    private fun isUserNameValid(username: String?): Boolean {
        return username != null && username.trim().isNotEmpty()
    }

    private fun isPasswordValid(password: String?): Boolean {
        return password != null && password.trim().length > 5
    }

    fun getLoginResult(): MutableLiveData<Result> {
        return loginResult
    }

    private fun saveUserCredentials(): Completable {
        return Completable.create {
            with(preferenceProvider) {
                val prefUserName = username.value

                setValue(Constants.SERVER_URL, serverUrl)

                if (prefUserName != null)
                    setValue(Constants.USERNAME, prefUserName)
            }
        }
    }

    private fun loadUserCredentials(): Completable {
        return Completable.create {
            with(preferenceProvider) {
                username.value = getString(Constants.USERNAME, "") ?: ""
            }
        }
    }

    private fun updateLoginStatus() {
        canLogin.value = isUserNameValid(username.value) &&
                            isPasswordValid(password.value) &&
                            loginInProgress.value == false
    }
}