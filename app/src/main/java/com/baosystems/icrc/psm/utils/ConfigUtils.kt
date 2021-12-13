package com.baosystems.icrc.psm.utils

import android.content.res.Resources
import com.baosystems.icrc.psm.R
import com.baosystems.icrc.psm.commons.Constants
import com.baosystems.icrc.psm.data.AppConfig
import com.baosystems.icrc.psm.data.TransactionType
import com.baosystems.icrc.psm.exceptions.InitializationException
import java.io.IOException
import java.util.*

object ConfigUtils {
    private const val CONFIG_RESOURCE = R.raw.config

    @JvmStatic
    fun getAppConfig(res: Resources): AppConfig {
        val configProps = loadConfigFile(res)

        // TODO: Refactor AppConfig to use a Map instead, to avoid repetition
        return AppConfig(
            getConfigValue(configProps, Constants.CONFIG_PROGRAM),
            getConfigValue(configProps, Constants.CONFIG_ITEM_CODE),
            getConfigValue(configProps, Constants.CONFIG_ITEM_VALUE),
            getConfigValue(configProps, Constants.CONFIG_STOCK_ON_HAND),
            getConfigValue(configProps, Constants.CONFIG_DE_DISTRIBUTED_TO),
            getConfigValue(configProps, Constants.CONFIG_DE_STOCK_DISTRIBUTION),
            getConfigValue(configProps, Constants.CONFIG_DE_STOCK_CORRECTION),
            getConfigValue(configProps, Constants.CONFIG_DE_STOCK_DISCARD)
        )
    }

    fun getConfigValue(configProps: Properties,  key: String): String {
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

    @JvmStatic
    fun getTransactionDataElement(transactionType: TransactionType, config: AppConfig): String {
        val dataElementUid = when (transactionType) {
            TransactionType.DISTRIBUTION -> config.stockDistribution
            TransactionType.CORRECTION -> config.stockCorrection
            TransactionType.DISCARD -> config.stockDiscarded
        }

        return dataElementUid
    }
}
