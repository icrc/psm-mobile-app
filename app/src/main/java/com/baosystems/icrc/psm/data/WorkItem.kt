package com.baosystems.icrc.psm.data

import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy

data class WorkItem(
    val name: String,
    val type: WorkType,
    val data: Data? = null,
    val delayInSecs: Long? = null,
    val policy: ExistingWorkPolicy? = null,
    val periodicWorkPolicy: ExistingPeriodicWorkPolicy? = null
)
