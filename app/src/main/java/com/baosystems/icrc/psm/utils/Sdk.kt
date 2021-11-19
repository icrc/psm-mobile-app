package com.baosystems.icrc.psm.utils

import android.content.Context
import com.baosystems.icrc.psm.BuildConfig
import com.baosystems.icrc.psm.R
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.D2Configuration
import org.hisp.dhis.android.core.D2Manager

class Sdk {
    companion object {
        @JvmStatic
        fun d2(context: Context): D2 {
            return try {
                D2Manager.getD2()
            } catch (e: IllegalStateException) {
                D2Manager.blockingInstantiateD2(getD2Configuration(context))
            }
        }

        fun getD2Configuration(context: Context): D2Configuration {
            // This will be null if not debug mode to make sure your data is safe
//            val flipperInterceptor: Interceptor? = FlipperManager.setUp(context.applicationContext)
//            val networkInterceptors: MutableList<Interceptor> = ArrayList()
//
//            if (flipperInterceptor != null) {
//                networkInterceptors.add(flipperInterceptor)
//            }

            return D2Configuration.builder()
                .appName(context.getString(R.string.app_name))
                .appVersion(BuildConfig.VERSION_NAME)
                .readTimeoutInSeconds(30)
                .connectTimeoutInSeconds(30)
                .writeTimeoutInSeconds(30)
//                .networkInterceptors(networkInterceptors)
                .context(context)
                .build()
        }
    }
}