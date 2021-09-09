package com.baosystems.icrc.pharmacystockmanagement.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun LocalDateTime.humanReadable(): String =
    this.format(DateTimeFormatter.ofPattern(Constants.DATETIME_FORMAT))