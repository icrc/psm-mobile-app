package com.baosystems.icrc.psm.services

import android.content.Intent
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.user.User
import org.hisp.dhis.android.core.user.openid.IntentWithRequestCode
import org.hisp.dhis.android.core.user.openid.OpenIDConnectConfig

class UserManagerImpl(private val d2: D2) : UserManager {

    override fun login(username: String, password: String, serverUrl: String): Observable<User?> {
        return Observable.defer {
            d2.userModule().logIn(username, password, serverUrl).toObservable()
        }


//        return d2.userModule()?.logIn(username.value, password.value, serverUrl.value)
//            ?.subscribeOn(Schedulers.io())
//            ?.observeOn(AndroidSchedulers.mainThread())
//            // TODO: Extract errors into a single location
//            ?.doOnSuccess { user ->
//                run {
//                    if (user != null) {
//                        Log.d("LoginModel", "user is logged in")
//                        loginResult.postValue(LoginModel.Result(user))
//                    } else {
//                        Log.d("LoginModel", "user is logged in but empty")
//                        loginResult.postValue(LoginModel.Result("Login error: no user"))
//                    }
//                }
//            }
//            ?.doOnError { throwable ->
//                run {
//                    var errorCode = ""
//                    try {
//                        if (throwable is D2Error) {
//                            errorCode = ":" + throwable.errorCode()
//                        }
//                    } catch (e: Exception) {
//                    }
//                    loginResult.postValue(LoginModel.Result("Login error$errorCode"))
//                    throwable.printStackTrace()
//                }
//            };
    }

    override fun login(config: OpenIDConnectConfig): Observable<IntentWithRequestCode?> {
        TODO("Not yet implemented")
    }

    override fun handleAuthData(
        serverUrl: String,
        data: Intent?,
        requestCode: Int
    ): Observable<User?> {
        TODO("Not yet implemented")
    }

    override fun isUserLoggedIn(): Observable<Boolean?> {
        return Observable.defer {
            d2.userModule().isLogged.toObservable()
        }
    }

    override fun userName(): Single<String?> {
        return Single.defer{
            d2.userModule().userCredentials().get().map { it.username() }
        }
    }

    override fun logout(): Completable? {
        return d2.userModule().logOut()
    }
}