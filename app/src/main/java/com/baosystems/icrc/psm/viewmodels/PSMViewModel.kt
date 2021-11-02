package com.baosystems.icrc.psm.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.baosystems.icrc.psm.utils.Constants
import com.baosystems.icrc.psm.utils.humanReadableDateTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

open class PSMViewModel: ViewModel() {
    val lastSyncDate: MutableLiveData<String> = MutableLiveData()
//    val colorTheme: MutableLiveData<Int> =

    init {
        // TODO: Replace the last sync date below with the actual value fetched
        //  from the repository
        val formatter = DateTimeFormatter.ofPattern(Constants.LAST_SYNCED_DATETIME_FORMAT)
        val dateTime = LocalDateTime.parse("2021-09-28 14:32:33", formatter)

        // Update the last sync date based on the last stored data read
        updateLastSyncDate(dateTime)
    }

    fun updateLastSyncDate(syncDate: LocalDateTime) {
        lastSyncDate.value = syncDate.humanReadableDateTime()
    }
}