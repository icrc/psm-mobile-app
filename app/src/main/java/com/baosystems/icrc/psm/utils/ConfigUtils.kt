package com.baosystems.icrc.psm.utils

import com.baosystems.icrc.psm.exceptions.InitializationException
import java.util.*

object ConfigUtils {
    fun getConfigValue(configProps: Properties,  key: String): String {
        if (key.isEmpty()) throw InitializationException("Configuration key cannot be empty")

        return configProps.getProperty(key)
    }
}
