package com.baosystems.icrc.psm.utils

import android.content.Context
import com.baosystems.icrc.psm.service.FlipperManager
import okhttp3.Interceptor
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.D2Configuration
import org.hisp.dhis.android.core.D2Manager

class Sdk {
    companion object {
        @Throws(IllegalArgumentException::class)
        fun d2(): D2? {
            return D2Manager.getD2()
        }

        fun getD2Configuration(context: Context): D2Configuration? {
            // This will be null if not debug mode to make sure your data is safe
            val flipperInterceptor: Interceptor? = FlipperManager.setUp(context.applicationContext)
            val networkInterceptors: MutableList<Interceptor> = ArrayList()
            if (flipperInterceptor != null) {
                networkInterceptors.add(flipperInterceptor)
            }
            return D2Configuration.builder()
                // TODO: Dynamically load the app name from the resource string
                .appName("Pharmacy Stock Management")
                // TODO: Dynamically load the app version
                .appVersion("0.0.1")
                .readTimeoutInSeconds(30)
                .connectTimeoutInSeconds(30)
                .writeTimeoutInSeconds(30)
                .networkInterceptors(networkInterceptors)
                .context(context)
                .build()
        }
    }


}