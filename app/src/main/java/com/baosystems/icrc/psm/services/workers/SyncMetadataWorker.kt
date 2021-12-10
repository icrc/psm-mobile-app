package com.baosystems.icrc.psm.services.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.baosystems.icrc.psm.commons.Constants
import com.baosystems.icrc.psm.services.PreferenceProvider
import com.baosystems.icrc.psm.services.SyncManager
import com.baosystems.icrc.psm.utils.DateUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.time.LocalDateTime

@HiltWorker
class SyncMetadataWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncManager: SyncManager,
    private val preferenceProvider: PreferenceProvider
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        var metadataSynced = false

        try {
            syncManager.metadataSync()
            metadataSynced = true
        } catch (e: Exception) {
            // TODO: Handle situations where the error is due to no network
            Timber.e(e)
            e.printStackTrace()
        }

        val syncDate = LocalDateTime.now().format(DateUtils.getDateTimePattern())
        preferenceProvider.setValue(Constants.LAST_METADATA_SYNC_DATE, syncDate)
        preferenceProvider.setValue(Constants.LAST_METADATA_SYNC_STATUS, metadataSynced)

        if (!metadataSynced)
            return Result.failure()

        return Result.success()
    }
}