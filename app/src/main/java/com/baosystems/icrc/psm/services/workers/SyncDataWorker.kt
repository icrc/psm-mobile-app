package com.baosystems.icrc.psm.services.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.baosystems.icrc.psm.commons.Constants
import com.baosystems.icrc.psm.data.AppConfig
import com.baosystems.icrc.psm.services.PreferenceProvider
import com.baosystems.icrc.psm.services.SyncManager
import com.baosystems.icrc.psm.utils.DateUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.time.LocalDateTime

@HiltWorker
class SyncDataWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val appConfig: AppConfig,
    private val syncManager: SyncManager,
    private val preferenceProvider: PreferenceProvider
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        var teiSynced = false

        try {
            syncManager.syncTEIs(appConfig.program)
            teiSynced = true
        } catch (e: Exception) {
            // TODO: Handle situations where the error is due to no network
            Timber.e(e)
        }

        val syncDate = LocalDateTime.now().format(DateUtils.getDateTimePattern())
        preferenceProvider.setValue(Constants.LAST_DATA_SYNC_DATE, syncDate)
        preferenceProvider.setValue(Constants.LAST_DATA_SYNC_STATUS, teiSynced)

        val syncStatus = syncManager.checkSyncStatus()
        preferenceProvider.setValue(Constants.LAST_DATA_SYNC_RESULT, syncStatus.name)

        return Result.success()
    }
}