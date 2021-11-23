package com.baosystems.icrc.psm.utils

object Constants {
    const val LAST_SYNCED_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss"
    const val TRANSACTION_DATE_FORMAT = "yyyy-MM-dd"

    //Preferences
    const val SHARED_PREFS = "icrc_psm_shared_prefs"

    const val SERVER_URL = "SERVER_URL"
    const val USERNAME = "USERNAME"
    const val PASSWORD = "PASSWORD"

    const val LAST_SYNC_DATE = "LAST_SYNC_DATE"
    const val ITEM_PAGE_SIZE = 10

    // Configuration file keys
    const val CONFIG_PROGRAM = "program"
    const val CONFIG_PROGRAM_STAGE = "program_stage"
    const val CONFIG_ITEM_CODE = "item_code"
    const val CONFIG_ITEM_VALUE = "item_value"
    const val CONFIG_STOCK_ON_HAND = "stock_on_hand"
    const val CONFIG_DE_DELIVER_TO = "stock_on_hand"
    const val CONFIG_DE_STOCK_DISTRIBUTION = "stock_on_hand"
    const val CONFIG_DE_STOCK_CORRECTION = "stock_on_hand"
    const val CONFIG_DE_STOCK_DISCARD = "stock_on_hand"

    const val SEARCH_QUERY_DEBOUNCE = 300L
    const val NULL_NUMBER_PLACEHOLDER = -112L

    // Intent Extras
    const val INTENT_EXTRA_TRANSACTION = "TRANSACTION_CHOICES";
    const val INTENT_EXTRA_STOCK_ENTRIES = "STOCK_ENTRIES";
}