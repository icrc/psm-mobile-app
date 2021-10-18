package com.baosystems.icrc.psm.utils

import android.content.res.Resources
import com.baosystems.icrc.psm.R
import com.baosystems.icrc.psm.exceptions.InitializationException
import java.io.IOException
import java.util.*

object ConfigUtils {
    private const val CONFIG_RESOURCE = R.raw.config

    fun getConfigValue(configProps: Properties,  key: String): String {
        if (key.isEmpty()) throw InitializationException("Configuration key cannot be empty")

        return configProps.getProperty(key)
    }

    @JvmStatic
    fun loadConfigFile(resources: Resources): Properties {
        val configProps = Properties()

        try {
            configProps.load(resources.openRawResource(CONFIG_RESOURCE))
            return configProps
        } catch (e: IOException) {
            throw e
        }
    }
}
