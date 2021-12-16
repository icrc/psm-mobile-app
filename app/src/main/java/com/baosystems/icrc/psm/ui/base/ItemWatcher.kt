package com.baosystems.icrc.psm.ui.base

import org.hisp.dhis.rules.models.RuleEffect

interface ItemWatcher<A, B, C> {
    fun quantityChanged(item: A, position: Int, value: B?, callback: OnQuantityValidated?)
    fun updateFields(item: A, qty: B?, position: Int, ruleEffects: List<RuleEffect>)
    fun hasError(item: A): Boolean
    fun getQuantity(item: A): Long?
    fun getStockOnHand(item: A): C?
    fun removeItem(item: A, position: Int)

    interface OnQuantityValidated {
        fun validationCompleted(ruleEffects: List<RuleEffect>)
    }
}