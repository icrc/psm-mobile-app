package com.baosystems.icrc.psm.views.adapters

interface ItemWatcher<T, S> {
    fun quantityChanged(item: T?, value: S)
    fun getValue(item: T): S?
}