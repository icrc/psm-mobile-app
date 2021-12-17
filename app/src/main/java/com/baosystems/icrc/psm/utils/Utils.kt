package com.baosystems.icrc.psm.utils

import android.text.TextUtils
import timber.log.Timber

class Utils {
    companion object {
        @JvmStatic
        fun isValidStockOnHand(value: String?): Boolean {
            if (value == null || TextUtils.isEmpty(value)) {
                Timber.w("Stock on hand value received is empty")
                return false
            }

            return try {
                value.toLong() >= 0
            } catch (e: Exception) {
                false
            }
        }
    }

}