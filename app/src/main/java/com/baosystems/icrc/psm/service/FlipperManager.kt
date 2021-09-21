package com.baosystems.icrc.psm.service

import android.content.Context
import com.baosystems.icrc.psm.BuildConfig
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.soloader.SoLoader
import okhttp3.Interceptor

class FlipperManager {
    companion object {
        fun setUp(appContext: Context?): Interceptor? {
            return if (BuildConfig.DEBUG && FlipperUtils.shouldEnableFlipper(appContext)) {
                SoLoader.init(appContext, false)

                val client = AndroidFlipperClient.getInstance(appContext)
                val networkPlugin = NetworkFlipperPlugin()
                client.addPlugin(networkPlugin)
                client.addPlugin(
                    DatabasesFlipperPlugin(appContext)
                )
                client.addPlugin(
                    InspectorFlipperPlugin(appContext, DescriptorMapping.withDefaults())
                )
                client.start()
                FlipperOkhttpInterceptor(networkPlugin)
            } else {
                null
            }
        }
    }
}