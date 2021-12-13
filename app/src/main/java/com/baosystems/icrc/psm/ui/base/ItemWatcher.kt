package com.baosystems.icrc.psm.ui.base

import org.hisp.dhis.rules.models.RuleEffect

interface ItemWatcher<T, S> {
    fun quantityChanged(item: T, value: S, callback: OnQuantityValidated?)
    fun getValue(item: T): S?
    fun removeItem(item:T)

    interface OnQuantityValidated {
        fun validationCompleted(ruleEffects: List<RuleEffect>)
    }
}