package com.baosystems.icrc.psm.ui.sync

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.work.WorkInfo
import com.baosystems.icrc.psm.commons.Constants.INITIAL_SYNC
import com.baosystems.icrc.psm.data.AppConfig
import com.baosystems.icrc.psm.services.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    val config: AppConfig,
    private val syncManager: SyncManager
) : ViewModel() {

    fun startSync() {
        syncManager.sync()
    }

    fun getSyncStatus(): LiveData<List<WorkInfo>> = syncManager.getSyncStatus(INITIAL_SYNC)
}