package com.baosystems.icrc.pharmacystockmanagement.data.models

import com.baosystems.icrc.pharmacystockmanagement.data.TransactionType
import java.time.LocalDateTime

data class UserActivity(
    val type: TransactionType,
    val date: LocalDateTime,
    var distributedTo: Destination? = null,
)
