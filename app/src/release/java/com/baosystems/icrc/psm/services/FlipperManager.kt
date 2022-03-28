package com.baosystems.icrc.psm.services

import android.content.Context
import okhttp3.Interceptor

/**
 * No-op flipper manager
 */
class FlipperManager {
    companion object {
        fun setUp(appContext: Context?): Interceptor? = null
    }
}