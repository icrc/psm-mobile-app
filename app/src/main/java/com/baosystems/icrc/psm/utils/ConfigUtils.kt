package com.baosystems.icrc.psm.utils

import android.content.res.Resources
import com.baosystems.icrc.psm.R
import com.baosystems.icrc.psm.exceptions.InitializationException
import java.io.IOException
import java.util.*

object ConfigUtils {
    private const val CONFIG_RESOURCE = R.raw.config

    fun getConfigValue(configProps: Properties,  key: String): String? {
        if (key.isEmpty()) throw InitializationException("Configuration key '$key' cannot be empty")

        return configProps.getProperty(key)
    }

    /**
     * Load the configuration file
     *
     * @throws IOException An exception could be thrown while loading the config file
     * @return The Properties object corresponding to the loaded configuration file
     */
    @JvmStatic
    fun loadConfigFile(resources: Resources): Properties {
        val configProps = Properties()

        configProps.load(resources.openRawResource(CONFIG_RESOURCE))
        return configProps
    }
}
