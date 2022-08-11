package com.baosystems.icrc.psm.ui.sync

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.work.WorkInfo
import com.baosystems.icrc.psm.services.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val syncManager: SyncManager
) : ViewModel() {

    fun startSync() {
        syncManager.sync()
    }

    fun getSyncStatus(workName: String): LiveData<List<WorkInfo>> =
        syncManager.getSyncStatus(workName)
}