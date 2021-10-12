package com.baosystems.icrc.psm.viewmodels

import android.app.Application
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.baosystems.icrc.psm.service.*
import com.baosystems.icrc.psm.service.scheduler.BaseSchedulerProvider
import com.baosystems.icrc.psm.service.scheduler.SchedulerProviderImpl
import com.baosystems.icrc.psm.utils.Constants
import com.baosystems.icrc.psm.utils.Sdk
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.hisp.dhis.android.core.user.User

// TODO: Extend 'ViewModel' if it doesn't eventually require the application context
class LoginViewModel(
    application: Application,
    private val schedulerProvider: BaseSchedulerProvider,
    private val preferenceProvider: PreferenceProvider,
    private val userManager: UserManager
) : AndroidViewModel(application) {
    private val TAG = "LoginModel"

    // TODO: Remove the temporary values added for testing
    val username: MutableLiveData<String> = MutableLiveData()
//    val password: MutableLiveData<String> = MutableLiveData()

    // TODO: Remove later, temporarily used to fill up the password field for testing
//    val password: MutableLiveData<String> = MutableLiveData("Toromona32-Teloblast27")
    val password: MutableLiveData<String> = MutableLiveData("Bow-34-Fish?")
    val serverUrl: MutableLiveData<String> = MutableLiveData()
    val loginInProgress: MutableLiveData<Boolean> = MutableLiveData(false)
    val canLogin: MutableLiveData<Boolean> = MutableLiveData(false)

    private val loginResult: MutableLiveData<Result> = MutableLiveData()

    private val disposable = CompositeDisposable()


    init {
//        schedulerProvider = SchedulerProviderImpl()
//
//        // TODO: Inject D2
//        val d2 = Sdk.d2()
//        userManager = d2?.let { UserManagerImpl(it) }!!
//
//        preferenceProvider = SecurePreferenceProviderImpl(getApplication())
        disposable.add(
            loadUserCredentials().subscribe()
        )
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
        val loginUrl = serverUrl.value

        if (loginUser == null || loginPwd == null || loginUrl == null) {
          return
        }

        loginInProgress.value = true
        updateLoginStatus()

        disposable.add(
            userManager
                .login(loginUser, loginPwd, loginUrl)
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

    private fun handleError(throwable: Throwable?) {
        var errorCode = ""
        try {
            if (throwable is D2Error) {
                errorCode = throwable.errorCode().toString()

                // TODO: Sometimes the error is D2 ALREADY_AUTHENTICATED error,
                //  ensure you handle it appropriately
                if (throwable.errorCode() == D2ErrorCode.ALREADY_AUTHENTICATED) {
                    disposable.add(
                        userManager.userName().subscribe(
                            {name -> Log.d(TAG, "The already logged in user is : $name")},
                            {err -> err.message?.let { Log.e(TAG, it) } }
                        )
                    )

                }
            }
        } catch (e: Exception) { }

        // TODO: Extract errors into a single location
        loginResult.postValue(Result("Login error: $errorCode"))

        throwable?.printStackTrace()
    }

    private fun handleLoginResponse(user: User?) {
        if (user != null) {
            Log.d(TAG, "${user.name()} is logged in")
            loginResult.postValue(Result(user))
        } else {
            Log.d(TAG, "user is logged in but empty")
            loginResult.postValue(Result("Login error: no user"))
        }
    }

    //    fun loginDataChanged(serverUrl: String, username: String, password: String) {
    fun loginDataChanged() {
        updateLoginStatus()
    }

    private fun isServerUrlValid(serverUrl: String?): Boolean {
        return serverUrl != null &&
                Patterns.WEB_URL.matcher(serverUrl).matches()
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
                setValue(Constants.SERVER_URL, serverUrl.value)
                setValue(Constants.USERNAME, username.value)

                val prefUserName = username.value
                val prefServerUrl = serverUrl.value

                if (prefServerUrl != null)
                    setValue(Constants.SERVER_URL, prefServerUrl)

                if (prefUserName != null)
                    setValue(Constants.USERNAME, prefUserName)

            }
        }
    }

    private fun loadUserCredentials(): Completable {
        return Completable.create {
            with(preferenceProvider) {
                serverUrl.value = getString(Constants.SERVER_URL, "")
                username.value = getString(Constants.USERNAME, "")
            }
        }
    }

    private fun updateLoginStatus() {
        canLogin.value = isServerUrlValid(serverUrl.value) &&
                            isUserNameValid(username.value) &&
                            isPasswordValid(password.value) &&
                            loginInProgress.value == false
    }
}