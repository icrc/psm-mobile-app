package com.baosystems.icrc.pharmacystockmanagement.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// TODO: Currently not working in the layout files. Fix the issue
fun LocalDateTime.humanReadable(): String =
    this.format(DateTimeFormatter.ofPattern(Constants.LAST_SYNCED_DATETIME_FORMAT))