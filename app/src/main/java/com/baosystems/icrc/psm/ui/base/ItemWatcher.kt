package com.baosystems.icrc.psm.ui.base

interface ItemWatcher<T, S> {
    fun quantityChanged(item: T, value: S)
    fun getValue(item: T): S?
    fun removeItem(item:T)
}