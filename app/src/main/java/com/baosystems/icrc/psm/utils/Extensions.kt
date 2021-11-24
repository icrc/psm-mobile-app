package com.baosystems.icrc.psm.utils

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

fun LocalDateTime.humanReadableDateTime(): String = this.format(DateUtils.getDateTimePattern())

fun LocalDateTime.humanReadableDate(): String = this.format(DateUtils.getDatePattern())

fun String.toDate(): Date? {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    return formatter.parse(this)
}