package com.baosystems.icrc.psm.data

sealed class NetworkState<out T: Any> {
    object Loading: NetworkState<Nothing>()
    object Empty: NetworkState<Nothing>()
    object NotFound: NetworkState<Nothing>()
    data class Success<out T: Any>(val result: T): NetworkState<T>()
    data class Error(val errorStringRes: Int): NetworkState<Nothing>()
}
