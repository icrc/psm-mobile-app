package com.baosystems.icrc.psm.views.adapters

import android.text.Editable

interface ItemWatcher<T, S> {
    fun beforeTextChanged(position: Int, s: CharSequence?, start: Int, count: Int, after: Int)
    fun onTextChanged(position: Int, s: CharSequence?, start: Int, before: Int, count: Int)
    fun afterTextChanged(position: Int, s: Editable?)
    fun getValue(item: T): S?
}