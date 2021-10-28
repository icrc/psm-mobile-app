package com.baosystems.icrc.psm.utils

import android.widget.AutoCompleteTextView
import androidx.databinding.BindingAdapter
import com.baosystems.icrc.psm.utils.Constants.LAST_SYNCED_DATETIME_FORMAT
import com.baosystems.icrc.psm.utils.Constants.TRANSACTION_DATE_FORMAT
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// TODO: Currently not working in the layout files. Fix the issue
fun LocalDateTime.humanReadableDateTime(): String =
    this.format(DateTimeFormatter.ofPattern(LAST_SYNCED_DATETIME_FORMAT))

fun LocalDateTime.humanReadableDate(): String =
    this.format(DateTimeFormatter.ofPattern(TRANSACTION_DATE_FORMAT))

@BindingAdapter("date")
fun setDate(view: AutoCompleteTextView, date: LocalDateTime) =
    view.setText(date.humanReadableDate())