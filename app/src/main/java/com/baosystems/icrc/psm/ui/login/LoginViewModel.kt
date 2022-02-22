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
import com.baosystems.icrc.psm.utils.isConfigComplete
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
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
        Timber.d("Open ID config: %s", openIdConfig)
    }

    class Result {
        var user: User? = null
        var error: String? = null

        constructor(user: User?) {
            this.user = user
        }

        constructor(error: String?) {
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
                    // TODO: Perform any cleanups after the background login process is done
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
        var errorCode = ""
        if (throwable is D2Error) {
            errorCode = throwable.errorCode().toString()

            // TODO: Sometimes the error is D2 ALREADY_AUTHENTICATED error,
            //  ensure you handle it appropriately
            if (throwable.errorCode() == D2ErrorCode.ALREADY_AUTHENTICATED) {
                disposable.add(
                    userManager.userName().subscribe(
                        { name -> Timber.d("The already logged in user is : $name") },
                        { err -> err.message?.let { Timber.e(it) } }
                    )
                )

            }
        }

        Timber.e(throwable)

        // TODO: Extract errors into a single location
        loginResult.postValue(Result("Login error: $errorCode"))

        throwable?.printStackTrace()
    }

    private fun handleLoginResponse(user: User?) {
        if (user != null) {
            Timber.d(user.name() + " is logged in")
            loginResult.postValue(Result(user))
        } else {
            Timber.d("user is logged in but empty")
            loginResult.postValue(Result("Login error: no user"))
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