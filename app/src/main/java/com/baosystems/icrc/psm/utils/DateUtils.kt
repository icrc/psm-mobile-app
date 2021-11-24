package com.baosystems.icrc.psm.utils

import java.time.format.DateTimeFormatter

class DateUtils {
    companion object {
        @JvmStatic
        fun getDateTimePattern(): DateTimeFormatter = DateTimeFormatter.ofPattern(Constants.DATETIME_FORMAT)

        @JvmStatic
        fun getDatePattern(): DateTimeFormatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT)
    }
}