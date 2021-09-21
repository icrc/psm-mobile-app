package com.baosystems.icrc.psm.data.models

import com.baosystems.icrc.psm.data.TransactionType
import java.time.LocalDateTime

data class UserActivity(
    val type: TransactionType,
    val date: LocalDateTime,
    var distributedTo: Destination? = null,
)
