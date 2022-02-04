package com.baosystems.icrc.psm.utils

import android.text.TextUtils
import timber.log.Timber
import java.util.regex.Pattern

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

        /**
         * Checks if a string is a signed numeric
         *
         * @param s The string to check
         * @return A Boolean denoting the outcome of the operation
         */
        @JvmStatic
        fun isSignedNumeric(s: String) = Pattern.compile("-?\\d+").matcher(s).matches()
    }

}